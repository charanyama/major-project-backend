from fastapi import APIRouter, HTTPException, Depends
from typing import List
import random
import uuid

from app.schemas import SearchRequest, InteractionRequest, RecommendationResponse
from app.models import Product, User, UserInteraction
from app.services import (
    search_pinecone_hybrid, 
    fetch_products_by_ids, 
    get_collaborative_recommendations,
    get_related_products,
    rank_candidates,
    record_interaction,
    fake,
    EVENT_WEIGHTS,
    generate_embeddings
)
from app.database import db, pinecone_index

router = APIRouter()

@router.post("/search", response_model=RecommendationResponse)
async def semantic_search(request: SearchRequest):
    """Hybrid Contextual Search using Pinecone with Metadata Filtering"""
    candidate_ids = await search_pinecone_hybrid(request.query, request.filters, request.top_k)
    products = await fetch_products_by_ids(candidate_ids)
    return RecommendationResponse(products=products, strategy_used="hybrid_pinecone_search")

@router.get("/home/{user_id}", response_model=RecommendationResponse)
async def get_homepage_recommendations(user_id: str):
    """Personalized Homepage Mix"""
    candidate_ids = await get_collaborative_recommendations(user_id, top_k=20)
    ranked_ids = await rank_candidates(candidate_ids, user_id)
    # limit to 10
    final_ids = ranked_ids[:10]
    products = await fetch_products_by_ids(final_ids)
    return RecommendationResponse(products=products, strategy_used="collaborative_filtering")

@router.get("/related/{product_id}", response_model=RecommendationResponse)
async def get_related(product_id: str):
    """Pure Content Based using Pinecone specific ID feature"""
    candidate_ids = await get_related_products(product_id, top_k=5)
    products = await fetch_products_by_ids(candidate_ids)
    return RecommendationResponse(products=products, strategy_used="content_based_item_item")

@router.post("/interactions")
async def track_interaction(request: InteractionRequest):
    """Track User Action for Collaborative processing"""
    if request.event_type not in EVENT_WEIGHTS:
        raise HTTPException(status_code=400, detail="Invalid event_type")
    interaction_id = await record_interaction(request.user_id, request.product_id, request.event_type)
    return {"message": "Interaction recorded", "id": interaction_id}

@router.post("/seed")
async def seed_synthetic_data(num_users: int = 10, num_products: int = 50, num_interactions: int = 200):
    """Seed Data if DB is empty for demonstration."""
    # Create products
    created_products = []
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
        created_products.append(p)
        
        # Insert into mongo
        await db.products.insert_one(p.model_dump(by_alias=True))
        
        # Fake 768-D insert to Pinecone if working online
        if pinecone_index:
            try:
                vector = [random.uniform(-1, 1) for _ in range(768)]
                pinecone_index.upsert(vectors=[{
                    "id": p_id,
                    "values": vector,
                    "metadata": {"brand": p.brand, "category": p.category, "price": p.price}
                }])
            except Exception as e:
                print(f"Skipping pinecone upsert due to error: {e}")
                
    # Create users
    created_users = []
    for _ in range(num_users):
        u = User(
            full_name=fake.name(),
            email=fake.email(),
            phone=fake.phone_number()
        )
        res = await db.users.insert_one(u.model_dump(by_alias=True, exclude={"id"}))
        created_users.append(str(res.inserted_id))
        
    # Create interactions
    event_types = list(EVENT_WEIGHTS.keys())
    for _ in range(num_interactions):
        await record_interaction(
            user_id=random.choice(created_users),
            product_id=random.choice(created_products).id,
            event_type=random.choice(event_types)
        )
        
    return {"message": f"Seeded {num_users} users, {num_products} products, {num_interactions} interactions."}

@router.get("/")
def index():
    return "Welcome to the Recommendation Service"