"""
app/rag/retriever.py
────────────────────
Pinecone vector store manager using LangChain's PineconeVectorStore.

Responsibilities
────────────────
- Create the Pinecone index on first run (if it doesn't exist).
- Expose `add_documents()` for ingestion.
- Expose `as_retriever()` for the RAG chain.
- Expose `clear_namespace()` for forced re-indexing.
- Expose `vector_count()` for the health endpoint.

Embedding model
───────────────
Uses `sentence-transformers/all-MiniLM-L6-v2` (384-dim, runs locally for free).
If you switch models, update PINECONE_DIMENSION in .env to match.
"""

from __future__ import annotations

import logging
import time

from langchain_core.documents import Document
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_pinecone import PineconeVectorStore
from pinecone import Pinecone, ServerlessSpec

from app.config import settings

logger = logging.getLogger(__name__)

# _EMBEDDING_MODEL = "sentence-transformers/all-MiniLM-L6-v2"
_EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"

# Pinecone serverless cloud/region mapping
# Adjust PINECONE_ENVIRONMENT in .env to match your project's cloud+region.
_CLOUD_REGION_MAP: dict[str, tuple[str, str]] = {
    "us-east-1-aws":    ("aws",   "us-east-1"),
    "us-west-2-aws":    ("aws",   "us-west-2"),
    "eu-west-1-aws":    ("aws",   "eu-west-1"),
    "us-central1-gcp":  ("gcp",   "us-central1"),
    "gcp-starter":      ("gcp",   "us-central1"),   # legacy free tier alias
    "eastus-azure":     ("azure", "eastus"),
}


def _parse_cloud_region(environment: str) -> tuple[str, str]:
    """Return (cloud, region) from the PINECONE_ENVIRONMENT string."""
    entry = _CLOUD_REGION_MAP.get(environment.lower())
    if entry:
        return entry
    # Fallback: try to infer from common patterns like "us-east-1-aws"
    parts = environment.rsplit("-", 1)
    if len(parts) == 2:
        return parts[1], parts[0]
    logger.warning(
        "Unknown PINECONE_ENVIRONMENT '%s'. Defaulting to aws / us-east-1.", environment
    )
    return "aws", "us-east-1"


class VectorStoreManager:
    """
    Manages a Pinecone index used as the RAG knowledge base.

    Usage
    -----
    vsm = VectorStoreManager()
    vsm.add_documents(docs)            # during ingestion
    retriever = vsm.as_retriever()     # during query
    """

    def __init__(self) -> None:
        self._embeddings = HuggingFaceEmbeddings(
            model_name=_EMBEDDING_MODEL,
            model_kwargs={"device": "cpu"},
            encode_kwargs={"normalize_embeddings": True},
        )
        self._pc = Pinecone(api_key=settings.pinecone_api_key)
        self._ensure_index()
        self._store = self._load_store()

    # ── Private ───────────────────────────────────────────────────────────────

    def _ensure_index(self) -> None:
        """Create the Pinecone index if it doesn't already exist."""
        existing = {idx.name for idx in self._pc.list_indexes()}
        if settings.pinecone_index_name in existing:
            logger.info("Pinecone index '%s' already exists.", settings.pinecone_index_name)
            return

        cloud, region = _parse_cloud_region(settings.pinecone_environment)
        logger.info(
            "Creating Pinecone index '%s' (dim=%d, metric=%s, cloud=%s, region=%s).",
            settings.pinecone_index_name,
            settings.pinecone_dimension,
            settings.pinecone_metric,
            cloud,
            region,
        )
        self._pc.create_index(
            name=settings.pinecone_index_name,
            dimension=settings.pinecone_dimension,
            metric=settings.pinecone_metric,
            spec=ServerlessSpec(cloud=cloud, region=region),
        )
        # Wait until index is ready
        for attempt in range(20):
            info = self._pc.describe_index(settings.pinecone_index_name)
            if info.status.get("ready"):
                logger.info("Pinecone index is ready.")
                return
            logger.info("Waiting for index to be ready (attempt %d)…", attempt + 1)
            time.sleep(3)

        raise RuntimeError(
            f"Pinecone index '{settings.pinecone_index_name}' did not become ready in time."
        )

    def _load_store(self) -> PineconeVectorStore:
        index = self._pc.Index(settings.pinecone_index_name)
        return PineconeVectorStore(
            index=index,
            embedding=self._embeddings,
            namespace=settings.pinecone_namespace,
            text_key="name"
        )

    # ── Public ────────────────────────────────────────────────────────────────

    def add_documents(self, documents: list[Document]) -> int:
        """Upsert documents into Pinecone. Returns number of chunks added."""
        if not documents:
            return 0
        # PineconeVectorStore.add_documents returns list of IDs
        self._store.add_documents(documents)
        logger.info("Upserted %d chunks to Pinecone namespace '%s'.",
                    len(documents), settings.pinecone_namespace)
        return len(documents)

    def as_retriever(self):
        """Return a LangChain retriever configured for MMR search."""
        return self._store.as_retriever(
            search_type="mmr",
            search_kwargs={
                "k": settings.retriever_k,
                "fetch_k": settings.retriever_k * 3,
                "lambda_mult": 0.6,  # 0 = max diversity, 1 = max relevance
            },
        )

    def clear_namespace(self) -> None:
        """
        Delete all vectors in the configured namespace.
        Used when force_reload=True on /ingest.
        """
        logger.warning(
            "Clearing all vectors in Pinecone namespace '%s'.", settings.pinecone_namespace
        )
        index = self._pc.Index(settings.pinecone_index_name)
        index.delete(delete_all=True, namespace=settings.pinecone_namespace)
        # Reload the store wrapper after clearing
        self._store = self._load_store()

    def vector_count(self) -> int:
        """Return total vector count for the namespace (used by /health)."""
        try:
            index = self._pc.Index(settings.pinecone_index_name)
            stats = index.describe_index_stats()
            ns_stats = stats.namespaces.get(settings.pinecone_namespace, {})
            return ns_stats.get("vector_count", 0)
        except Exception as exc:  # noqa: BLE001
            logger.error("Failed to fetch Pinecone stats: %s", exc)
            return -1


# Module-level singleton — one Pinecone connection per process
vector_store = VectorStoreManager()
