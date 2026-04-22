"""
app/rag/pipeline.py
───────────────────
Core RAG chain built with LangChain LCEL (LangChain Expression Language).

Architecture
────────────
                 ┌──────────────────────────────────────────────┐
  question  ───▶ │  Pinecone Retriever (MMR, top-k chunks)       │
                 └───────────────┬──────────────────────────────┘
                                 │  retrieved Document objects
                                 ▼
                 ┌──────────────────────────────────────────────┐
                 │  Prompt  (system + context + history + q)     │
                 └───────────────┬──────────────────────────────┘
                                 │  formatted ChatPromptValue
                                 ▼
                 ┌──────────────────────────────────────────────┐
                 │  Groq LLM  (ChatGroq, temperature=0.1)        │
                 └───────────────┬──────────────────────────────┘
                                 │  AIMessage
                                 ▼
                           answer (str) + source docs
"""

from __future__ import annotations

import logging
from typing import Any

from langchain_core.documents import Document
from langchain_core.messages import AIMessage, HumanMessage
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnableLambda, RunnableParallel, RunnablePassthrough
from langchain_groq import ChatGroq

from app.config import settings
from app.rag.prompt import RAG_PROMPT, format_docs
from app.rag.retriever import vector_store

logger = logging.getLogger(__name__)


# ── LLM factory ───────────────────────────────────────────────────────────────

def _build_llm() -> ChatGroq:
    return ChatGroq(
        api_key=settings.groq_api_key,
        model=settings.groq_model,
        temperature=0.1,        # low temp = factual, grounded answers
        max_tokens=1024,
        streaming=False,
    )


# ── History conversion ────────────────────────────────────────────────────────

def _convert_history(raw: list[dict[str, str]]) -> list[HumanMessage | AIMessage]:
    """
    Convert [{"role": "human"|"ai", "content": "..."}] → LangChain messages.
    """
    messages: list[HumanMessage | AIMessage] = []
    for turn in raw:
        role = turn.get("role", "human").lower()
        content = turn.get("content", "")
        if role in ("human", "user"):
            messages.append(HumanMessage(content=content))
        else:
            messages.append(AIMessage(content=content))
    return messages


# ── Pipeline class ────────────────────────────────────────────────────────────

class RAGPipeline:
    """
    Main interface used by the FastAPI route handlers.

    Public methods
    --------------
    ask(question, conversation_history) → dict
    reload()                            → None   (call after re-ingestion)
    """

    def __init__(self) -> None:
        self._llm = _build_llm()
        self._retriever = vector_store.as_retriever()
        self._chain = self._build_chain()

    # ── Chain construction ─────────────────────────────────────────────────────

    def _build_chain(self):
        """
        LCEL chain:
          input ──▶ parallel(retrieve+format, passthrough) ──▶ prompt ──▶ LLM ──▶ str
        """
        retrieve_and_format = RunnableParallel(
            {
                # formatted string injected into system prompt
                "context":      self._retriever | RunnableLambda(format_docs),
                # raw docs returned as sources in the API response
                "raw_docs":     self._retriever,
                "question":     RunnablePassthrough(),
                "chat_history": RunnableLambda(lambda x: x.get("chat_history", [])),
            }
        )

        chain = (
            retrieve_and_format
            | RunnableLambda(self._format_prompt_input)
            | RAG_PROMPT
            | self._llm
            | StrOutputParser()
        )
        return chain

    @staticmethod
    def _format_prompt_input(data: dict) -> dict:
        """Map retriever output keys → prompt template variable names."""
        return {
            "context":      data["context"],
            "question":     data["question"]["question"],
            "chat_history": data["chat_history"],
        }

    # ── Public API ─────────────────────────────────────────────────────────────

    def ask(
        self,
        question: str,
        conversation_history: list[dict[str, str]] | None = None,
    ) -> dict[str, Any]:
        """
        Run the full RAG pipeline for a single question.

        Parameters
        ----------
        question : str
        conversation_history : list[dict]
            Optional prior turns for multi-turn conversations.

        Returns
        -------
        dict:
            "answer"  → str
            "sources" → list[Document]
            "model"   → str
        """
        history = _convert_history(conversation_history or [])

        # Retrieve docs separately so we can return them in the response
        retrieved_docs: list[Document] = self._retriever.invoke(question)

        answer: str = self._chain.invoke(
            {
                "question":     question,
                "chat_history": history,
            }
        )

        logger.info(
            "RAG answer generated | model=%s | sources=%d | q_len=%d",
            settings.groq_model,
            len(retrieved_docs),
            len(question),
        )

        return {
            "answer":  answer,
            "sources": retrieved_docs,
            "model":   settings.groq_model,
        }

    def reload(self) -> None:
        """Rebuild the retriever after re-ingestion so new docs are live."""
        self._retriever = vector_store.as_retriever()
        self._chain = self._build_chain()
        logger.info("RAG pipeline retriever reloaded.")


# Module-level singleton
rag_pipeline = RAGPipeline()
