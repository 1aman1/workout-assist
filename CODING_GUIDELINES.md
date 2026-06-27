# Android Coding Guidelines

These guidelines are mandatory for this project unless a documented exception is approved in code review.

## 0. Always keep design.md in sync with latest changes and append latest changes as versioning in design_version.md

## 1. Architecture and boundaries

- Use clear layering: UI -> ViewModel -> Use case (optional) -> Repository -> Data source.
- Keep business rules out of Composables and out of Activities/Fragments.
- Expose immutable UI state from ViewModel.
- Keep data models separated by responsibility:
  - Entity: storage shape.
  - Domain model: business shape.
  - UI state: render shape.

```kotlin
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val days: List<WorkoutDayUi> = emptyList(),
    val error: String? = null
)

class WorkoutViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState(isLoading = true))
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
}
```

## 2. Kotlin code style

- Follow Kotlin official style and keep functions small and focused.
- Prefer `val` over `var`.
- Never use `!!` unless there is no safe alternative and reasoning is documented.
- Use explicit names. Avoid abbreviations that are not obvious.
- Avoid god classes. Split by feature and responsibility.
- Keep extension functions near their domain and avoid dumping unrelated utilities.

```kotlin
// Avoid
fun upd(w: WorkoutDayModel) { /* ... */ }

// Prefer
fun updateWorkoutDay(day: WorkoutDayModel) { /* ... */ }
```

## 3. Compose UI rules

- State hoisting is default: keep state in ViewModel or parent composable.
- Composables should be mostly pure render functions.
- Use stable keys in lazy lists.
- Use `remember` only for UI-local ephemeral state.
- Use `LaunchedEffect` with correct keys to avoid duplicate side effects.
- Keep heavy calculations out of composition. Use `derivedStateOf` or precompute in ViewModel.
- Provide accessibility labels (`contentDescription`) for interactive icons.

```kotlin
LazyColumn {
    items(items = uiState.days, key = { it.dayNumber }) { day ->
        DayCard(day = day, onClick = { onDayClick(day.dayNumber) })
    }
}
```

## 4. ViewModel and state management

- Expose one `StateFlow<UiState>` for screen rendering.
- Represent loading, success, and error explicitly.
- One-off events (navigation, toast) should use event channels/flows, not state booleans that persist.
- Do not pass Android framework types into repository/domain layers.

## 5. Coroutines and threading

- Use `viewModelScope` for UI-triggered work.
- Use structured concurrency. Never use `GlobalScope`.
- Inject dispatchers when possible for testability.
- Repository APIs should be `suspend` for writes and `Flow` for streams.
- Handle cancellation correctly and avoid swallowing exceptions.

```kotlin
viewModelScope.launch {
    runCatching { repository.refreshPlan() }
        .onFailure { throwable ->
            _uiState.update { it.copy(error = throwable.message ?: "Unexpected error") }
        }
}
```

## 6. Data layer and Room

- Keep DAO focused on persistence operations only.
- Keep mapping logic in repository, not in UI.
- Room schema changes must include migration strategy.
- `fallbackToDestructiveMigration` is acceptable for debug prototypes, not for production releases.
- Keep transactions explicit for multi-step writes.

## 7. Error handling and logging

- Never silently ignore exceptions.
- Convert technical errors into user-meaningful UI messages.
- Log with enough context for debugging, but never log secrets.
- Keep logs structured and avoid noisy logs in release builds.

## 8. Testing standards

- Add/update tests for every non-trivial behavior change.
- Minimum expected coverage for changed logic:
  - ViewModel state transitions.
  - Repository mapping logic.
  - Critical DAO queries.
- Use fake repositories for ViewModel tests.
- Prefer deterministic tests (controlled dispatcher/time).

Suggested commands before opening PR:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

## 9. Performance and UX quality

- Avoid unnecessary recompositions.
- Keep list items lightweight.
- Use immutable UI models for predictable recomposition behavior.
- Avoid blocking main thread (disk/network/large parsing).
- Keep animations meaningful and short.

## 10. Security and privacy

- Do not hardcode API keys, tokens, or credentials.
- Use secure storage for sensitive values.
- Validate and sanitize all external input.
- Minimize collected user data and keep it local unless explicitly required.

## 11. Pull request checklist

- Scope is focused and minimal.
- No unrelated refactors mixed with feature changes.
- Code follows architecture and naming rules above.
- Tests updated/added for changed behavior.
- Lint/build pass locally.
- Docs updated when behavior or flow changes.
- If UI/flow changed, append a new version in `DESIGN_VERSIONS.md`.

## 12. Definition of done

A task is done only when all are true:

- Behavior works for normal and edge cases.
- Build passes.
- Relevant tests pass.
- No obvious performance regression.
- Documentation is current.
