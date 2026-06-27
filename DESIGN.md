# workout-assist

Lean Android utility app for fast workout logging during training.

Primary design source: see DESIGN_VERSIONS.md and append all future design updates as version increments.

## Current Product Snapshot (v1.19)

- Local-only Android app (no auth, no cloud sync).
- 7-day repeating workout template, seeded on first launch.
- Schedule-first navigation with day detail and in-session logging.
- Planned vs actual capture at set level (actual reps and optional actual weight).
- Modernized visual system (refreshed cards, gradients, typography, theme tokens) with no behavioral regressions.

## Screen Map

### 1) Schedule Screen

- Shows stacked day cards for the 7-day loop.
- Today card is visually dominant.
- Completed workout days show a done indicator.
- Schedule title is renameable from top app bar.
- Tap any day card to open workout day detail.

### 2) Workout Day Screen

- Top app bar includes back navigation, rename workout action (edit-capable context), and edit mode switch.
- During active workout, freeze lock toggle appears in top app bar.
- Header shows workout name with date on the right.
- Date change is available only when edit mode is ON and workout is not active.
- Manual workout done toggle is visible only in edit mode.
- Pre-workout primary CTA is full-width Start Workout.
- Active workout state shows Finish action and freeze status text.
- Entering edit mode collapses expanded exercise cards for cleaner editing context.
- While workout mode is active, back action asks confirmation before exiting session.

### 3) Active Session Panel (inside Workout Day)

- Current set logger displays exercise and set progress.
- Captures actual reps (required) and actual weight (optional).
- Save Set advances set flow automatically.
- After final set on an exercise, prompts to move to next exercise.
- Finish action shows confirmation, then summary dialog.

### 4) Exercise List

- Exercise rows are collapsed by default.
- Each row always shows a compact metric strip (set, reps, weight, interval) for quick scanning.
- Expand reveals detailed metric rows and deeper context.
- In edit-capable context:
  - Long-press drag handle enables reorder.
  - Metric chips and metric detail fields open wheel picker for direct numeric edits.
  - Delete/edit actions are available through row menu.
- Outside edit mode:
  - Right swipe toggles done/undo for exercises.
  - Toggle is allowed only for past/today workouts (not future dates).

### 5) Completion Behavior

- Exercise done state persists per exercise.
- Workout done state persists per planned date.
- If swipe-done makes all exercises complete, workout is auto-marked done and achievement popup is shown.
- Schedule and day screens both reflect the same workout completion state.

## Interaction Rules

- Edit mode gates template changes.
- Freeze mode defaults ON when workout starts.
- While freeze is ON during active workout, destructive/template structure edits remain blocked.
- Reorder is only available in edit-capable context.
- Workout mode exits only after explicit user confirmation when back is pressed.

## Data Model (Current)

- Template day includes:
  - day number
  - workout name
  - planned date
  - completion-for-date marker
- Exercise includes:
  - name, sets, reps, interval, planned weight
  - position
  - isDone
- Session logging includes:
  - workout session start/finish
  - per-set planned vs actual reps
  - per-set planned vs actual weight

## First-Run Seed

- App seeds a full 7-day reference split template.
- User can open a day and start logging immediately without manual setup.

## Deferred / Out of Scope

- Graph/analytics views.
- Automated rest timer behavior.
- Cloud sync and authentication.

## Documentation Rule

- README should reflect current implemented behavior only.
- Historical design decisions and incremental rationale belong in DESIGN_VERSIONS.md.
