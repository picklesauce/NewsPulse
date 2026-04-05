# NewsPulse (Team-101-17)

## Description

**NewsPulse** is an Android app that provides people with tailored news articles and world headlines, to keep them updated on world events, regardless of their interests and lifestyle. NewsPulse surfaces articles by topic and interest, with reading history, saved articles, and makes use of a beautiful layered UI

### About the team

We are a group of CS students who are the absolute the best at building great projects. 

### The team members

* Baldeep Pannu
* Aadit Shah
* Ayaan Sarfraz
* Hanson Liu

### Contact

* baldeep.pannu@uwaterloo.ca
* aadit.shah@uwaterloo.ca
* a3sarfraz@uwaterloo.ca
* h239liu@uwaterloo.ca

### Main window

![NewsPulse main screen](docs/screenshots/main-window.png)


### Demo video

* [Project demo on YouTube](https://www.youtube.com/) 

### Acknowledgements

* [NewsAPI.ai / Event Registry](https://newsapi.ai/) for article data (API key via `local.properties`; see **Getting Started**).
* Course staff and TAs for feedback and CS346 structure.
* Jetpack Compose, Kotlin, and Android Open Source Project libraries used in this app.

### Releases

* [GitLab Releases](-/releases) — tagged builds and release notes for grading and deployment.

---

## 2. Project Information

* [Team Contract](../../wikis/Team-Contract)
* [Project Proposal](../../wikis/Project-Proposal)
* [Meeting Minutes](../../wikis/Team-Meetings) — updated log for the full term
* [Developer Logs](../../wikis/Developer-Logs) — updated log for the full term 
* [Team Reflections](../../wikis/Team-Reflections)

---

## 3. User Guide

### A brief overview

**Sign-in:** From the login screen, use **Google**, or **email/password**; **Sign Up** opens registration, then you go to **Home**.

**Tabs (bottom navigation):** **Home** — choose interests/categories, scroll the feed, tap an article to read, **Save Offline**, and see **related articles**. **Discover** — trending categories; open one for articles or **follow** it (top right); article view works like Home. **Saved Articles** — offline-saved pieces. **Profile** — edit profile and interests; **reading history**.

More detail, walkthroughs, and edge cases: **[Usage Guide (wiki)](../../wikis/Usage-Guide)**.

---

## 4. Design Documents

* [UML / ERD (Mermaid)](../../wikis/NewsPulse-UML-Diagram) — diagram source for the data model.
* [Class diagrams](../../wikis/Class-Diagrams) — high-level app and layer diagrams 

**Architecture (summary)**

* **ui/** — Views (Compose screens, ViewModels, theme). Renders data and handles user input. Depends only on domain interfaces.
* **domain/** — Business objects (e.g. Article, LoginState) and interfaces for data access. No Android or Compose dependencies. Defines the contracts that the data layer implements.
* **data/** — Implementations that fetch or persist data (e.g. SharedPreferences wrappers, API clients). Uses domain interfaces. The `data.mock` package holds mock implementations for testing.

---

## 5. Grading Instructions

**For the TA**

* **Build:** `./gradlew assembleDebug` should succeed; run the app on an emulator or device as above.
* **API key:** If `NEWSAPI_AI_KEY` is unset, the app still runs with mock data — note when grading live article features.
* **Tests:** `./gradlew testDebugUnitTest` — unit tests for selected ViewModels and domain utilities.
* **Optional:** See [docs/newsapifix.md](docs/newsapifix.md) for NewsAPI-related behavior notes if something behaves unexpectedly with the live API.

---

## Useful Links

* [Team Contract](../../wikis/Team-Contract)
* [Project Proposal](../../wikis/Project-Proposal)
* [Team Meetings](../../wikis/Team-Meetings)
* [Project repository](../../) _(GitLab project root)_
