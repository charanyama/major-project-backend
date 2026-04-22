# Ecommerce RAG Service

A Retrieval-Augmented Generation microservice that answers questions grounded
exclusively in your own data: local docs, ecommerce DB records, and live
responses from your sibling services.

## Stack

| Layer | Technology |
|---|---|
| API | FastAPI |
| LLM | Groq (`llama3-70b-8192` default) via `langchain-groq` |
| Embeddings | `sentence-transformers/all-MiniLM-L6-v2` (local, free) |
| Vector store | Pinecone (managed serverless) |
| Pipeline | LangChain LCEL |

## Project Structure

```
rag-service/
├── app/
│   ├── main.py               # FastAPI app & all routes
│   ├── config.py             # Settings (pydantic-settings, reads .env)
│   ├── rag/
│   │   ├── pipeline.py       # Core LCEL RAG chain (retriever → prompt → LLM)
│   │   ├── retriever.py      # Pinecone vector store manager
│   │   └── prompt.py         # System prompt + format_docs helper
│   ├── ingest/
│   │   ├── ingestor.py       # Orchestrator — runs all loaders
│   │   ├── doc_loader.py     # PDF / Markdown / TXT / DOCX loader
│   │   ├── db_loader.py      # SQLAlchemy DB records loader
│   │   └── api_loader.py     # HTTP loader for sibling microservices
│   └── models/
│       └── schemas.py        # Pydantic request / response schemas
├── docs/                     # ← Drop your PDF / .md / .docx files here
├── .env.example
├── requirements.txt
├── Dockerfile
└── docker-compose.yml
```

## Quick Start

### 1. Configure environment

```bash
cp .env.example .env
# Fill in: GROQ_API_KEY, PINECONE_API_KEY, PINECONE_ENVIRONMENT, DB_URL
```

### 2. Install dependencies

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
```

### 3. Drop your documents

Place any PDFs, Markdown files, Word docs, or plain text files in `./docs/`.

### 4. Index all sources (run once, then on updates)

```bash
# Index everything
python -m app.ingest.ingestor

# Index only files
python -m app.ingest.ingestor docs

# Force a full re-index (clears Pinecone namespace first)
python -m app.ingest.ingestor all --force
```

### 5. Start the service

```bash
uvicorn app.main:app --reload
# API docs: http://localhost:8000/docs
```

### Docker

```bash
docker-compose up --build
```

---

## API Reference

### `POST /ask`

Ask a question. Returns a grounded answer + the source chunks retrieved.

```json
// Request
{
  "question": "What is your return policy?",
  "conversation_history": []
}

// Response
{
  "answer": "Our return policy allows returns within 30 days...",
  "sources": [
    {
      "content": "Returns must be initiated within 30 days of delivery...",
      "source": "return-policy.pdf",
      "metadata": { "file_type": "pdf", "loader": "PyPDFLoader" }
    }
  ],
  "model": "llama3-70b-8192",
  "retriever_k": 6
}
```

### `POST /ingest`

Trigger (re)indexing.

```json
// Request
{ "source": "all", "force_reload": false }

// source options: "all" | "docs" | "database" | "apis"
```

### `GET /health`

```json
{
  "status": "ok",
  "vector_store": "pinecone",
  "index": "ecommerce-rag",
  "namespace": "ecommerce",
  "vector_count": 3842
}
```

---

## Adding a New Data Source

**New sibling service:** Add an entry to `SIBLING_APIS` in `.env`, then
`POST /ingest` with `{"source": "apis"}`.

**New DB table:** Add the table name to `DB_TABLES` in `.env`, then
`POST /ingest` with `{"source": "database"}`.

**New documents:** Drop files in `./docs/`, then
`POST /ingest` with `{"source": "docs"}`.

---

## Pinecone Index Setup Notes

- The index is created automatically on first startup if it doesn't exist.
- `PINECONE_DIMENSION` must match your embedding model (384 for `all-MiniLM-L6-v2`).
- `PINECONE_ENVIRONMENT` should match your Pinecone project's cloud + region
  (e.g. `us-east-1-aws`, `eu-west-1-aws`, `gcp-starter`).
- Data is isolated per `PINECONE_NAMESPACE` — set a unique namespace per
  environment (dev / staging / prod) to avoid cross-contamination.
