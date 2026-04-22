from pydantic import BaseModel, Field
from typing import List, Optional

from app.models import Product

class SearchFilter(BaseModel):
    brand: Optional[str] = None
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    category: Optional[str] = None

class SearchRequest(BaseModel):
    query: str
    filters: Optional[SearchFilter] = None
    top_k: int = 10

class InteractionRequest(BaseModel):
    user_id: str
    product_id: str
    event_type: str
    
class RecommendationResponse(BaseModel):
    products: List[Product]
    strategy_used: str
