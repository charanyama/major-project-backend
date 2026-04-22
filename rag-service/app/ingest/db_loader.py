"""
app/ingest/db_loader.py
───────────────────────
Reads rows from your ecommerce database and converts them to Documents.

How it works
────────────
1. Connect via SQLAlchemy (supports PostgreSQL, MySQL, SQLite, etc.).
2. For each table in settings.db_tables, SELECT all rows.
3. Each row is serialised to "key: value" text.
4. Wrapped in a LangChain Document with table + row metadata.
5. Split into chunks.

Tip: customise `_row_to_text` for specific tables if you want richer
     text representations (e.g. more readable product summaries).
"""

from __future__ import annotations

import logging
from typing import Any

from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from sqlalchemy import create_engine, inspect, text

from app.config import settings

logger = logging.getLogger(__name__)


def _splitter() -> RecursiveCharacterTextSplitter:
    return RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
    )


def _row_to_text(table: str, row: dict[str, Any]) -> str:
    """
    Convert a DB row to a human-readable string for embedding.

    Customise per-table for better semantics, e.g.:
        if table == "products":
            return (
                f"Product: {row['name']}  |  SKU: {row['sku']}  |  "
                f"Price: ${row['price']}  |  Category: {row['category']}"
            )
    """
    lines = [f"Table: {table}"]
    for key, value in row.items():
        if value is not None:
            lines.append(f"  {key}: {value}")
    return "\n".join(lines)


def load_db_documents() -> list[Document]:
    """
    Load all rows from configured DB tables as chunked LangChain Documents.

    Returns
    -------
    list[Document]
    """
    if not settings.db_url:
        logger.warning("DB_URL is not set. Skipping database loader.")
        return []

    if not settings.db_tables:
        logger.warning("DB_TABLES is not set. Skipping database loader.")
        return []

    try:
        engine = create_engine(settings.db_url, pool_pre_ping=True)
    except Exception as exc:
        logger.error("Failed to create DB engine: %s", exc)
        return []

    splitter = _splitter()
    all_docs: list[Document] = []

    # Check which tables actually exist before querying
    inspector = inspect(engine)
    existing_tables = set(inspector.get_table_names())

    with engine.connect() as conn:
        for table in settings.db_tables:
            if table not in existing_tables:
                logger.warning("Table '%s' not found in DB. Skipping.", table)
                continue

            try:
                result = conn.execute(text(f"SELECT * FROM {table}"))  # noqa: S608
                rows = [dict(zip(result.keys(), row)) for row in result.fetchall()]

                if not rows:
                    logger.info("Table '%s' is empty.", table)
                    continue

                raw_docs = [
                    Document(
                        page_content=_row_to_text(table, row),
                        metadata={
                            "source":  f"db/{table}",
                            "table":   table,
                            "loader":  "SQLAlchemyLoader",
                            "row_id":  str(row.get("id", i)),
                        },
                    )
                    for i, row in enumerate(rows)
                ]

                chunks = splitter.split_documents(raw_docs)
                all_docs.extend(chunks)
                logger.info(
                    "DB table '%s': %d rows → %d chunks", table, len(rows), len(chunks)
                )

            except Exception as exc:  # noqa: BLE001
                logger.error("Error loading table '%s': %s", table, exc)

    logger.info("DB loader done: %d total chunks", len(all_docs))
    return all_docs
