# Day Insertion & Multi-Day Exercise Redesign

## Problem Statement
Current: Days are a fixed, append-only 1..N sequence; each exercise belongs to exactly one
day (`ExerciseEntity.dayNumber`, non-null).
Goal: Days can be inserted at any position in the cycle (pushing later days back by one);
a single exercise can be marked onto multiple days at once (e.g. "Treadmill" on every day);
an exercise with zero day assignments sits in an unassigned pool.

This replaces both earlier drafts (deleted): the "workouts as reusable units" draft and the
"single nullable dayNumber" draft were both wrong per the corrections below.

---

## Decisions Confirmed
- **Day insertion**: a new day can be inserted at any position in the cycle, not just
  appended. Inserting pushes every later day's number up by 1 (renumbering cascade).
- **Exercise assignment is many-to-many**: one exercise can be marked onto multiple days
  (e.g. create "Treadmill" once, mark it on all 7 days) - not "at most one day."
- **Pool on day deletion**: when a day is deleted, only the exercises *exclusively* on that
  day (no other day assignments) fall into the unassigned pool. An exercise also assigned
  to other days just loses that one assignment and stays assigned to the rest.
- (Carried over) Removing a day is a soft archive - history/sessions tied to it stay intact.
- (Carried over) Pool/assignment management is a new dedicated screen.

---

## Key Architecture Problem This Creates (and the fix)

`dayNumber` today is used as **both** a stable identity (FK target for exercises/sessions)
**and** a mutable display position (1, 2, 3...). Those two meanings must now be split:

- Once day insertion can renumber later days, a historical `WorkoutSessionEntity` row that
  says `dayNumber = 5` would silently start meaning a *different* day after an insert shifts
  everything after the insertion point - corrupting history without touching that row.
- Fix: give `TemplateDayEntity` a **stable surrogate id** (`id: Long`, autoincrement,
  never reused/renumbered) separate from **`position: Int`** (the mutable 1..N display
  order, freely renumbered on insert/delete). All new FKs (exercise↔day links, sessions)
  point at the stable `id`, never at `position`.
- `WorkoutSessionEntity` keeps its existing `dayNumber: Int` column, but it becomes a frozen
  historical label (the position at the time the session was logged, never rewritten) and
  gains a new nullable `dayId: Long?` as the live stable link. Due-day/cycle logic (next-due
  day, streak, etc.) must read `dayId`/`position`, not compare raw historical `dayNumber`s.

---

## Data Model Changes

### TemplateDayEntity - split identity from position

```kotlin
@Entity(tableName = "template_days")
data class TemplateDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,   // stable, never reused
    val position: Int,                                    // mutable 1..N display order
    val workoutName: String,
    val plannedDateEpochDay: Long,                        // derived from position, as before
    val completedForDateEpochDay: Long? = null,
    val isActive: Boolean = true                          // soft-archive instead of delete
)
```

### ExerciseEntity - drop the single dayNumber FK entirely

```kotlin
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val plannedRepsBySetJson: String = "",
    val plannedWeightBySetJson: String = "",
    val remarks: String = "",
    val isDone: Boolean = false
    // no dayNumber / position here - day membership + per-day order live in the join table
)
```

### NEW: ExerciseDayLinkEntity - many-to-many join table

```kotlin
@Entity(
    tableName = "exercise_day_links",
    primaryKeys = ["exerciseId", "dayId"],
    foreignKeys = [
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TemplateDayEntity::class, parentColumns = ["id"], childColumns = ["dayId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("dayId")]
)
data class ExerciseDayLinkEntity(
    val exerciseId: Long,
    val dayId: Long,
    val position: Int   // this exercise's order within THIS day's list (independent per day)
)
```
An exercise with **zero rows** in `exercise_day_links` is "in the pool" - no separate flag needed.

### WorkoutSessionEntity - add a stable link, keep the historical label

```kotlin
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long?,          // NEW: stable link to TemplateDayEntity.id (nullable: custom activities)
    val dayNumber: Int,        // KEEP: frozen historical position label, never rewritten
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val advancesCycle: Boolean = true,
    val isCustomActivity: Boolean = false
)
```
`SetLogEntity` is unchanged (keys off `sessionId` + `exerciseId`, unaffected by this).

---

## Repository API (new/changed)

```kotlin
// Day management (insert-anywhere, soft-archive)
suspend fun insertDay(atPosition: Int, workoutName: String): Long   // renumbers days at/after atPosition
suspend fun archiveDay(dayId: Long)                                 // isActive = false; drops its exercise links only

// Exercise pool / multi-day assignment
fun observeUnassignedExercises(): Flow<List<ExerciseModel>>         // exercises with zero links
suspend fun createExercise(draft: ExerciseDraft): Long              // starts unassigned
suspend fun assignExerciseToDay(exerciseId: Long, dayId: Long, position: Int)
suspend fun unassignExerciseFromDay(exerciseId: Long, dayId: Long)  // drops just this one link
suspend fun setExerciseDayAssignments(exerciseId: Long, dayIds: Set<Long>)  // bulk "mark on these days"

// Existing, reworked to go through the join table
fun observeDays(): Flow<List<WorkoutDayModel>>   // active days, each with its linked exercises in link.position order
```

---

## Migration (Room v6 -> v7)

This is a full rebuild of `template_days` and `exercises` (column removed/added, PK changes),
plus a new join table - straightforward `ALTER TABLE` is not sufficient for either:

1. `template_days`: rebuild with `id` (new autoincrement PK) + `position` (copy of the old
   `dayNumber`) + `isActive = 1` for all existing rows.
2. `exercises`: rebuild dropping `dayNumber`; for every existing row, also emit one
   `exercise_day_links` row `(exerciseId, dayId, position = old exercises.position)` resolving
   `dayId` by matching the exercise's old `dayNumber` to the new day's `position`.
3. `workout_sessions`: additive `ALTER TABLE ADD COLUMN dayId INTEGER`, backfilled by matching
   existing `dayNumber` to the new day's `position` (best-effort; old data has no insert/renumber
   history to reverse, so this is exact for anyone migrating from the current single-cycle data).
4. New `exercise_day_links` table created fresh.

---

## UI Deltas (sketch only, not yet built)
- New dedicated pool/assignment screen: exercise list with a multi-select "which days is this
  on" control (checkboxes per day) instead of a single day picker.
- Insert-day action takes a target position ("insert before Day 3"), not just append.
- Archive-day confirm dialog lists exercises that would become unassigned (i.e. had no other
  day) vs. ones that just lose this one assignment and stay elsewhere.
- Existing ScheduleScreen/WorkoutDayScreen read exercises through the join table; a day's
  exercise list order comes from that day's `exercise_day_links.position`, not a global one.

---

## Related Code Fix (done alongside this doc)
The Insights "Retro" lookback graph bucketed history into hardcoded rolling **7-day** windows
regardless of the plan's actual day count. Fixed to bucket by the plan's current day count
instead, so it stays meaningful once cycles aren't fixed at 7 days (see `InsightsScreen.kt`).

---

## Status
Planning only - no entities/migration/UI code written yet. Next step (when confirmed): Phase 1
data model + migration, verified with a real Gradle build before any repository/UI changes.
