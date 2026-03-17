# NewsPulse Database Schema

This document describes the database schema for the NewsPulse application. The schema is designed to support article management, user preferences, reading history, and interest-based content filtering.

## Overview

The database consists of 8 main tables:
- **app_users** - Simple application-managed login credentials (email/password)
- **interests** - Catalog of available interests (Countries, People, Companies, Topics)
- **articles** - News articles and content
- **article_interests** - Many-to-many relationship between articles and interests
- **user_profiles** - User account information and preferences
- **reading_history** - Tracks articles users have read
- **saved_articles** - Articles users have bookmarked/saved
- **followed_interests** - Interests that users follow

## Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────────┐         ┌─────────────┐
│  interests  │◄────────┤ article_interests ├────────►│  articles   │
│             │         │  (junction table) │         │             │
└──────┬──────┘         └──────────────────┘         └──────┬──────┘
       │                                                     │
       │                                                     │
       │                                                     │
       │                                                     │
┌──────▼──────────┐                              ┌─────────▼──────────┐
│followed_interests│                              │ reading_history    │
└──────┬──────────┘                              └─────────┬──────────┘
       │                                                    │
       │                                                    │
       │                                                    │
       │                                                    │
       │         ┌──────────────────┐                       │
       └────────►│  user_profiles  │◄──────────────────────┘
                 └────────┬─────────┘
                          │
                          │
                 ┌────────▼─────────┐
                 │ saved_articles   │
                 └────────┬─────────┘
                          │
                          │
                 ┌────────▼─────────┐
                 │    app_users     │
                 └──────────────────┘
