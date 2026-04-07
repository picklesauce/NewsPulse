# Getting started

## Prerequisites

- [Android Studio](https://developer.android.com/studio) (Koala or newer recommended) or IntelliJ with the Android plugin
- JDK 11+ (Android Studio bundles a suitable JDK)
- An Android **emulator** (API 34+) or a physical device with USB debugging

## Clone and open

1. Clone the repository.
2. Open the project root in Android Studio and let Gradle sync finish.

## Configuration (`local.properties`)

Secrets and machine-specific settings live in **`local.properties`** at the project root. That file is **gitignored** and must not be committed.

1. Copy the template:

   ```bash
   cp local.properties.example local.properties
   ```

2. Ensure Android Studio has set `sdk.dir` (or add it manually if needed).

3. Add API keys your team uses for development or grading:

   | Key | Purpose |
   |-----|---------|
   | `NEWSAPI_AI_KEY` | Live news from Event Registry. If empty, the app uses **mock** articles so you can still run and test UI. |
   | `SUPABASE_URL`, `SUPABASE_ANON_KEY` | Supabase-backed sign-in and synced preferences. Optional for local testing depending on your build. |
   | `GEMINI_API_KEY` | Optional: smarter “related articles” ranking. |

For the **final course release**, the team should either:

- Attach a **release APK** built with the needed keys (so installers do not need to build), and/or  
- Publish **grader-only** configuration (e.g. on the GitLab Wiki release page) so a TA can copy values into `local.properties` and build with full features.

## Build and run

- **Debug run:** use the Run button in Android Studio, or:

  ```bash
  ./gradlew installDebug
  ```

- **Release APK** (for submission / side-load):

  ```bash
  ./gradlew assembleRelease
  ```

  Output: `app/build/outputs/apk/release/app-release.apk` (unsigned unless you configure signing; for course side-loading, align with your instructor’s expectations).

## Tests

```bash
./gradlew testDebugUnitTest
```

## Troubleshooting

- **Gradle cannot find SDK:** Open the project in Android Studio once so it writes `sdk.dir` into `local.properties`, or set `ANDROID_HOME` and point `sdk.dir` at your SDK path.
- **No live articles:** `NEWSAPI_AI_KEY` is missing or invalid - expected behavior is mock data; confirm the key in `local.properties`.
- **Supabase errors:** Check URL and anon key; anon keys are not as sensitive as service keys but should still only be shared through agreed channels (e.g. team wiki for graders).
