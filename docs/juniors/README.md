# `docs/juniros/` — Q&A Index

> **Not sure where to look?** Start here. Each question points to **one** canonical doc.

## Core questions

| I want to understand… | Read this |
|---|---|
| What is this project, how do I open it, what's the file layout, and what's the first file I should read? | [`00-getting-started.md`](./00-getting-started.md) |
| What Kotlin features does the codebase use (`val`, `data class`, `object`, `StateFlow`, coroutines, lambdas)? | [`01-kotlin-concepts.md`](./01-kotlin-concepts.md) |
| I'm coming from Flutter. How do Flutter concepts map to Jetpack Compose? | [`02-flutter-to-compose.md`](./02-flutter-to-compose.md) |
| How does MVVM work here? What's the big picture? | [`03-architecture-overview.md`](./03-architecture-overview.md) |
| How does a ViewModel hold state? Why `StateFlow`? What's the private-mutable / public-readonly split? | [`04-viewmodel-deep-dive.md`](./04-viewmodel-deep-dive.md) |
| How is the UI built? What are the screens, the theme, and how do they consume ViewModel state? | [`05-ui-layer.md`](./05-ui-layer.md) |
| Where does the data come from? JSON, `KhatiraRepository`, models? | [`06-data-layer.md`](./06-data-layer.md) |
| How does navigation work? What is `AppModule` and why no Hilt? | [`07-navigation-and-di.md`](./07-navigation-and-di.md) |
| What's the current state of the codebase? Known issues? | [`08-project-audit.md`](./08-project-audit.md) |
| How do I write a local JVM unit test (no emulator)? | [`09-unit-testing-guide.md`](./09-unit-testing-guide.md) |

## Canonical homes for re-explained topics

| Topic | Canonical home |
|---|---|
| `StateFlow` / `MutableStateFlow` / `asStateFlow()` | `04-viewmodel-deep-dive.md` (Deep Dive: MutableStateFlow vs StateFlow) |
| Manual `AppModule` DI (singleton vs fresh, no-Hilt rationale) | `07-navigation-and-di.md` (Dependency Injection: AppModule) |
| Routes + NavHost + navArgument + single-Activity | `07-navigation-and-di.md` (Navigation: Routes) |
| `AppColors` / golden theme | `05-ui-layer.md` (Theme) |
| Build / install / test commands | `00-getting-started.md` (Building and Running) |
| Project directory structure | `00-getting-started.md` (Project Directory Structure) |

## Adding a new doc

1. Decide which of the 10 files (00–09) is its canonical home.
2. If you must add a new file, number it `10-…` and add a row to the **Core questions** table above.
3. If you find yourself restating a topic that already has a canonical home, replace your restatement with a one-line cross-reference link.

> **Note:** Kotlin language concepts (`01`) and Flutter→Compose mapping (`02`) share identical content with the [Athkarix project docs](https://github.com/athkarix/athkarix-android/tree/main/docs/juniros). Differences are called out inline where they exist.
