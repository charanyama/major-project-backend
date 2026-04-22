"""
app/rag/prompt.py
─────────────────
System prompt and RAG chain prompt templates.

Design rules
────────────
1. The LLM must ONLY use information from the retrieved context.
2. It must NEVER fabricate data (prices, policies, stock levels, etc.).
3. If the context doesn't contain the answer, it says so clearly.
4. Responses are concise and customer-friendly.
"""

from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder

# ── System message ─────────────────────────────────────────────────────────────

SYSTEM_PROMPT = """\
You are a helpful and knowledgeable AI assistant for our ecommerce platform.

STRICT RULES:
1. Answer ONLY using information from the <context> block below.
2. If the context does not contain enough information to answer the question,
   respond exactly: "I don't have enough information to answer that. Please
   contact our support team for assistance."
3. NEVER invent prices, product names, policies, stock levels, order statuses,
   or any other facts.
4. Keep answers concise, accurate, and customer-friendly.
5. When referencing specific data (e.g. a price or policy), mention the source.

<context>
{context}
</context>
"""

# ── Full RAG chat prompt ───────────────────────────────────────────────────────

RAG_PROMPT = ChatPromptTemplate.from_messages(
    [
        ("system", SYSTEM_PROMPT),
        MessagesPlaceholder(variable_name="chat_history", optional=True),
        ("human", "{question}"),
    ]
)


def format_docs(docs) -> str:
    """
    Serialise retrieved LangChain Documents into the context string
    injected into the system prompt.
    """
    if not docs:
        return "No relevant context found."
    parts = []
    for i, doc in enumerate(docs, 1):
        source = doc.metadata.get("source", "unknown")
        parts.append(f"[{i}] Source: {source}\n{doc.page_content.strip()}")
    return "\n\n---\n\n".join(parts)
