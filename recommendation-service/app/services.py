import uuid
import datetime
import random
from typing import List, Dict, Any, Optional
from faker import Faker

from app.models import Product, User, UserInteraction, Address
from app.schemas import SearchFilter
from app import database
from bson import ObjectId

fake = Faker()

EVENT_WEIGHTS = {
    'scroll': 0.5,
    'page_view': 1.0,
    'click': 1.5,
    'review': 3.0,
    'add_to_cart': 5.0,
    'order_placed': 10.0
}

async def generate_embeddings(text: str) -> List[float]:
    """Uses SentenceTransformers to encode text to 768-D vector"""
    model = database.embedding_model
    if not model:
        return [random.uniform(-1, 1) for _ in range(768)] # Fallback
    vector = model.encode(text)
    return vector.tolist()

async def search_pinecone_hybrid(query: str, filters: SearchFilter, top_k: int = 10) -> List[str]:
    """Convert natural search query to vector and search pinecone with metadata filters"""
    query_vector = await generate_embeddings(query)
    
    # Construct pinecone metadata filter
    pinecone_filter = {}
    if filters:
        if filters.brand:
            pinecone_filter["brand"] = {"$eq": filters.brand}
        if filters.category:
            pinecone_filter["category"] = {"$eq": filters.category}
        if filters.min_price is not None or filters.max_price is not None:
            pinecone_filter["price"] = {}
            if filters.min_price is not None:
                pinecone_filter["price"]["$gte"] = filters.min_price
            if filters.max_price is not None:
                pinecone_filter["price"]["$lte"] = filters.max_price
            if not pinecone_filter["price"]:
                del pinecone_filter["price"]

    try:
        response = database.pinecone_index.query(
            vector=query_vector,
            filter=pinecone_filter if pinecone_filter else None,
            top_k=top_k,
            include_metadata=False
        )
        return [match["id"] for match in response["matches"]]
    except Exception as e:
        print(f"Pinecone search error: {e}")
        return []

async def fetch_products_by_ids(product_ids: List[str]) -> List[Product]:
    products = await database.db.products.find({"_id": {"$in": product_ids}}).to_list(length=100)
    # maintain ordering if possible
    prod_dict = {str(p["_id"]): Product(**p) for p in products}
    return [prod_dict[pid] for pid in product_ids if pid in prod_dict]

async def get_collaborative_recommendations(user_id: str, top_k: int = 10) -> List[str]:
    """Find products interacting by finding users with similar interactions."""
    # Step 1: Find products this user has interacted with (views, carts, orders)
    user_interactions = await database.db.interactions.find({"user_id": user_id}).to_list(length=100)
    recent_product_ids = [str(i["product_id"]) for i in user_interactions]
    
    if not recent_product_ids:
        # Fallback: globally trending based on high-weight interactions
        pipeline = [
            {"$group": {"_id": "$product_id", "score": {"$sum": "$score_weight"}}},
            {"$sort": {"score": -1}},
            {"$limit": top_k}
        ]
        trending = await database.db.interactions.aggregate(pipeline).to_list(length=100)
        return [str(t["_id"]) for t in trending]
        
    # Step 2: Find other users who interacted with these products
    pipeline = [
        {"$match": {"product_id": {"$in": recent_product_ids}, "user_id": {"$ne": user_id}}},
        {"$group": {"_id": "$user_id", "overlap_score": {"$sum": "$score_weight"}}},
        {"$sort": {"overlap_score": -1}},
        {"$limit": 5} # top 5 similar users
    ]
    similar_users = await database.db.interactions.aggregate(pipeline).to_list(length=10)
    similar_user_ids = [u["_id"] for u in similar_users]
    
    if not similar_user_ids:
        return await get_collaborative_recommendations("GLOBAL_FALLBACK", top_k)
        
    # Step 3: Get top products from those similar users
    pipeline2 = [
        {"$match": {"user_id": {"$in": similar_user_ids}, "product_id": {"$nin": recent_product_ids}}},
        {"$group": {"_id": "$product_id", "rec_score": {"$sum": "$score_weight"}}},
        {"$sort": {"rec_score": -1}},
        {"$limit": top_k}
    ]
    collab_products = await database.db.interactions.aggregate(pipeline2).to_list(length=100)
    return [str(p["_id"]) for p in collab_products]

async def get_related_products(product_id: str, top_k: int = 5) -> List[str]:
    """Content-based via Pinecone."""
    # In real app, we fetch product vector directly, or Pinecone supports query by ID!
    try:
        response = database.pinecone_index.query(id=product_id, top_k=top_k+1, include_metadata=False)
        matches = [m["id"] for m in response["matches"] if m["id"] != product_id]
        return matches[:top_k]
    except Exception as e:
        print(f"Error finding related: {e}")
        return []

async def rank_candidates(candidate_ids: List[str], user_id: str) -> List[str]:
    """Re-ranking phase"""
    # 1. penalize already ordered ones
    ordered_interactions = await database.db.interactions.find({
        "user_id": user_id, 
        "event_type": "order_placed"
    }).to_list(length=100)
    ordered_ids = set([str(i["product_id"]) for i in ordered_interactions])
    
    # Reranking is simple: push ordered to the bottom
    ranked = [pid for pid in candidate_ids if pid not in ordered_ids]
    ranked.extend([pid for pid in candidate_ids if pid in ordered_ids])
    return ranked

async def record_interaction(user_id: str, product_id: str, event_type: str):
    weight = EVENT_WEIGHTS.get(event_type, 1.0)
    interaction = UserInteraction(
        user_id=user_id,
        product_id=product_id,
        event_type=event_type,
        score_weight=weight
    )
    result = await database.db.interactions.insert_one(interaction.model_dump(by_alias=True, exclude={"id"}))
    return str(result.inserted_id)
