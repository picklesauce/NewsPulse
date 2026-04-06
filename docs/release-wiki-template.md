# Wiki copy-paste: Version 1.0.0 Release

Use this as the body for a GitLab Wiki page named **Version 1.0.0 Release** (adjust the title if your `versionName` differs). Replace bracketed placeholders before publishing.

---

## Release date

**[DD-Mon-YYYY]** — date the release APK/installer was produced.

## Version

**1.0.0** (see `VERSION` / `versionName` in `app/build.gradle.kts`).

## Summary

Short description of what this release contains for end users.

## Major changes

- [Change or feature 1]
- [Change or feature 2]
- [Change or feature 3]

## Issues

Link to your GitLab issue list or milestone:

- [Open issues / closed for this release](https://git.uwaterloo.ca/a44shah/team-101-17/-/issues)

## Installers

Attach the following to this Wiki page (or link to GitLab Release assets):

| Artifact | Description |
|----------|-------------|
| **Android APK** | `app-release.apk` (or signed variant) — side-load on emulator or device (API 34+). |

### For graders / course staff

- **Fast path:** Install the attached APK; the app should run without a local build.
- **Full API features:** If the APK was built with keys in CI or locally, live news and Supabase features should work as tested. If you need to **build from source** with the same configuration the team used, copy the contents of **`local.properties`** from the private section your team provides (Wiki-only or secure channel), or use **`local.properties.example`** plus keys supplied by the team.

Do **not** paste long-lived service-role secrets in public wikis; use **anon** keys and read-only API keys only, per your instructor’s policy.

## Release in GitLab

Create the release under **Deploy → Release** so the repository is tagged and archived as required by the course.

---

After publishing, add a **Releases** link in `README.md` pointing to this Wiki page.
