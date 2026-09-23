# Salamm — Backlog

## Done
- Release blockers fixed: signing via gitignored `keystore.properties`, R8 enabled (`optimization { enable = true }`), `finishAffinity()` replaces `exitProcess(0)`, Home error state (`HomeUiState.error` + render) with MockK tests.
- Cleanup: dead code removed (`useGoldenTitle`, duplicate import, template colors), `ShareUtil` DRY extraction, PascalCase util files, `AppModule` without `!!`.
- Structure: `ReaderScreen` split — `ui/reader/components/PageContent.kt` (cursor-based renderer over `Page.order`) + `ShinyBlackThumb.kt`.

## Decisions
- `HomeScreen.kt` (~301 lines) assessed: kept as one file — drawer + top bar + content form one cohesive screen; extraction would be churn without payoff.
- `WhatsAppUtil` kept: actively used (HomeScreen chat entry point) — audit claim of dead code was stale.

## Remaining
- [x] ViewModel tests: `ReaderViewModel`, `SearchViewModel` (MockK + `StandardTestDispatcher` + `runTest`, per AGENTS.md). Tests caught a real bug: `ReaderUiState(error=…)` kept `isLoading=true`, so Reader error screens never rendered — fixed in `ReaderViewModel`.
- [x] Final gates: `./gradlew assembleDebug testDebugUnitTest assembleRelease` — BUILD SUCCESSFUL.

## Backlog ideas (unprioritized)
- `SearchViewModel.loadContent` swallows exceptions silently — consider surfacing an error state like Home/Reader.
- `SearchUiState` has no `error` field yet (see above).
