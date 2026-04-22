# Recommendation System Architecture (E-Commerce Microservices)

## 1. Objective

Design a scalable, intelligent recommendation system that provides:

* **Related products (product page)** → fast, content-based
* **Personalized homepage recommendations** → behavior + AI-driven
* **High-quality ranking** → using user preferences + business logic

This system must integrate cleanly into an existing **microservices architecture** with event-driven communication.

---

## 2. Core Architecture Principle

The system follows a **multi-stage pipeline**:

### 1. Candidate Generation (Retrieval)

Generate a large pool of relevant products quickly.

### 2. Ranking (AI/Scoring)

Score products based on user preferences and behavior.

### 3. Re-ranking (Business Rules)

Apply constraints and final ordering.

---

## 3. High-Level Architecture

```
Client → API Gateway → Recommendation Service

Recommendation Service:
    ├── Candidate Generation Layer
    │     ├── Vector Search (Pinecone)
    │     ├── Popular Products
    │     ├── User Behavior Signals
    │
    ├── Feature Store (Redis / DB)
    │     └── User Profile & Preferences
    │
    ├── Ranking Layer (AI / Scoring)
    │
    ├── Re-ranking Layer (Business Rules)
    │
    └── Final Response
```

---

## 4. Data Flow (Event-Driven)

### 4.1 Events Produced

All user interactions must be captured:

```
UserViewedProductEvent
UserSearchedEvent
UserAddedToCartEvent
UserPurchasedEvent
```

### 4.2 Event Consumers

#### Recommendation Service

* Builds and updates **user profiles**
* Tracks preferences and behavior

#### Indexing Service

* Updates vector embeddings in Pinecone

---

## 5. User Profile & Preferences (Critical Layer)

This is the **core personalization engine**.

### Stored in: Redis / NoSQL

### Structure:

```
UserProfile:
  userId
  recent_views: [productIds]
  recent_searches: [keywords]
  cart_items: [productIds]
  purchased_items: [productIds]

  preferred_categories: {category: weight}
  preferred_brands: {brand: weight}
  price_range:
    min
    max

  interaction_scores:
    views_weight
    cart_weight
    purchase_weight

  user_embedding (optional)
```

### How Preferences Are Learned

* Views → weak signal
* Add to cart → medium signal
* Purchase → strong signal
* Search queries → intent signal

Weights are updated continuously via Kafka events.

---

## 6. Candidate Generation Layer

### Goal:

Generate **200–500 candidate products** from multiple sources.

---

### 6.1 Related Products (Product Page)

**Approach: Content-Based Filtering**

* Input: `productId`
* Query: Vector DB (Pinecone)
* Output: Similar products

✔ Fast
✔ Stateless
✔ No personalization required

---

### 6.2 Homepage Recommendations

Combine multiple strategies:

---

#### A. Popular Products

* Trending (last 24h / 7 days)
* High engagement

---

#### B. User Behavior-Based

From user profile:

* Recently viewed
* Recently searched
* Cart items

---

#### C. Content-Based Expansion

* Take user’s interacted products
* Fetch similar products via vector search

---

#### D. Preference-Based Filtering

Use learned preferences:

* Preferred categories
* Price range
* Brand affinity

---

### Final Candidate Pool

```
~200–500 products
(from all sources combined)
```

---

## 7. Ranking Layer (AI / Scoring)

### Goal:

Score each product based on **user + product + context**

---

### 7.1 Inputs

* User profile
* Product features
* Interaction history
* Context (time, recency)

---

### 7.2 Scoring Function (Phase 1)

```
score =
  w1 * similarity +
  w2 * popularity +
  w3 * recency +
  w4 * user_preference +
  w5 * interaction_strength
```

Where:

* **similarity** → vector similarity score
* **popularity** → global engagement
* **recency** → freshness
* **user_preference** → category/brand match
* **interaction_strength** → weighted actions

---

### 7.3 Advanced (Future)

Replace scoring with ML model:

* Gradient boosting (XGBoost / LightGBM)
* Neural ranking models

---

## 8. Re-ranking Layer (Final Optimization)

Apply business and UX constraints:

### Rules:

* Remove out-of-stock products
* Deduplicate products
* Ensure category diversity
* Avoid repetition of similar items
* Penalize already purchased products
* Boost:

  * Sponsored products
  * High-margin items

---

## 9. API Design

### 9.1 Homepage Recommendations

```
GET /recommendations/home/{userId}
```

Flow:

1. Fetch user profile
2. Generate candidates
3. Rank
4. Re-rank
5. Return top N

---

### 9.2 Related Products

```
GET /recommendations/product/{productId}
```

Flow:

1. Vector similarity search
2. Optional re-ranking
3. Return

---

## 10. Performance & Scalability

### 10.1 Caching

* Cache recommendations:

  ```
  key: userId
  ttl: 5–15 minutes
  ```

* Precompute for active users

---

### 10.2 Async Processing

* Kafka ensures:

  * Non-blocking updates
  * Real-time personalization

---

### 10.3 Storage Strategy

| Data Type    | Storage    |
| ------------ | ---------- |
| User profile | Redis      |
| Product data | Product DB |
| Embeddings   | Pinecone   |
| Events       | Kafka      |

---

## 11. Microservices Responsibilities

### Product Service

* Owns product data

### Indexing Service

* Generates embeddings
* Updates vector DB

### Recommendation Service

* Consumes events
* Builds user profiles
* Generates + ranks recommendations

### (Optional) Model Service

* Handles ML inference

---

## 12. Evolution Roadmap

### Phase 1

* Content-based + popularity
* Rule-based ranking

### Phase 2

* Add user behavior tracking

### Phase 3

* Strong personalization (preferences)

### Phase 4

* ML-based ranking

### Phase 5

* Real-time adaptive recommendations

---

## 13. Key Design Insight

This system is:

> A **hybrid recommendation engine**
> combining:
>
> * Content-based filtering
> * Behavioral analysis
> * User preference learning
> * AI ranking
> * Business-driven re-ranking

---

## 14. Summary

* **Related products** → direct vector similarity
* **Homepage** → multi-source candidate generation
* **Personalization** → user profile + preferences
* **AI layer** → ranking
* **Final control** → re-ranking

This ensures:

* Relevance
* Personalization
* Business alignment
* Scalability

---

## 15. What Makes This Strong

* Event-driven (real-time updates)
* Modular microservices
* Extensible (easy ML integration later)
* Production-ready architecture

---

This document can be used as a **reference blueprint** for implementing and scaling your recommendation system.
˝