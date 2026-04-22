from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import model_validator
from functools import lru_cache
from typing import Optional
import os


def get_env_file() -> str:
    """
    Decide which env file to load BEFORE settings initialization.
    """
    env = os.getenv("ENVIRONMENT", "dev").lower()
    return ".env.local" if env == "dev" else ".env.prod"


class Settings(BaseSettings):
    # Core environment selector
    ENVIRONMENT: str = "dev"

    # External services
    PINECONE_API_KEY: str
    PINECONE_INDEX_NAME: Optional[str] = None

    MONGODB_URI: str = "mongodb://localhost:27017"
    DATABASE_NAME: Optional[str] = None
    SUPABASE_URI: Optional[str] = None

    # ML config
    EMBEDDING_MODEL: Optional[str] = None  # 768-D embeddings

    # Strong separation: only ONE env file is loaded
    model_config = SettingsConfigDict(
        env_file=get_env_file(),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @model_validator(mode="after")
    def set_dynamic_defaults(self) -> "Settings":
        is_dev = self.ENVIRONMENT == "dev"

        if not self.PINECONE_INDEX_NAME:
            self.PINECONE_INDEX_NAME = (
                "seed-products" if is_dev else "products"
            )

        if not self.DATABASE_NAME:
            self.DATABASE_NAME = (
                "seedProducts" if is_dev else "store"
            )
        
        if not self.EMBEDDING_MODEL:
            self.EMBEDDING_MODEL = (
                # "sentence-transformers/all-MiniLM-L6-v2" if is_dev else "all-mpnet-base-v2"
                "all-mpnet-base-v2"
            )

        return self


@lru_cache()
def get_settings() -> Settings:
    return Settings()