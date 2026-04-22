import sys
import os
import asyncio
import uuid
import random
from typing import List

# Fix python path so the `app` module can be imported properly
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from faker import Faker
from app.config import get_settings
from app.models import Product, User, UserInteraction
from motor.motor_asyncio import AsyncIOMotorClient
from pinecone import Pinecone
import certifi

fake = Faker()

EVENT_WEIGHTS = {
    'scroll': 0.5,
    'page_view': 1.0,
    'click': 1.5,
    'review': 3.0,
    'add_to_cart': 5.0,
    'order_placed': 10.0
}

SEED_DATABASE = "seedProducts"
PINECONE_SEED_INDEX_NAME="seed-products"

async def seed():
    settings = get_settings()
    print("Initiating Local Database Connections...")
    db_client = AsyncIOMotorClient(settings.MONGODB_URI, serverSelectionTimeoutMS=5000, tls=True, tlsCAFile=certifi.where())
    db = db_client[SEED_DATABASE]
    
    print("Connecting to Pinecone...")
    pinecone_client = None
    pinecone_index = None
    try:
        pinecone_client = Pinecone(api_key=settings.PINECONE_API_KEY)
        pinecone_index = pinecone_client.Index(PINECONE_SEED_INDEX_NAME)
        print("Connected to Pinecone successfully.")
    except Exception as e:
        print(f"Failed to connect to pinecone. Vector uploading will be skipped. Ensure PINECONE_API_KEY is correct. Error: {e}")

    num_users = 10
    num_products = 200
    num_interactions = 200

    print("\n--- Generating Artificial Seed Data ---")
    
    print("Loading embedding model...")
    from sentence_transformers import SentenceTransformer
    try:
        embedding_model = SentenceTransformer(settings.EMBEDDING_MODEL)
    except Exception as e:
        print(f"Failed to load embedding model: {e}")
        embedding_model = None

    # 1. Create Products
    print(f"Creating {num_products} products...")
    created_products_ids = []
    categories = ["Laptop", "Phone", "Camera", "Headphones"]
    brands = ["Apple", "Samsung", "Sony", "Logitech"]
    
    for _ in range(num_products):
        p_id = str(uuid.uuid4())
        p = Product(
            _id=p_id,
            name=fake.catch_phrase(),
            category=random.choice(categories),
            price=round(random.uniform(10.0, 2000.0), 2),
            img_url=fake.image_url(),
            description=fake.text(),
            brand=random.choice(brands),
            sku=fake.uuid4()[:8]
        )
        created_products_ids.append(p_id)
        
        # Insert to Mongo
        await db.products.insert_one(p.model_dump(by_alias=True))
        
        # Insert to Pinecone
        if pinecone_index:
            try:
                if embedding_model:
                    text_to_embed = f"{p.name} {p.brand} {p.category} {p.description}"
                    vector = embedding_model.encode(text_to_embed).tolist()
                else:
                    # Fallback to random if model fails to load
                    vector = [random.uniform(-1, 1) for _ in range(768)]
                    
                pinecone_index.upsert(vectors=[{
                    "id": p_id,
                    "values": vector,
                    # We store metadata allowing filtering capabilities matching the hybrid endpoints
                    "metadata": {"brand": p.brand, "category": p.category, "price": float(p.price)}
                }])
            except Exception as e:
                print(f"Pinecone Upsert Error: {e}")
                
    # 2. Create Users
    print(f"Creating {num_users} users...")
    created_users_ids = []
    for _ in range(num_users):
        u = User(
            full_name=fake.name(),
            email=fake.email(),
            phone=fake.phone_number()
        )
        res = await db.users.insert_one(u.model_dump(by_alias=True, exclude={"id"}))
        created_users_ids.append(str(res.inserted_id))

    # 3. Create Interactions
    print(f"Creating {num_interactions} recommendation interaction events...")
    event_types = list(EVENT_WEIGHTS.keys())
    for _ in range(num_interactions):
        event_type = random.choice(event_types)
        weight = EVENT_WEIGHTS[event_type]
        interaction = UserInteraction(
            user_id=random.choice(created_users_ids),
            product_id=random.choice(created_products_ids),
            event_type=event_type,
            score_weight=weight
        )
        await db.interactions.insert_one(interaction.model_dump(by_alias=True, exclude={"id"}))
        
    print("\n✅ Database Seeding Fully Detailed and Completed Successfully.")

if __name__ == "__main__":
    asyncio.run(seed())
