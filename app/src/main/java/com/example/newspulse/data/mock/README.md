# Mock Repositories

Mock and fake implementations for demo/testing. All are in `data/mock/` to clearly separate from real implementations in `data/`.

## MockDB

Central test data. [MockDB.kt](MockDB.kt) provides deterministic articles and interests used by:

- **MockNewsRepository** – Returns `MockDB.articles` for the feed
- **MockInterestsCatalogRepository** – Returns `MockDB.interests` for the interests catalog

## Follow/Unfollow

**MockInterestsRepository** – In-memory store for which interests the user follows. Persists follow/unfollow state for the app session (sprint demo interactivity).

## Fake Repos (Previews)

Used by `createPreviewModel()` for Compose previews:

- **FakeNewsRepository** – Delegates to MockNewsRepository
- **FakeInterestsRepository** – Pre-seeded with MockDB interests; in-memory follow/unfollow
