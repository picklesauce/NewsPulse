# NewsPulse (Team-101-17)

## Description

**NewsPulse** is an Android app that provides people with tailored news articles and world headlines, to keep them updated on world events, regardless of their interests and lifestyle. NewsPulse surfaces articles by topic and interest, with reading history, saved articles, and makes use of a beautiful layered UI

### About the team

We are a group of CS students who are the absolute best at building great projects. 

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

* [NewsPulse Demo](https://www.youtube.com/shorts/5KZaRocHm0k) 

### Acknowledgements

* [Acknowledgements](https://github.com/picklesauce/NewsPulse/wiki/Acknowledgements)

### Releases

* [Version 1.0.0 Release (wiki)](https://github.com/picklesauce/NewsPulse/wiki/Version-1.0.0-Release) — release date, changes, install instructions, grader notes.
* [GitHub Release v1.0.0](https://github.com/picklesauce/NewsPulse/releases/tag/v1.0.0) — tagged source archives + APK download link (release assets; large APKs are not hosted on the wiki).

---

## 2. Project Information

* [Team Contract](https://github.com/picklesauce/NewsPulse/wiki/Team-Contract)
* [Project Proposal](https://github.com/picklesauce/NewsPulse/wiki/Project-Proposal)
* [Meeting Minutes (Team Meetings)](https://github.com/picklesauce/NewsPulse/wiki/Team-Meetings)
* [Team Reflections](https://github.com/picklesauce/NewsPulse/wiki/Team-Reflections)

---

## 3. User Guide

* [Getting Started (repository)](docs/getting-started.md) — clone, `local.properties`, build, test; optional matching [Wiki](https://github.com/picklesauce/NewsPulse/wiki) page if you publish one.
* [Usage Guide](https://github.com/picklesauce/NewsPulse/wiki/Usage-Guide)

---

## 4. Design Documents

* [UML / ERD (Mermaid)](https://github.com/picklesauce/NewsPulse/wiki/NewsPulse-UML-Diagram)
* [Class diagrams](https://github.com/picklesauce/NewsPulse/wiki/Class-Diagrams)

---

## 5. Grading Instructions

**For the TA**

* **Build:** `./gradlew assembleDebug` should succeed; run the app on an emulator or device as above.
* **Secrets:** Copy `local.properties.example` to `local.properties` and add keys your team provides on the **[Version 1.0.0 Release](https://github.com/picklesauce/NewsPulse/wiki/Version-1.0.0-Release)** wiki page (or use the submitted APK built with those keys). Never commit `local.properties`.
* **API key:** If `NEWSAPI_AI_KEY` is unset, the app still runs with mock data — note when grading live article features.
* **Tests:** `./gradlew testDebugUnitTest` and `./gradlew test`— unit tests for selected ViewModels and domain utilities.


---
