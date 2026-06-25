# Release Report

**Project:** Salamm (Khatir) — Android Mobile App  
**Audited:** 2026-06-24  
**Score:** 34 / 100

## Verdict

**Ship: NO** — 5 blockers found, 2 of which are security-critical.

---

## Stage 1 — Project Summary

| Attribute | Value |
|-----------|-------|
| **Type** | Android mobile app (Jetpack Compose, Material 3) |
| **Language** | Kotlin 2.2.10 |
| **Build** | Gradle 9.4.1 with AGP 9.2.1 |
| **Architecture** | MVVM — manual DI via `AppModule`, no Hilt/Dagger |
| **Min SDK / Target** | 24 / 36 |
| **CI/CD** | None — no GitHub Actions, no pipelines |
| **Tests** | 1 unit test file (2 tests), no androidTest/UI tests |

Content is a ~849KB JSON asset (`khatira_content.json`, 10,033 lines) with 34 chapters, ~532 pages. No network calls — fully offline.

---

## Stage 2 — Architecture Audit

**Strengths:**
- Clean MVVM separation, manual DI, good state exposure pattern (backing `StateFlow` properties)
- Repository abstraction (`KhatiraRepository` interface) enables testability
- `order` field on `Page` correctly drives rendering sequence — not assumed alphabetical
- RTL support via `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`

**Issues:**

| Issue | Severity | Detail |
|-------|----------|--------|
| DRY violation — share intent building duplicated | LOW | `Intent.ACTION_SEND` pattern repeated in `HomeScreen.kt` and `ReaderScreen.kt` |
| `colors.xml` unused defaults | LOW | `purple_200/500/700`, `teal_200/700` from template — unused in Compose theme |
| Single-module project | LOW | Fine for current size, limits scalability |
| `Json` decoder recreated per call | LOW | Should be a companion singleton |

---

## Stage 3 — Production QA

| Scenario | Status | Notes |
|----------|--------|-------|
| Loading state | ✅ | All screens show `CircularProgressIndicator` |
| Error state (reader) | ✅ | Shows `error` text from `ReaderUiState` |
| Error state (home) | ❌ **HIGH** | `loadChapters()` catches exception but sets empty state — user sees blank screen with no message |
| Empty content (reader) | ✅ | Shows `AppStrings.noContent` |
| Malformed JSON | ⚠️ PARTIAL | `Json { ignoreUnknownKeys = true }` handles schema drift, but a truly broken file crashes |
| Back press exit dialog | ✅ | `BackPressHandlerWithExitDialog` shows confirmation |
| **`exitProcess(0)` on exit** | 🔴 **BLOCKER** | `alert_exit_dialog.kt:46` — kills JVM without lifecycle cleanup. Must use `finishAffinity()` or `Activity.finish()` |

---

## Stage 4 — Security Audit (OWASP MASVS)

| Issue | Severity | Detail |
|-------|----------|--------|
| **Hardcoded signing credentials in VCS** | 🔴 **BLOCKER** | `app/build.gradle.kts:22-27`: `storePassword = "salam123"`, `keyPassword = "salam123"` in plaintext. Committed to git. |
| **Signing keystore location** | 🔴 **BLOCKER** | `storeFile = file("salam-release.jks")` — hardcoded relative path. If this JKS is in the repo, it's exposed with a known password. |
| No network calls | ✅ N/A | App is fully offline (local JSON asset) — no TLS, CORS, or network attack surface |
| No PII collection | ✅ | No user data stored |
| Backup rules are empty templates | ⚠️ MEDIUM | `backup_rules.xml` and `data_extraction_rules.xml` are Android Studio defaults — no explicit include/exclude. |

---

## Stage 5 — Performance Audit

| Issue | Severity | Detail |
|-------|----------|--------|
| **R8 minification disabled in release** | 🔴 **BLOCKER** | `app/build.gradle.kts:33-35`: `optimization { enable = false }` — release APK ships without shrink/obfuscate. APK ~double the necessary size, code reversible |
| JSON loaded entirely in memory | ⚠️ MEDIUM | 849KB JSON fully parsed into RAM on first access — acceptable for this volume, but no streaming |
| No `@Stable`/`@Immutable` annotations | LOW | Compose compiler 2.2 can infer most stability, but explicit annotations could improve recomposition |
| Image assets | LOW | Background images (`bg_home`, `bg_reader`) may add to APK size — verify they're compressed |

