# NewsPulse Database Schema

```mermaid
erDiagram

    INTERESTS {
        TEXT id PK
        TEXT type
        TEXT name
        TIMESTAMP created_at
    }

    ARTICLES {
        TEXT id PK
        TEXT title
        TEXT source
        TEXT url
        BIGINT published_at
        TEXT summary
        TEXT image_url
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ARTICLE_INTERESTS {
        TEXT article_id PK, FK
        TEXT interest_id PK, FK
    }

    USER_PROFILES {
        UUID user_id PK
        TEXT username
        TEXT member_since
        BOOLEAN onboarding_complete
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    READING_HISTORY {
        UUID id PK
        UUID user_id FK
        TEXT article_id FK
        TEXT title
        BIGINT read_at_millis
        TIMESTAMP created_at
    }

    SAVED_ARTICLES {
        UUID id PK
        UUID user_id FK
        TEXT article_id FK
        TIMESTAMP saved_at
    }

    FOLLOWED_INTERESTS {
        UUID id PK
        UUID user_id FK
        TEXT interest_id FK
        TIMESTAMP followed_at
    }

    ARTICLES ||--o{ ARTICLE_INTERESTS : categorizes
    INTERESTS ||--o{ ARTICLE_INTERESTS : tagged_with

    USER_PROFILES ||--o{ READING_HISTORY : reads
    ARTICLES ||--o{ READING_HISTORY : viewed

    USER_PROFILES ||--o{ SAVED_ARTICLES : saves
    ARTICLES ||--o{ SAVED_ARTICLES : saved

    USER_PROFILES ||--o{ FOLLOWED_INTERESTS : follows
    INTERESTS ||--o{ FOLLOWED_INTERESTS : followed  
    