# 🧠 NewsPulse – Interest & Article Retrieval Redesign Plan

## 🚨 Problem Summary

Current issues in the app:

- Users select interests → no or irrelevant articles shown
- Some categories (e.g. Technology) overload results, others show nothing
- Interest system is too rigid (hardcoded options)
- API rate limit (1000/day) restricts scalability
- Empty feeds break user trust

👉 Core issue:
> We are not reliably converting user interests into relevant articles.

---

# 🎯 Goal

Build a system that:

- Reliably fetches relevant articles for any user interest
- Handles weak or empty results gracefully
- Minimizes API usage via caching
- Supports dynamic, user-defined interests

---

# 🧩 1. Interest → Article Matching (Core Fix)

## ❌ Current Problem
All interests are treated the same:

interest → API call → results (often bad or empty)


## ✅ New Approach

interest → normalize → strategy by type → fetch → score → fallback


## 🧠 Interest Types & Strategies

### 🌍 Country
Example: Canada

- Primary: top headlines by country
- Fallback: `"Canada news"`

---

### 👤 Person
Example: Donald Trump

- Primary: `"Donald Trump"`
- Fallback: looser keyword match

---

### 🏢 Company
Example: Tesla

- Primary: `"Tesla"`
- Fallback: `"Tesla stock"`, `"Tesla EV"`

---

### 🧠 Topic
Example: Technology

Use predefined keyword clusters:

- Technology → AI, startups, software, gadgets
- Business → markets, economy, finance
- Sports → leagues, matches, teams

---

## 🏗 Required Component

Create:

InterestQueryResolver / ArticleRetrievalService


Responsible for:
- selecting strategy
- executing query
- scoring results
- applying fallback

---

# 🔍 2. Dynamic Interest Selection

## ❌ Current
- Hardcoded interests

## ✅ New Flow

User can type anything:
- "Canada"
- "Tesla"
- "AI"
- "NBA"

### Flow:
1. User inputs interest
2. Check if exists in DB
3. If not → create new interest
4. Assign type (Country, Person, Company, Topic)

---

## 🧠 Type Assignment

### Option A (Recommended for now)
User manually selects type

### Option B (Future)
Auto-detect type based on:
- known country list
- previous entries
- heuristics

---

## ⚠️ Important Rule

Not all interests will always return articles.

UI should communicate:
> "We try to find relevant articles. If coverage is limited, we show related content."

---

# ⚖️ 3. Handling No or Weak Results

## Result Quality Levels

### ✅ Strong
- Many relevant articles → show normally

### ⚠️ Weak
- Few results → supplement with related articles

### ❌ None
- No results → trigger fallback

---

## 🛠 Fallback Strategy

### 1. Cached Articles
- Show older articles
- Label: "Recent coverage"

---

### 2. Related Articles
Example:
- Interest: OpenAI
- Show: AI, Microsoft, ChatGPT

---

### 3. Broaden Search
Example:
- Mark Carney → fallback to "Canada politics"

---

### 4. User Guidance
- Suggest broader interests

---

## 🚨 Rule

Never show an empty feed.

### Fallback Order:
1. Fresh direct articles
2. Cached direct articles
3. Fresh related articles
4. Cached related articles
5. Suggest new interests

---

# 💾 4. Caching Strategy (CRITICAL)

## 🎯 Goal
Reduce API usage and improve performance

---

## 🧩 A. Query Cache

Store results of API queries

### Key:

type + interest value


Example:
- `topic:technology`
- `company:tesla`

### Store:
- articles
- fetchedAt
- expiresAt
- result quality

---

## 📰 B. Article Cache

Store normalized articles separately

### Fields:
- id
- title
- description
- source
- publishedAt
- url
- imageUrl

---

## ⏱ TTL Strategy

- Breaking news/topics → 15–30 min
- Company/person → 30–60 min
- Weak/no results → retry every few hours

---

## ❗ Negative Caching

If no results:
- cache empty result
- avoid repeated API calls

---

# 🧱 System Architecture


Android App
↓
Backend (Supabase / API layer)
↓
Cache (Supabase DB)
↓
News API


---

# 🔄 Feed Generation Flow


User Interests
↓
Interest Resolver
↓
Search Strategy
↓
Cache Check
↓
API Fetch (if needed)
↓
Normalize + Store
↓
Score Results
↓
Fallback Handling
↓
Merge + Rank Feed


---

# 🚀 Implementation Plan

## Phase 1: Fix Retrieval Logic
- Separate strategies by interest type
- Add fallback logic
- Add result scoring

---

## Phase 2: Redesign Interests
- Allow user input
- Require type selection
- Store dynamic interests

---

## Phase 3: Add Caching
- Query cache
- Article table
- TTL
- Negative caching

---

## Phase 4: Feed Assembly
- Merge results across interests
- Rank articles
- Ensure no empty feed

---

# 🧾 Suggested Tickets

### 1
Refactor interest model to support dynamic user-created interests and explicit types

### 2
Implement per-type article retrieval strategies

### 3
Build result scoring system (strong / weak / none)

### 4
Implement fallback search logic

### 5
Redesign interest selection UI (search + type)

### 6
Add query-level caching

### 7
Store normalized articles for reuse

### 8
Implement negative caching

### 9
Ensure non-empty feed with fallback logic

### 10
Merge and rank multi-interest feeds

---

# 🧠 Key Takeaway

The problem is not the API.

👉 The problem is:
> Weak interest-to-article mapping logic

Fix that first.

Then caching will naturally solve the rate limit issue.

---