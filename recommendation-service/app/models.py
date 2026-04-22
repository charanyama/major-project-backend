from pydantic import BaseModel, EmailStr, Field, ConfigDict, BeforeValidator
from typing import List, Optional, Any, Annotated
from datetime import datetime

# A type for Pydantic V2 to handle ObjectId
PyObjectId = Annotated[str, BeforeValidator(str)]

class Address(BaseModel):
    street: str
    city: str
    state: str
    country: str
    zipcode: str

class User(BaseModel):
    id: Optional[PyObjectId] = Field(alias="_id", default=None)
    full_name: str
    email: EmailStr
    phone: str
    role: str = "USER"
    status: str = "ACTIVE"
    created_at: datetime = Field(default_factory=datetime.utcnow)
    addresses: List[Address] = []

    model_config = ConfigDict(populate_by_name=True)

class Product(BaseModel):
    id: Optional[str] = Field(alias="_id", default=None)
    name: str
    category: str
    subcategory: Optional[str] = None
    price: float
    img_url: str
    rating: float = 0.0
    avg_rating: float = 0.0
    total_ratings: int = 0
    description: str
    brand: str
    sku: str
    created_at: datetime = Field(default_factory=datetime.utcnow)

    model_config = ConfigDict(populate_by_name=True)

class UserInteraction(BaseModel):
    id: Optional[PyObjectId] = Field(alias="_id", default=None)
    user_id: str
    product_id: str
    event_type: str # 'click', 'page_view', 'scroll', 'add_to_cart', 'order_placed', 'review'
    score_weight: float
    timestamp: datetime = Field(default_factory=datetime.utcnow)

    model_config = ConfigDict(populate_by_name=True)
