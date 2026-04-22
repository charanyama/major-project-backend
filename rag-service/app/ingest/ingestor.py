"""
app/ingest/ingestor.py
──────────────────────
Orchestrates all loaders and writes results to Pinecone.

Usage
─────
  As a script (first-time setup or manual re-index):
      python -m app.ingest.ingestor [all|docs|database|apis] [--force]

  Via the API (POST /ingest):
      {"source": "all", "force_reload": false}
"""

from __future__ import annotations

import logging
from dataclasses import dataclass, field

from langchain_core.documents import Document

from app.ingest.api_loader import load_api_documents
from app.ingest.db_loader import load_db_documents
from app.ingest.doc_loader import load_documents
from app.rag.retriever import vector_store

logger = logging.getLogger(__name__)


@dataclass
class IngestResult:
    total: int = 0
    details: dict[str, int] = field(default_factory=dict)


def run_ingestion(source: str = "all", force_reload: bool = False) -> IngestResult:
    """
    Load documents from one or all sources and upsert into Pinecone.

    Parameters
    ----------
    source : "all" | "docs" | "database" | "apis"
    force_reload : bool
        Clears the entire Pinecone namespace before indexing when True.

    Returns
    -------
    IngestResult  with total chunk count and per-source breakdown.
    """
    if force_reload:
        logger.warning("force_reload=True — clearing Pinecone namespace before ingestion.")
        vector_store.clear_namespace()

    result = IngestResult()

    # ── File-based documents ──────────────────────────────────────────────────
    if source in ("all", "docs"):
        docs: list[Document] = load_documents()
        n = vector_store.add_documents(docs)
        result.details["docs"] = n
        result.total += n

    # ── Database records ──────────────────────────────────────────────────────
    if source in ("all", "database"):
        db_docs: list[Document] = load_db_documents()
        n = vector_store.add_documents(db_docs)
        result.details["database"] = n
        result.total += n

    # ── Sibling service APIs ───────────────────────────────────────────────────
    if source in ("all", "apis"):
        api_docs: list[Document] = load_api_documents()
        n = vector_store.add_documents(api_docs)
        result.details["apis"] = n
        result.total += n

    logger.info(
        "Ingestion complete | source=%s | total=%d | breakdown=%s",
        source, result.total, result.details,
    )
    return result


# ── Standalone entry point ────────────────────────────────────────────────────

if __name__ == "__main__":
    import sys

    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
    )

    src   = sys.argv[1] if len(sys.argv) > 1 else "all"
    force = "--force" in sys.argv

    print(f"\n🔄  Running ingestion  |  source={src}  |  force_reload={force}\n")
    res = run_ingestion(source=src, force_reload=force)
    print(f"\n✅  Done — {res.total} chunks indexed into Pinecone.")
    print(f"    Breakdown: {res.details}\n")
