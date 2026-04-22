import asyncio
from motor.motor_asyncio import AsyncIOMotorClient
from pinecone import Pinecone
from sentence_transformers import SentenceTransformer
from app.config import get_settings
import certifi

settings = get_settings()

db_client = None
db = None
pinecone_client = None
pinecone_index = None
embedding_model = None
pg_conn = None

# ---------------- MongoDB ----------------
async def connect_to_mongo():
    global db_client, db
    try:
        db_client = AsyncIOMotorClient(
            settings.MONGODB_URI,
            serverSelectionTimeoutMS=5000,
            tls=True,
            tlsCAFile=certifi.where()
        )
        db = db_client[settings.DATABASE_NAME]

        # Force connection check
        await db.command("ping")

        print(f"Connected to MongoDB: {settings.DATABASE_NAME}")
    except Exception as e:
        print(f"MongoDB connection failed: {e}")
        db_client = None
        db = None


async def close_mongo_connection():
    global db_client
    if db_client:
        db_client.close()
        print("Closed MongoDB connection")


# ---------------- Pinecone ----------------
def setup_pinecone():
    global pinecone_client, pinecone_index
    try:
        pinecone_client = Pinecone(
            api_key=settings.PINECONE_API_KEY
        )

        pinecone_index = pinecone_client.Index(
            settings.PINECONE_INDEX_NAME
        )

        print(f"Connected to Pinecone index: {settings.PINECONE_INDEX_NAME}")
    except Exception as e:
        print(f"Pinecone connection failed: {e}")
        pinecone_client = None
        pinecone_index = None


# ---------------- Embedding Model ----------------
def setup_embedding_model():
    global embedding_model
    try:
        print(f"Loading embedding model: {settings.EMBEDDING_MODEL}")

        # Blocking operation — keep outside async context
        embedding_model = SentenceTransformer(
            settings.EMBEDDING_MODEL
        )

        print("Embedding model loaded.")
    except Exception as e:
        print(f"Embedding model load failed: {e}")
        embedding_model = None
