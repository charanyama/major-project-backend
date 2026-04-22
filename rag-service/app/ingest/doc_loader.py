"""
app/ingest/doc_loader.py
────────────────────────
Loads and chunks documents from the local `docs/` directory.

Supported file types
────────────────────
  .pdf   → PyPDFLoader        (text-based PDFs; scanned PDFs need OCR pre-processing)
  .md    → TextLoader         (UTF-8)
  .txt   → TextLoader         (UTF-8, autodetect fallback)
  .docx  → Docx2txtLoader
  .html  → UnstructuredHTMLLoader

Each file is split into chunks using RecursiveCharacterTextSplitter.
Metadata injected per chunk: source (relative path), file_type, loader.
"""

from __future__ import annotations

import logging
import os
from pathlib import Path

from langchain_community.document_loaders import (
    Docx2txtLoader,
    PyPDFLoader,
    TextLoader,
    UnstructuredHTMLLoader,
)
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import settings

logger = logging.getLogger(__name__)

_SUPPORTED: dict[str, type] = {
    ".pdf":  PyPDFLoader,
    ".md":   TextLoader,
    ".txt":  TextLoader,
    ".docx": Docx2txtLoader,
    ".html": UnstructuredHTMLLoader,
    ".htm":  UnstructuredHTMLLoader,
}


def _splitter() -> RecursiveCharacterTextSplitter:
    return RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
        separators=["\n\n", "\n", ". ", "! ", "? ", " ", ""],
    )


def load_documents(docs_dir: str | None = None) -> list[Document]:
    """
    Walk the docs directory, load every supported file, and return chunks.

    Parameters
    ----------
    docs_dir : str | None
        Override the default path from settings (useful in tests).

    Returns
    -------
    list[Document]  — chunked, metadata-enriched documents.
    """
    base = Path(docs_dir or settings.docs_dir)

    if not base.exists():
        logger.warning("Docs directory '%s' does not exist. Skipping.", base)
        return []

    splitter = _splitter()
    all_chunks: list[Document] = []
    files_processed = 0

    for root, _, files in os.walk(base):
        for fname in files:
            fpath = Path(root) / fname
            suffix = fpath.suffix.lower()

            if suffix not in _SUPPORTED:
                logger.debug("Skipping unsupported file: %s", fpath)
                continue

            loader_cls = _SUPPORTED[suffix]
            try:
                # TextLoader needs explicit encoding args
                if suffix in (".md", ".txt"):
                    loader = loader_cls(str(fpath), encoding="utf-8", autodetect_encoding=True)
                else:
                    loader = loader_cls(str(fpath))

                raw_docs = loader.load()

                for doc in raw_docs:
                    doc.metadata.update(
                        {
                            "source":    str(fpath.relative_to(base)),
                            "file_type": suffix.lstrip("."),
                            "loader":    loader_cls.__name__,
                        }
                    )

                chunks = splitter.split_documents(raw_docs)
                all_chunks.extend(chunks)
                files_processed += 1
                logger.info("Loaded '%s' → %d chunks", fpath.name, len(chunks))

            except Exception as exc:  # noqa: BLE001
                logger.error("Failed to load '%s': %s", fpath, exc)

    logger.info(
        "Doc loader done: %d files → %d total chunks", files_processed, len(all_chunks)
    )
    return all_chunks
