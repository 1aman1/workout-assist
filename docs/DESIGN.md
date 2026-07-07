# workout-assist

Lean Android utility app for fast workout logging during training.

Primary design source: see DESIGN_VERSIONS.md and append all future design updates as version increments.

## Current Product Snapshot (v1.89)

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
- `Finish Workout` in session actions now requires long press and stays available regardless of exercises logged.
- Starting a workout session now briefly shows one random motivational message (2s) with an `X` to dismiss early.
- Forced zero-inset overrides were rolled back to restore touchable top content across pages.
- In active workout data rows, confirmed set edits keep highlighted styling without showing an explicit `Edited` text badge.
- Focused exercise name is centered in the workout session set-info card.
- Active workout no longer shows the Template Frozen/Unlocked lock control.
- Schedule page cards now show date on the left and `Day N` text on the right.
- Left schedule cards use a fixed-width date slot so varying date lengths do not shift layout.
- Infinity page cards now show date as a side-aligned header value and hide exercise-count text.
- Infinity card header alignment now keeps primary text and date on the same row to avoid split top/bottom visual drift.
- Schedule and Infinity cards now always show default `Day N` labels instead of `Today` text.
- Schedule page cards now also hide exercise-count text for a cleaner, consistent card stack.
- Settings, Workout Day, and Workout Start flows now suppress duplicated parent top insets to remove residual empty top gaps.
- Nested views inside Settings and Workout Day now align to their own app bars without extra leading top spacer.
- Active workout progress now appears as compact `1/n Done` text pinned beside the exercise strip so it stays visible while chips scroll.
- Pinned `1/n Done` strip now uses larger text for clearer readability during active workout.
- Workout exercise-strip chip labels now also use larger text for easier readability while scrolling.
- Infinity page `Today` quick-jump button now uses a higher-contrast filled style with larger presence for better visibility.
- System back on Insights/Settings now first returns to Workout home (`Schedule`) instead of exiting immediately.
- System back on Workout home now asks confirmation (`Do you want to exit?`) before closing the app.
- Active workout strip now includes a fixed `i` action to open the currently selected exercise's associated remark on demand.
- Insights now focuses on ratios plus workout-specific history by workout name.
- Top `Trailing 7 Day Ratio` and `This Month Ratio` now show plain count format only (no percentage suffix).
- Insights top ratio strip now displays only the two values (for example `2/7` and `10/31`) without metric label text.
- Insights history selection now uses workout-level dropdown first (day-style workout), then exercise selection within that workout.
- Insights renders selected exercise history as stacked date cards from newest to oldest.
- Each date card shows set-level `weight x reps` entries (for example `50 x6`, `60 x6x6`).
- Insights exercise history now supports pointed edit/delete at set-entry level.
- Insights also supports complete delete of one selected date entry for selected exercise only; same workout on other dates remains untouched.
- A one-time production reset now clears existing persisted workout/session data and reseeds template dates from today on first launch after this update.
- Room destructive migration fallback has been removed so future schema upgrades must use explicit migrations to preserve data.
- On `workout.day`, expanded reps/wgt table edits are now truly per-set and persisted independently (editing one set no longer changes all sets).
- Exercise templates now persist per-set planned reps and per-set planned weight arrays, with set logging reading planned values by set number.
- Backup export/import now includes per-set planned arrays and still imports older backups that only contain scalar reps/weight fields.
- Number wheel dialogs now also accept double tap on the scroll value area as an immediate confirm action.
- In active workout session, logging an exercise now auto-focuses and auto-scrolls to the next unfinished exercise chip.
- Settings now includes a `Page Command Names` reference block with stable names for quick command-style targeting.
- On `workout.day`, cycle swipe navigation has been dropped; header interactions now stay on the selected day's fixed date.
- On `workout.day`, expanded exercise details now show a compact set-wise table with `reps` and `wgt` rows; interval is excluded from this expanded table.
- On `workout.day`, exercise-card metric chips are removed from collapsed rows; reps/wgt table values are editable via wheel picker when expanded (in edit mode).

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
- During active workout, freeze lock toggle is removed for a cleaner top bar.
- Header shows workout name with date on the right.
- Date change is available only when edit mode is ON and workout is not active.
- Manual workout done toggle is visible only in edit mode.
- Pre-workout primary CTA is full-width Start Workout.
- Start Workout is disabled while edit mode is ON.
- Active workout header keeps focus on workout/session controls, with finish accessed through explicit session actions.
- Finish action is triggered by long-press gesture inside session actions to reduce accidental taps.
- On workout start, a short-lived motivational message card appears and auto-dismisses after 2 seconds unless closed sooner.
- Recent forced inset alignment was reverted for now to avoid pulled-up, hard-to-touch top content.
- Set rows that have been edited remain visually distinct (style-only) to reduce accidental re-editing of previously changed sets.
- Exercise title above planned/set rows is centered for better scanability in session mode.
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
- Collapsed rows show exercise name and actions only (no metric chips).
- Expand reveals a compact set-wise table (rows: reps and wgt; columns: sets) plus deeper context.
- Expanded exercise details now include a remarks section.
- In edit-capable context:
  - Long-press drag handle enables reorder.
  - Tapping reps/wgt table values opens wheel picker for direct numeric edits.
  - Delete/edit actions are available through row menu.
- Outside edit mode:
  - Right swipe toggles done/undo for exercises.
  - Swipe-complete hint background uses stronger contrast for clearer action visibility.
  - Toggle is allowed only for past/today workouts (not future dates).

### 7) Quick Edit Wheel Dialog

- Number wheel picker is centered in the dialog when editing expanded table values and quick-edit fields.
- Interval wheel uses 15-second increments.
- Weight wheel uses 0.5 kg increments.

### 5) Completion Behavior

- Exercise done state persists per exercise.
- Workout done state persists per planned date.
- If swipe-done makes all exercises complete, workout is auto-marked done and achievement popup is shown.
- Schedule and day screens both reflect the same workout completion state.

## Interaction Rules

- Edit mode gates template changes.
- Template structure edits remain blocked during active workout.
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
  - name, sets, interval
  - scalar compatibility fields: reps, planned weight
  - per-set planned arrays: planned reps by set, planned weight by set
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
