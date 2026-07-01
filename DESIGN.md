# workout-assist

Lean Android utility app for fast workout logging during training.

Primary design source: see DESIGN_VERSIONS.md and append all future design updates as version increments.

## Current Product Snapshot (v1.51)

- Local-only Android app (no auth, no cloud sync).
- 7-day repeating workout template, seeded on first launch.
- Bottom-tab navigation with Workout (default), Insights, and Settings.
- Workout tab has schedule-first navigation with day detail and in-session logging.
- Planned vs actual capture at set level (actual reps and optional actual weight).
- Modernized visual system (refreshed cards, gradients, typography, theme tokens) with no behavioral regressions.
- Settings tab includes full export/import backup for local state.
- Launcher icon uses black dumbbell foreground with turquoise background for stronger brand contrast.
- Theme direction is role-based: white background, turquoise status surfaces, green done/action surfaces.
- Settings now shows a small bottom-corner app version label that opens version details on tap.
- Settings includes role-based theme color selectors for background, status surfaces, and done/action surfaces.
- Export/import now surfaces a styled success/failure feedback card in Settings.
- Schedule screen includes a top page switcher with `Schedule` (default) and `Infinity` sections.
- `Infinity` extends Schedule as a free-scrolling window that continues to earlier and later cycle days.
- `Infinity` includes a small `Today` button to quickly jump back to today entry.
- The Schedule/Infinity switcher uses a larger 50-50 segmented control for easier tapping.
- The old Workout Schedule header section is removed to prioritize the page switcher.
- Custom right-edge scrollbar indicators are removed from Schedule and Infinity lists.
- Workout day screen no longer adds extra top spacing above its first content card.
- Workout day top app bar no longer keeps extra top inset space before navigation/actions.
- Workout day exercise list now fills remaining vertical area to avoid lower blank strips.
- Workout day list bottom reserve is reduced when FAB is hidden to avoid excess lower empty space.
- Infinity `Done` markers are now date-specific and no longer repeat across past/future cycle copies.
- Bottom tab labels (Workout/Insights/Settings) are user-renamable from Settings.
- Insights metrics are session-level (finished sessions), including rolling ratios like `2/7` and `13/31`.
- Insights now leads with a prominent `My Ratio` for last 7 days (`done/7`) based on finished session status.
- Theme settings now include per-role RGB color pickers (Background, Status, Done/Actions) with persisted custom colors.
- Start Workout now switches to a dedicated focused workout page where users explicitly pick an exercise, see a `(1 + n)` data section (planned reps + one row per set), and set per-set reps via wheel picker interaction.
- While Edit mode is active on workout screen, back navigation is disabled to prevent accidental exits.
- Workout start page hides extra helper headings (`Pick exercise to focus`, `Focused Exercise`, and `Data section (...)`) for a cleaner interface.
- Planned reps row in workout start page data section is now visually grayed to reinforce read-only behavior.
- Insights now includes a `Refresh Stats` button with circular refresh action.
- Insights now shows only two metrics: trailing 7-day ratio and this-month ratio.
- This-month ratio is now `done sessions / total days in current month`.
- Finishing a workout session no longer shows a workout summary stats popup.
- Active workout now uses a wider bottom-anchored `Log Exercise` button for easier thumb reach.
- `Finish Workout` is hidden by default and appears only after explicit `Show Session Actions` tap.

## Screen Map

### 1) Schedule Screen

- Top page labels can switch between `Schedule` and `Infinity` sections.
- The top section control is split into two equal-width segments.
- `Schedule` section keeps the existing Day 1 to Day 7 stacked card experience.
- `Infinity` section repeats those day templates in a long vertical window.
- Scrolling up reveals earlier cycle days; scrolling down reveals later cycle days.
- A bottom-right `Today` quick action scrolls directly to the current-day entry.

### 6) Settings Screen

- Includes a `Labels` option similar to `Theme` for nested rename controls.
- Labels options can rename Schedule/Infinity page buttons and Workout/Insights/Settings bottom tabs.
- Renamed labels apply immediately to navigation and workout page switch buttons.
- Shows stacked day cards for the 7-day loop.
- Today card is visually dominant.
- Completed workout days show a done indicator.
- Tap any day card to open workout day detail.

### 2) Workout Day Screen

- Top app bar includes back arrow navigation icon, rename workout action (edit-capable context), and edit mode switch.
- During active workout, freeze lock toggle appears in top app bar.
- Header shows workout name with date on the right.
- Date change is available only when edit mode is ON and workout is not active.
- Manual workout done toggle is visible only in edit mode.
- Pre-workout primary CTA is full-width Start Workout.
- Start Workout is disabled while edit mode is ON.
- Active workout header shows freeze status text, while finish is accessed through explicit session actions.
- Entering edit mode collapses expanded exercise cards for cleaner editing context.
- While workout mode is active, back action asks confirmation before exiting session.
- Turning edit mode OFF after making template changes prompts for backup export.

### 6) Settings Screen

- Accessed from bottom tab bar.
- Provides Export to file and Import from file actions.
- Export writes a JSON backup containing schedule title and all workout tables.
- Import restores that JSON backup into local storage and returns user to Workout tab.
- Provides selectable color options for Background, Status (exercise cards), and Done/Actions roles.
- Small version label is shown at bottom-right.
- Tapping version opens a dialog with latest version details.

### 3) Active Session Panel (inside Workout Day)

- Current set logger displays exercise and set progress.
- Captures actual reps (required) and actual weight (optional).
- Save Set advances set flow automatically.
- After final set on an exercise, prompts to move to next exercise.
- Finish action shows confirmation, then summary dialog.

### 4) Exercise List

- Exercise rows are collapsed by default.
- Exercise status cards (current or done) use a shared turquoise status surface.
- Each row always shows a compact metric strip (set, reps, weight, interval) for quick scanning.
- Expand reveals detailed metric rows and deeper context.
- Expanded exercise details now include a remarks section (remarks are not shown in metric chips).
- In edit-capable context:
  - Long-press drag handle enables reorder.
  - Metric chips and metric detail fields open wheel picker for direct numeric edits.
  - Delete/edit actions are available through row menu.
- Outside edit mode:
  - Right swipe toggles done/undo for exercises.
  - Swipe-complete hint background uses stronger contrast for clearer action visibility.
  - Toggle is allowed only for past/today workouts (not future dates).

### 7) Quick Edit Wheel Dialog

- Number wheel picker is centered in the dialog when editing chips/metrics.
- Interval wheel uses 15-second increments.
- Weight wheel uses 0.5 kg increments.

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
- Start Workout cannot begin while edit mode is active.
- Pressing back in Settings switches back to Workout tab.
- After template changes in edit mode, disabling edit offers Export/Later backup prompt.

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
- Backup file includes:
  - schedule title preference
  - template days
  - exercises
  - workout sessions
  - set logs

## First-Run Seed

- App seeds a full 7-day reference split template.
- User can open a day and start logging immediately without manual setup.

## Deferred / Out of Scope

- Graph/analytics views.
- Automated rest timer behavior.
- Cloud sync and authentication (manual local backup/import available).

## Documentation Rule

- README should reflect current implemented behavior only.
- Historical design decisions and incremental rationale belong in DESIGN_VERSIONS.md.
