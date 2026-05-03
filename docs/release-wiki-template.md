# Wiki copy-paste: Version 1.0.0 Release

Use this for the GitHub Wiki page **Version 1.0.0 Release**. The links below use this repository’s GitHub Release URL; change owner/repo if you fork or rename.

**Note:** Wikis are a poor place for large binaries. The APK is **not** attached to the wiki. Host the APK via **GitHub Release assets** (link) or another download URL, and describe that here.

---

## Release date

**06-Apr-2026** — date this installer/APK was produced (adjust if needed).

## Version

**1.0.0** (see `VERSION` / `versionName` in `app/build.gradle.kts`).

## Summary

NewsPulse is an Android app for personalized news: topics and interests, reading history, saved articles, and article detail views. This release is the course 1.0 deliverable: a known, testable build suitable for installation on an emulator or device.

## Major changes

- Personalized news feed and interest/topic flows  
- Reading history and saved articles  
- Account flows (local and/or Supabase-backed, per project configuration)  
- Live news and optional LLM-related features when API keys were present at build time  

## Issues

- [Project issues (GitHub)](https://github.com/picklesauce/NewsPulse/issues)

## Installers

The wiki **should not** host large APK files. The **Android APK** is provided as a **download link** below.

| Artifact | What it is |
|----------|------------|
| **Android APK** | Release build for side-loading. **Minimum API 34** — use an emulator or device that meets this. |

### Download the APK

- **Primary:** Open the **[GitHub Release v1.0.0](https://github.com/picklesauce/NewsPulse/releases/tag/v1.0.0)** page and use the **Assets** section (e.g. uploaded `.apk`, Google Drive, or other host) to download the `.apk` file.  
- The release APK was built with the team’s API configuration so it runs **without** a local `local.properties` on the install target.

### Install the APK (quick)

- **Emulator:** Create an **Android Virtual Device (API 34+)** in Android Studio. Drag the `.apk` onto the running emulator, or run `adb install path-to-your.apk`.  
- **Device:** Enable **Developer options** and **USB debugging**, connect via USB, then `adb install …`, or copy the file and open it (allow install from that source if prompted).

### Installer images (optional)

If required, add **screenshots** of install/run using the wiki **image** upload (small files only) or embed images from the repository.

## For graders / course staff

- **Recommended:** Download the APK from the **[GitHub Release v1.0.0](https://github.com/picklesauce/NewsPulse/releases/tag/v1.0.0)** assets — **no clone or build** required.  
- **Build from source:** Clone the repo, copy `local.properties.example` to `local.properties`, set `sdk.dir` and any keys your team provides. See `docs/getting-started.md` in the repository. Without a news API key, the app can still run with **mock** article data for UI grading.

## GitHub Release (tag + source archive)

This version is tagged as **`v1.0.0`**. The **[GitHub Release v1.0.0](https://github.com/picklesauce/NewsPulse/releases/tag/v1.0.0)** page provides **source code downloads** (zip/tar) for this tag, per course instructions.