```

## Tables

### 1. `interests`

Catalog of all available interests that can be associated with articles and followed by users.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | TEXT | PRIMARY KEY | Unique identifier (e.g., "interest-technology") |
| `type` | TEXT | NOT NULL, CHECK | One of: 'Country', 'Person', 'Company', 'Topic' |
| `name` | TEXT | NOT NULL, UNIQUE | Display name (e.g., "Technology", "Elon Musk") |
| `created_at` | TIMESTAMP | DEFAULT NOW() | When the interest was added |

**Indexes:**
- `idx_interests_type` - For filtering by interest type
- `idx_interests_name` - For name lookups

**Relationships:**
- Referenced by `article_interests` (many-to-many with articles)
- Referenced by `followed_interests` (many-to-many with users)

---

### 2. `articles`

Stores all news articles and their metadata.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | TEXT | PRIMARY KEY | Unique article identifier (e.g., "art-1") |
| `title` | TEXT | NOT NULL | Article headline |
| `source` | TEXT | NOT NULL | News source/publication name |
| `url` | TEXT | DEFAULT '' | Article URL |
| `published_at` | BIGINT | NOT NULL | Publication timestamp in milliseconds |
| `summary` | TEXT | DEFAULT '' | Article summary/description |
| `image_url` | TEXT | DEFAULT '' | URL to article image |
| `created_at` | TIMESTAMP | DEFAULT NOW() | When record was created |
| `updated_at` | TIMESTAMP | DEFAULT NOW() | When record was last updated |

**Indexes:**
- `idx_articles_published_at` - For sorting by publication date (DESC)
- `idx_articles_source` - For filtering by source

**Relationships:**
- Has many `article_interests` (many-to-many with interests)
- Referenced by `reading_history`
- Referenced by `saved_articles`

---

### 3. `article_interests`

Junction table for the many-to-many relationship between articles and interests.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `article_id` | TEXT | PRIMARY KEY, FK → articles(id) | References articles table |
| `interest_id` | TEXT | PRIMARY KEY, FK → interests(id) | References interests table |

**Composite Primary Key:** `(article_id, interest_id)`

**Indexes:**
- `idx_article_interests_article` - For finding all interests for an article
- `idx_article_interests_interest` - For finding all articles with an interest

**Cascade Behavior:** ON DELETE CASCADE - Deleting an article or interest removes related junction records

---

### 4. `app_users`

Simple application-managed account table used for login credentials.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Stable user identifier used by app data tables |
| `email` | TEXT | NOT NULL, UNIQUE | Login email |
| `password` | TEXT | NOT NULL | Login password (stored as plain text in current sprint implementation) |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Account creation timestamp |

**Indexes:**
- Unique index on `email`

**Relationships:**
- Referenced by `user_profiles.user_id` (one-to-one)

---

### 5. `user_profiles`

User account information and preferences.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | UUID | PRIMARY KEY, FK → app_users(id) | Unique user identifier from app_users |
| `username` | TEXT | NOT NULL | User's display name |
| `member_since` | TEXT | NOT NULL | Join date in format "MMM yyyy" (e.g., "Feb 2026") |
| `onboarding_complete` | BOOLEAN | DEFAULT FALSE | Whether user completed onboarding |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Account creation timestamp |
| `updated_at` | TIMESTAMP | DEFAULT NOW() | Last profile update timestamp |

**Indexes:**
- `idx_user_profiles_username` - For username lookups

**Relationships:**
- Referenced by `reading_history`
- Referenced by `saved_articles`
- Referenced by `followed_interests`

---

### 6. `reading_history`

Tracks which articles users have read, with timestamps.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique record identifier |
| `user_id` | UUID | NOT NULL, FK → user_profiles(user_id) | References user_profiles table |
| `article_id` | TEXT | NOT NULL, FK → articles(id) | References articles table |
| `title` | TEXT | NOT NULL | Article title (denormalized for quick access) |
| `read_at_millis` | BIGINT | NOT NULL | Timestamp when article was read (milliseconds) |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Record creation timestamp |

**Indexes:**
- `idx_reading_history_user` - For finding user's reading history
- `idx_reading_history_user_read_at` - For sorting by read time (DESC)
- `idx_reading_history_article` - For finding all users who read an article

**Cascade Behavior:** ON DELETE CASCADE - Deleting a user removes their reading history

---

### 7. `saved_articles`

Articles that users have bookmarked or saved for later.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique record identifier |
| `user_id` | UUID | NOT NULL, FK → user_profiles(user_id) | References user_profiles table |
| `article_id` | TEXT | NOT NULL, FK → articles(id) | References articles table |
| `saved_at` | TIMESTAMP | DEFAULT NOW() | When article was saved |

**Unique Constraint:** `(user_id, article_id)` - Prevents duplicate saves

**Indexes:**
- `idx_saved_articles_user` - For finding user's saved articles
- `idx_saved_articles_user_saved_at` - For sorting by save time (DESC)
- `idx_saved_articles_article` - For finding all users who saved an article

**Cascade Behavior:** ON DELETE CASCADE - Deleting a user removes their saved articles

---

### 8. `followed_interests`

Tracks which interests each user follows.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PRIMARY KEY | Unique record identifier |
| `user_id` | UUID | NOT NULL, FK → user_profiles(user_id) | References user_profiles table |
| `interest_id` | TEXT | NOT NULL, FK → interests(id) | References interests table |
| `followed_at` | TIMESTAMP | DEFAULT NOW() | When interest was followed |

**Unique Constraint:** `(user_id, interest_id)` - Prevents duplicate follows

**Indexes:**
- `idx_followed_interests_user` - For finding user's followed interests
- `idx_followed_interests_interest` - For finding all users following an interest

**Cascade Behavior:** ON DELETE CASCADE - Deleting a user removes their followed interests

---

## Key Relationships Summary

1. **App Users ↔ User Profile**: One-to-one via `user_profiles.user_id`
   - Each app user has one profile row

2. **Articles ↔ Interests**: Many-to-many via `article_interests`
   - An article can have multiple interests
   - An interest can be associated with multiple articles

3. **Users ↔ Articles**: 
   - One-to-many via `reading_history` (users read many articles)
   - One-to-many via `saved_articles` (users save many articles)

4. **Users ↔ Interests**: Many-to-many via `followed_interests`
   - A user can follow multiple interests
   - An interest can be followed by multiple users

## Data Types Mapping

| Kotlin/App Type | Database Type | Notes |
|----------------|---------------|-------|
| `String` (email) | TEXT | Used in `app_users.email` |
| `String` (password) | TEXT | Used in `app_users.password` |
| `String` (id) | TEXT | Article and Interest IDs |
| `UUID` (user id) | UUID | Used in `app_users.id` and user-linked tables |
| `String` (content) | TEXT | Titles, summaries, URLs |
| `Long` (timestamp) | BIGINT | Published timestamps in milliseconds |
| `List<Interest>` | Junction table | Stored in `article_interests` |
| `Set<String>` (IDs) | Junction table | Stored in `followed_interests` |
| `Boolean` | BOOLEAN | Flags like `onboarding_complete` |

## Common Queries

### Get all interests for an article
```sql
SELECT i.* FROM interests i
JOIN article_interests ai ON i.id = ai.interest_id
WHERE ai.article_id = 'art-1';
```

### Get all articles for a user's followed interests
```sql
SELECT DISTINCT a.* FROM articles a
JOIN article_interests ai ON a.id = ai.article_id
JOIN followed_interests fi ON ai.interest_id = fi.interest_id
WHERE fi.user_id = 'user-uuid-here';
```

### Get user's reading history (most recent first)
```sql
SELECT a.*, rh.read_at_millis FROM articles a
JOIN reading_history rh ON a.id = rh.article_id
WHERE rh.user_id = 'user-uuid-here'
ORDER BY rh.read_at_millis DESC;
```

## Notes

- All foreign keys use `ON DELETE CASCADE` to maintain referential integrity
- Timestamps use `TIMESTAMP WITH TIME ZONE` for proper timezone handling
- UUIDs are generated using `uuid_generate_v4()` function
- Current implementation uses `app_users` for simple email/password login persistence
- Passwords are plain text in current sprint implementation (acceptable for learning/demo; not recommended for production)
- If migrating to Supabase Auth later, `user_profiles.user_id` can instead link to `auth.users.id`
- Denormalized fields (like `reading_history.title`) are included for performance but can be removed if you prefer to always join with `articles`

