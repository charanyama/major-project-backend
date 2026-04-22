"""
app/config.py
─────────────
Centralised settings loaded from environment / .env file.
All modules import `settings` from here — never read os.environ directly.
"""

from __future__ import annotations

import json
from typing import Any

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    # ── LLM (Groq) ────────────────────────────────────────────────────────────
    groq_api_key: str = Field(..., alias="GROQ_API_KEY")
    groq_model: str = Field("llama3-70b-8192", alias="GROQ_MODEL")

    # ── Vector Store (Pinecone) ───────────────────────────────────────────────
    pinecone_api_key: str = Field(..., alias="PINECONE_API_KEY")
    pinecone_index_name: str = Field("ecommerce-rag", alias="PINECONE_INDEX_NAME")
    pinecone_environment: str = Field("us-east-1-aws", alias="PINECONE_ENVIRONMENT")
    pinecone_dimension: int = Field(384, alias="PINECONE_DIMENSION")
    pinecone_metric: str = Field("cosine", alias="PINECONE_METRIC")
    pinecone_namespace: str = Field("ecommerce", alias="PINECONE_NAMESPACE")

    # ── Document sources ──────────────────────────────────────────────────────
    docs_dir: str = Field("./docs", alias="DOCS_DIR")

    # ── Database ──────────────────────────────────────────────────────────────
    db_url: str | None = Field(None, alias="DB_URL")
    db_tables: list[str] = Field(default_factory=list, alias="DB_TABLES")

    @field_validator("db_tables", mode="before")
    @classmethod
    def parse_db_tables(cls, v: Any) -> list[str]:
        if isinstance(v, str):
            return [t.strip() for t in v.split(",") if t.strip()]
        return v or []

    # ── Sibling APIs ──────────────────────────────────────────────────────────
    sibling_apis: list[dict] = Field(default_factory=list, alias="SIBLING_APIS")

    @field_validator("sibling_apis", mode="before")
    @classmethod
    def parse_sibling_apis(cls, v: Any) -> list[dict]:
        if isinstance(v, str):
            try:
                return json.loads(v)
            except json.JSONDecodeError:
                return []
        return v or []

    # ── RAG tuning ────────────────────────────────────────────────────────────
    retriever_k: int = Field(6, alias="RETRIEVER_K")
    chunk_size: int = Field(800, alias="CHUNK_SIZE")
    chunk_overlap: int = Field(100, alias="CHUNK_OVERLAP")

    # ── App ───────────────────────────────────────────────────────────────────
    app_env: str = Field("development", alias="APP_ENV")
    log_level: str = Field("INFO", alias="LOG_LEVEL")


# Module-level singleton — import `settings` everywhere
settings = Settings()
