# Architecture Guidelines

## MVVM Structure (Kotlin Android)

```
android/app/src/main/java/com/cardsnap/
├── data/            # Room DB, DAO, Repositories
├── domain/          # Models, OCR, Parser
├── ui/              # Screens, Navigation, Theme
├── util/            # Utilities
└── MainActivity.kt
```

## Dependency Rules

- `ui` → `domain`, `data` (via ViewModel)
- `domain` → NO external dependencies
- `data` → `domain` (models)
- ViewModels mediate between UI and data layers

## Naming Conventions

- Screens: `*Screen.kt`
- ViewModels: `*ViewModel.kt`
- Repositories: `*Repository.kt`
- DAOs: `*Dao.kt`
- Models: `*Model.kt` or data classes
- Utils: `*Util.kt` or `*Helper.kt`

## Code Size Limits

- **Max 200 lines per file** (warn at 200, error at 300)
- **Max 30 lines per function** (warn at 30, error at 50)
- **Complexity ≤ 8** (warn), ≤ 10 (error)
- **Max nesting depth ≤ 4**
- **Max 3 parameters per function**

If code exceeds these limits, REFACTOR immediately using Extract Method, Extract Class, or other patterns.

## Forbidden Patterns

- NO direct Room/DB access from UI layer
- NO business logic in Composables
- NO circular dependencies between modules
- All external deps go through repositories

## Before Generating Code

1. Identify which layer the code belongs to
2. Check dependency rules
3. Keep composables focused on UI only
4. Move business logic to ViewModels
5. Keep files under 200 lines, functions under 30 lines
6. Run: `./gradlew lint` before commit
