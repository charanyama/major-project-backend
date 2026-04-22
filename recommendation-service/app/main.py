from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.database import connect_to_mongo, close_mongo_connection, setup_pinecone, setup_embedding_model
from app.routes import router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    await connect_to_mongo()
    setup_pinecone()
    setup_embedding_model()
    yield
    # Shutdown
    await close_mongo_connection()

app = FastAPI(
    title="Recommendation Microservice",
    description="E-Commerce AI Recommendation Engine with FastAPI, Motor, and Pinecone",
    version="1.0.0",
    lifespan=lifespan
)

app.include_router(router, prefix="/api/v1/recommendations")

@app.get("/health")
async def health_check():
    return {"status": "healthy"}


@app.get("/")
async def index():
    return {"message": "The server is up and running. Welcome to recommendation service.!"}
