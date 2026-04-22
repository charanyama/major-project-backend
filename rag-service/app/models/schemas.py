"""
app/models/schemas.py
─────────────────────
Pydantic v2 request / response models for all API endpoints.
"""

from __future__ import annotations

from enum import Enum
from typing import Any

from pydantic import BaseModel, Field


# ── /ask ──────────────────────────────────────────────────────────────────────

class AskRequest(BaseModel):
    question: str = Field(
        ...,
        min_length=3,
        max_length=2000,
        examples=["What is your return policy?"],
    )
    conversation_history: list[dict[str, str]] = Field(
        default_factory=list,
        description="Optional prior turns: [{'role': 'human'|'ai', 'content': '...'}]",
    )


class SourceDocument(BaseModel):
    content: str
    source: str
    metadata: dict[str, Any] = Field(default_factory=dict)


class AskResponse(BaseModel):
    answer: str
    sources: list[SourceDocument] = Field(default_factory=list)
    model: str
    retriever_k: int


# ── /ingest ───────────────────────────────────────────────────────────────────

class IngestSource(str, Enum):
    all = "all"
    docs = "docs"
    database = "database"
    apis = "apis"


class IngestRequest(BaseModel):
    source: IngestSource = IngestSource.all
    force_reload: bool = Field(
        False,
        description=(
            "If True, deletes all vectors in the Pinecone namespace "
            "before re-indexing."
        ),
    )


class IngestResponse(BaseModel):
    status: str
    documents_indexed: int
    index: str
    namespace: str
    details: dict[str, int] = Field(default_factory=dict)


# ── /health ───────────────────────────────────────────────────────────────────

class HealthResponse(BaseModel):
    status: str = "ok"
    vector_store: str
    index: str
    namespace: str
    vector_count: int
