"""
app/main.py
───────────
FastAPI application — entry point for the RAG service.

Endpoints
─────────
  GET  /health     Liveness check + Pinecone vector count
  POST /ask        Answer a question using RAG
  POST /ingest     (Re)index documents from one or all sources

Run locally
───────────
  uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
"""

from __future__ import annotations

import logging
import time

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import settings
from app.ingest.ingestor import run_ingestion
from app.models.schemas import (
    AskRequest,
    AskResponse,
    HealthResponse,
    IngestRequest,
    IngestResponse,
    SourceDocument,
)
from app.rag.pipeline import rag_pipeline
from app.rag.retriever import vector_store

# ── Logging ────────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)


# ── Application ────────────────────────────────────────────────────────────────

app = FastAPI(
    title="Ecommerce RAG Service",
    description=(
        "Retrieval-Augmented Generation microservice. "
        "Answers questions grounded in your docs, DB records, "
        "and sibling service data — powered by Groq + Pinecone."
    ),
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],      # tighten to specific origins in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Middleware: request timing ─────────────────────────────────────────────────

@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start = time.perf_counter()
    response = await call_next(request)
    ms = (time.perf_counter() - start) * 1000
    response.headers["X-Process-Time-Ms"] = f"{ms:.1f}"
    return response


# ── Global exception handler ───────────────────────────────────────────────────

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error("Unhandled exception on %s: %s", request.url.path, exc, exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error. Check service logs."},
    )


# ── Routes ─────────────────────────────────────────────────────────────────────

@app.get("/health", response_model=HealthResponse, tags=["ops"])
async def health():
    """
    Liveness check. Returns the current vector count in the Pinecone namespace.
    """
    count = vector_store.vector_count()
    return HealthResponse(
        status="ok",
        vector_store="pinecone",
        index=settings.pinecone_index_name,
        namespace=settings.pinecone_namespace,
        vector_count=count,
    )


@app.post("/ask", response_model=AskResponse, tags=["rag"])
async def ask(body: AskRequest):
    """
    Answer a user question using Retrieval-Augmented Generation.

    The answer is grounded **exclusively** in indexed documents from your
    docs directory, database tables, and sibling service APIs.

    - **question**: The user's question (3–2000 characters).
    - **conversation_history**: Optional prior turns for multi-turn conversations.
      Format: `[{"role": "human"|"ai", "content": "..."}]`

    The `sources` in the response shows which document chunks were retrieved.
    """
    try:
        result = rag_pipeline.ask(
            question=body.question,
            conversation_history=body.conversation_history,
        )
    except Exception as exc:
        logger.error("RAG pipeline error: %s", exc, exc_info=True)
        raise HTTPException(status_code=500, detail=str(exc)) from exc

    sources = [
        SourceDocument(
            content=doc.page_content[:500],           # truncate for response size
            source=doc.metadata.get("source", "unknown"),
            metadata={k: v for k, v in doc.metadata.items() if k != "source"},
        )
        for doc in result["sources"]
    ]

    return AskResponse(
        answer=result["answer"],
        sources=sources,
        model=result["model"],
        retriever_k=settings.retriever_k,
    )


@app.post("/ingest", response_model=IngestResponse, tags=["ops"])
async def ingest(body: IngestRequest):
    """
    Trigger document ingestion (or re-ingestion) into Pinecone.

    - **source**: `all` | `docs` | `database` | `apis`
    - **force_reload**: if `true`, all vectors in the namespace are deleted
      before indexing begins.

    ⚠️  Long-running — for large datasets, call this via a background job
    or a scheduled task rather than a synchronous HTTP request.
    """
    try:
        result = run_ingestion(
            source=body.source.value,
            force_reload=body.force_reload,
        )
    except Exception as exc:
        logger.error("Ingestion error: %s", exc, exc_info=True)
        raise HTTPException(status_code=500, detail=str(exc)) from exc

    # Reload retriever so the pipeline picks up newly indexed docs immediately
    rag_pipeline.reload()

    return IngestResponse(
        status="success",
        documents_indexed=result.total,
        index=settings.pinecone_index_name,
        namespace=settings.pinecone_namespace,
        details=result.details,
    )
