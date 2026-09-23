# Salamm — Backlog

## Done
- Release blockers fixed: signing via gitignored `keystore.properties`, R8 enabled (`optimization { enable = true }`), `finishAffinity()` replaces `exitProcess(0)`, Home error state (`HomeUiState.error` + render) with MockK tests.
- Cleanup: dead code removed (`useGoldenTitle`, duplicate import, template colors), `ShareUtil` DRY extraction, PascalCase util files, `AppModule` without `!!`.
- Structure: `ReaderScreen` split — `ui/reader/components/PageContent.kt` (cursor-based renderer over `Page.order`) + `ShinyBlackThumb.kt`.

## Decisions
- `HomeScreen.kt` (~301 lines) assessed: kept as one file — drawer + top bar + content form one cohesive screen; extraction would be churn without payoff.
- `WhatsAppUtil` kept: actively used (HomeScreen chat entry point) — audit claim of dead code was stale.

## Remaining
- [ ] ViewModel tests: `ReaderViewModel`, `SearchViewModel` (MockK + `StandardTestDispatcher` + `runTest`, per AGENTS.md).
- [ ] Final gates: `./gradlew assembleDebug testDebugUnitTest assembleRelease`.
