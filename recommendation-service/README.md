# seeding guide and database details

## Required Databases

To run the Recommendation Microservice successfully, you need the following databases configured in your `.env` file:

### 1. MongoDB (Relational & Interaction Data)
**Purpose:** Stores user profiles, product metadata, and granular user interactions (events).
**Variables Needed:**
- `MONGODB_URI`: The connection string for your MongoDB instance (e.g., `mongodb://localhost:27017` or a MongoDB Atlas URI).
- `DATABASE_NAME`: The name of the database to use (defaults to `ecommerce_db`).

**Collections Used:**
- `users`: Stores instances of the `User` Pydantic model.
- `products`: Stores the raw product metadata corresponding to the vectors.
- `interactions`: Stores time-series behavioral data of users clicking, viewing, or purchasing products.

### 2. Pinecone (Vector Database)
**Purpose:** Stores the 768-D vector embeddings of items to facilitate semantic search and content-based recommendation.
**Variables Needed:**
- `PINECONE_API_KEY`: Found in your Pinecone dashboard.
- `PINECONE_INDEX_NAME`: The target index for your product vectors (defaults to `products-index`).

**Index Details Setup Needed via Pinecone Cloud:**
- **Dimensions**: `768` (required by `all-mpnet-base-v2` SentenceTransformer).
- **Metric**: `cosine` (standard for semantic textual similarity).

---

## Seeding Data

You can seed the databases with artificial data for testing. The seeding script will:
1. Create 50 fake products and push them to MongoDB.
2. Push random 768-dimensional embeddings for those products into Pinecone along with metadata (brand, category, price) for filtering.
3. Automatically generate 10 fake users and register 200 randomly weighted interaction events (e.g., clicks, cart additions, orders).

### Running the Seed Script
Make sure your `.env` is fully set up, then run the python script from the root of the project:

```bash
# Make sure you are in the python virtual environment where requirements are installed
python scripts/seed_data.py
```
