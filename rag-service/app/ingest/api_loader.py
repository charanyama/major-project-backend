"""
app/ingest/api_loader.py
────────────────────────
Fetches JSON from your sibling ecommerce microservices and converts
each response into LangChain Documents for vector indexing.

Configure services in SIBLING_APIS (.env):
    [
      {"name": "inventory-service", "url": "http://inventory:8001/api/items",
       "headers": {"X-Internal-Token": "secret"}},
      ...
    ]

Each service's JSON response is expected to be either:
  - A list of objects  → one Document per object
  - A single object    → one Document
  - Any other JSON     → one Document (serialised)
"""

from __future__ import annotations

import json
import logging
from typing import Any

import httpx
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from app.config import settings

logger = logging.getLogger(__name__)

_HTTP_TIMEOUT = 15.0  # seconds


def _splitter() -> RecursiveCharacterTextSplitter:
    return RecursiveCharacterTextSplitter(
        chunk_size=settings.chunk_size,
        chunk_overlap=settings.chunk_overlap,
    )


def _item_to_text(service_name: str, item: Any) -> str:
    """Serialise a single JSON value to a human-readable embedding string."""
    if isinstance(item, dict):
        lines = [f"Service: {service_name}"]
        for k, v in item.items():
            if v is not None:
                lines.append(f"  {k}: {v}")
        return "\n".join(lines)
    return f"Service: {service_name}\n{json.dumps(item, indent=2, default=str)}"


def _fetch_service(client: httpx.Client, service: dict) -> list[Document]:
    """Fetch one service endpoint and return its raw (unsplit) Documents."""
    name = service.get("name", "unknown-service")
    url  = service.get("url", "")
    hdrs = service.get("headers", {})

    if not url:
        logger.warning("Service '%s' has no URL configured. Skipping.", name)
        return []

    try:
        resp = client.get(url, headers=hdrs, timeout=_HTTP_TIMEOUT)
        resp.raise_for_status()
        payload = resp.json()
    except httpx.HTTPStatusError as exc:
        logger.error(
            "HTTP %d from service '%s' (%s)", exc.response.status_code, name, url
        )
        return []
    except httpx.RequestError as exc:
        logger.error("Request error fetching service '%s': %s", name, exc)
        return []
    except json.JSONDecodeError as exc:
        logger.error("Non-JSON response from service '%s': %s", name, exc)
        return []

    items = payload if isinstance(payload, list) else [payload]

    return [
        Document(
            page_content=_item_to_text(name, item),
            metadata={
                "source":     f"api/{name}",
                "service":    name,
                "url":        url,
                "loader":     "APILoader",
                "item_index": str(i),
            },
        )
        for i, item in enumerate(items)
    ]


def load_api_documents() -> list[Document]:
    """
    Fetch all configured sibling services and return chunked Documents.

    Returns
    -------
    list[Document]
    """
    if not settings.sibling_apis:
        logger.warning("SIBLING_APIS not configured. Skipping API loader.")
        return []

    splitter  = _splitter()
    all_docs: list[Document] = []

    with httpx.Client(follow_redirects=True) as client:
        for service in settings.sibling_apis:
            raw_docs = _fetch_service(client, service)
            if not raw_docs:
                continue

            chunks = splitter.split_documents(raw_docs)
            all_docs.extend(chunks)
            logger.info(
                "API '%s': %d items → %d chunks",
                service.get("name"), len(raw_docs), len(chunks),
            )

    logger.info("API loader done: %d total chunks", len(all_docs))
    return all_docs