---

## Stage 6 — UX Review

| Issue | Severity | Detail |
|-------|----------|--------|
| Icons missing `contentDescription` | MEDIUM | `HomeScreen.kt:99,221,269`, `SearchScreen.kt:170` — icons have `null` content description. Affects TalkBack |
| `useGoldenTitle` variable is dead code | LOW | `ReaderScreen.kt:78` — computed but the ternary at line 128 always uses `AppColors.golden` anyway |
| Search doesn't navigate to specific page | MEDIUM | Search results navigate to chapter start, not the specific matched page |
| No dark theme vector icons | LOW | Adaptive icons present, but no dark-theme-aware launcher icon variants |
| Arabic RTL ✅ | GOOD | `LocalLayoutDirection provides LayoutDirection.Rtl` — correct for Arabic content |

---

## Stage 7 — Distribution Readiness

| Issue | Severity | Detail |
|-------|----------|--------|
| **compileSdk = 37 does not exist** | 🔴 **BLOCKER** | API 37 doesn't exist (API 36 = Android 16 is latest). Build will fail with SDK not found |
| No CI/CD pipeline | HIGH | No automation for build, test, or deploy |
| Minimal test coverage (2 tests) | HIGH | No ViewModel tests, no UI tests — regressions impossible to catch pre-release |
| `versionCode = 1`, `versionName = "1.0"` | INFO | Fine for initial release, but need versioning strategy |
| No privacy policy | INFO | Required for Google Play Store data safety form |
| No screenshots/previews | INFO | Need app store screenshots |
| No app signing strategy documented | INFO | Where is the JKS file? How is it secured outside VCS? |

---

## Blockers (must fix)

- [ ] **`app/build.gradle.kts:22-27`** — Hardcoded signing credentials in VCS. Move to `keystore.properties` (gitignored) or CI secrets.
- [ ] **`app/build.gradle.kts:24,26`** — Keystore (`salam-release.jks`) committed to VCS. Remove from git.
- [ ] **`app/build.gradle.kts:9`** — `compileSdk = 37` does not exist. Set to `36`.
- [ ] **`app/build.gradle.kts:33-35`** — R8 optimization disabled in release. Enable with `isMinifyEnabled = true` and `isShrinkResources = true`. Write proper keep rules.
- [ ] **`util/alert_exit_dialog.kt:46`** — `exitProcess(0)`. Replace with activity `finishAffinity()` or `finish()`.

## Required (before release)

- [ ] Add ViewModel unit tests (HomeViewModel, ReaderViewModel, SearchViewModel)
- [ ] Add CI/CD pipeline (GitHub Actions: `assembleDebug` + `testDebugUnitTest`)
- [ ] Fix HomeViewModel: show error state when loading fails instead of blank screen
- [ ] Add `contentDescription` on icon-only composables for accessibility
- [ ] Configure backup rules properly or disable backup if not needed
- [ ] Remove unused colors from `colors.xml`
- [ ] Implement proper versioning strategy
- [ ] Prepare Google Play listing: privacy policy, screenshots, data safety form

## Optional (fast-follow)

- [ ] Compress background images to webp to reduce APK size
- [ ] Search navigation to specific page within chapter
- [ ] `@Stable` annotations on data classes for Compose optimization
- [ ] Modularize into `:core`, `:data`, `:ui` modules
- [ ] Add `network_security_config.xml` (low priority — no network calls)
- [ ] Change `Json` instance to a companion singleton
- [ ] Remove unused `useGoldenTitle` dead code in `ReaderScreen.kt:78`
- [ ] Add `@SerialName` annotations to Page fields for consistency

## Recommended Commands

```bash
# Build (will fail on compileSdk 37 — fix first)
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Check for kotlin warnings
./gradlew lint

# After fixes, build release
./gradlew assembleRelease
```
