# workout-assist

Lean Android utility app for fast workout logging during training.

Primary design source: see DESIGN_VERSIONS.md and append all future design updates as version increments.

## Current Product Snapshot (v1.104)

> v1.104 is the authoritative current state for Schedule, Insights, active session, and Analytics. Older bullets below are historical and may be superseded by these notes.

### v1.104 current behavior
- In a session, logging a set's reps/weight fills down: the value is copied to that set and every later set (a ladder), so set 1's values pre-fill the rest; edit any set to fill down from there. Saving persists the filled values.
- The Insights "Last N days" ratio window is editable (5–15): tap the bar to change it (persisted); the 30-day bar is fixed. Empty cells on both ratio bars use the missed-banner color.
- The Insights routine streak triangle's target days are editable (5–15) by long-pressing it (persisted); default follows the plan length.
- Active-session top bar splits the two stopwatches 40:60 (Total : Rest).
- The focused exercise shows its rest to the left of the name as a compact `rest 1m30s` / `rest 2m` (bigger font), and its card is a solid color (no shadow bleeding behind the title).
- After logging a set, the rest timer holds ~2s (so the final duration is readable), blinking to warn of the reset, then resets.
- Expandable exercise cards show `interval : value` and `remarks : value` inline, and reps/weight as plain `xN` / `N kg` text (no boxes), tightly spaced.

### v1.103 current behavior
- Horizontal swipe switches between the bottom tabs from their home screens: swipe left goes to the next tab (Workout -> Insights -> Settings), swipe right goes back, with a directional slide animation (content slides in the swipe direction; a tab tap animates too). Disabled during an active session, in the workout day detail, and over the Progress Graphs overlay (which have their own horizontal gestures).
- The Workout (home) header (plan title + streak strip + Compact/Calendar toggle) is separated from the scrolling list by a thin `HorizontalDivider`, so the header no longer visually blends into the scroll content.
- The exercise remark is editable during an active workout: the `i` remark dialog now has a text field with Save/Cancel. Remarks live on the exercise template, so an edit persists and shows again next time the same workout is opened.
- The Workout (home) title row now shows a compact streak strip under the plan title: `cycleLength` small bricks with the current routine streak filled in the Done/primary color and the rest in the missed-banner color (visual only, no numbers).
- The Insights routine triangle shows a small orange fire icon (same as the "due today" marker) just above the green hypotenuse for each completed day in the streak, marching up the slope.
- Progress Graphs (Beta) now opens from a card at the end of the Insights home page (moved out of Settings > Analytics, which was removed).
- The Insights routine triangle has a numeric bottom axis (e.g. 7 6 5 4 3 2 1, counting down left-to-right) so the days remaining to get back on routine are easy to read off the steps.
- Turning **off** Edit mode now prompts "Save changes?" when edits were made: **Save** keeps them (edits are written live as you make them), **Discard** rolls the day's template back to a snapshot captured when Edit was turned on (added exercises removed, edited ones restored in place, deleted ones re-inserted). Tapping outside the dialog cancels and stays in Edit mode.
- After **Save** (only when something changed), the follow-up backup prompt no longer exports directly — its confirm button reads "Go to settings" and navigates to the Settings tab (where export lives); "Later" dismisses.

### v1.102 current behavior
- Finishing a workout uses a press-and-hold on the bottom "Hold to Finish Workout" button (Session Actions): it fills as you hold (like exit) and finishes when full — there is no separate confirm dialog, so a stray tap can't end the session.
- Schedule cards show Day, Date, and workout name on one line: Day (fixed 52dp) and Date (fixed 64dp) have fixed widths so every workout name starts at the same column; the DUE tap-hint sits just below.
- The active-workout top bar shows only the two stopwatches; the date was removed from the top-right. The two timers are told apart by a leading icon rather than text labels — a clock (`Timer`) for the total and a reset icon (`RestartAlt`, primary-tinted) for the rest timer — with a larger monospace time; the rest timer flashes on reset.
- Insights "routine" stat is a single full-width smooth triangle (rising left-to-right): the left part (your streak) is the Done/primary color and the remaining part (days to get back on routine) uses the themed missed-banner color, split at `streak / cycle`. Below it: "You're on routine" when the streak fills a full cycle, else "N days to get back on routine".

### v1.101 current behavior
- Active workout top bar shows two stopwatches in a 50/50 split: "Total" (whole session, starts when the workout starts) and "Rest" (interval since the last set log). "Rest" resets to 0:00 and briefly flashes whenever a set's reps or weight is saved. Both are `m:ss`, monospace, and not persisted. The old `logged/total` count ("2/8") was removed from the top bar; the date stays on the right.

### v1.100 current behavior
- Done marker upgraded from a plain check to an achievement badge: a completed training day shows a medal (`Icons.Rounded.MilitaryTech`, tinted with the Done/Actions theme color — green by default) on a soft circular badge; an auto-logged rest day shows a muted moon (`Icons.Rounded.Bedtime`) so the medal is reserved for actual workouts.

### v1.99 current behavior
- Exiting an active workout session is now hold-to-confirm: the "Exit workout mode?" dialog's confirm is a press-and-hold button (a red "Hold to exit" that fills over ~1.2s and only exits when full). A tap does nothing; Back / the top-bar back both open this same hold confirm, and "Stay" or tapping outside keeps the session. This prevents accidental exits from a stray/pocket touch.

### v1.98 current behavior
- The Back-to-routine stat texts are editable in Settings > Labels: "Routine stat title" (default "Back to routine"), "Days-to-routine text" (default "days to get back on routine", shown after the number), and "On-routine text" (default "You're on routine"). The number's singular/plural special-case was dropped since the suffix is now user-defined.

### v1.97 current behavior
- Insights home adds a "Back to routine" stat: an on-plan streak = the count of consecutive most-recent days that each have a logged session (rest days auto-log, so only a missed scheduled workout breaks it; today isn't penalized until it's over). It shows `daysToRoutine = max(0, cycleLength - streak)` ("N days to get back on routine") or "You're on routine" once the streak reaches one full cycle. It updates as you show up and resets when a day is missed.

### v1.96 current behavior
- The missed-day banner text (default "Missed · tap to add") is editable in Settings > Labels ("Missed banner text").
- Theme now has a 4th role, "Missed banner", which sets the color of the missed-day cards (Calendar) and the domino pips (Compact); it uses the same tappable swatches + custom HSV picker as the other roles, and the banner text auto-contrasts (black/white) against the chosen color.

### v1.95 current behavior
- Insights tab is split: the home view shows the adherence ratios plus a "Workout Insights" card with an Open button; tapping it navigates to a dedicated Workout Insights page (back arrow / system back returns).
- Settings > Labels now edits all page and subpage titles in addition to the toggle/tabs: Plan title, Compact/Calendar buttons, Workout/Insights/Settings tabs, and titles for Insights, Workout Insights, Progress Graphs, Theme, Labels, and Page Commands. All labels are persisted and applied live (Settings subpage top bars and the Insights/Graphs titles read from them).
- Labels are threaded through a single `AppLabels` data class (SettingsScreen takes `labels: AppLabels` + `onLabelsSaved: (AppLabels) -> Unit`).

### v1.94 current behavior
- Settings export/import feedback renders at the top of the Settings page and auto-scrolls into view so success/failure is immediately visible.
- Insights adherence ratios are horizontal battery-style step bars: last-7-days as 7 steps and last-30-days as 30 steps, each filled cell = one done day (the `n/total` value shown alongside).
- Workout Insights workout dropdown prefixes each workout with its cycle day number (`Day N - Name`) in the button and the menu.
- Workout day page: the right-swipe-to-mark-done gesture is removed (exercise rows render directly; the swipe hint/achievement popup and the `setExerciseDone`/`updateExerciseDone` data methods are dropped).
- Workout day page no longer shows the date in the header (where the exercise list and Start Workout button are).
- Settings > Advanced: Page Command Names moved behind an "Options" button into a dedicated Page Commands view; the command list was refreshed and a `settings.pagecommands` entry added.

### v1.93 current behavior
- Every timeline card shows the workout title in both Compact and Calendar (the separate "Done/Upcoming" status text was removed); today's card still shows "Today · tap to start" and done days keep the tick.
- Single-tap a done workout opens it in the workout page; double-tap still removes it (confirm).
- The backfill dialog (tap a missed day in Calendar) now also lists the rest day (day 7) as a markable option.
- Compact view collapses missed-day runs between two shown dates into a thin red "domino" strip (one pip per missed day); Calendar still shows full red missed-day cards.
- The Compact/Calendar toggle animates cards with the default item fade-in/placement animations (item exit fade is disabled so removed red missed-day cards/pips don't linger behind the stack), plus a reveal-on-expand scroll (below).
- The Compact/Calendar toggle button labels are renamable in Settings > Labels (defaults "Compact"/"Calendar").
- The Workout tab header title ("Your plan") is editable in Settings > Labels ("Plan title", backed by the persisted/backed-up `scheduleTitle`).
- Settings > Theme redesigned: a horizontal role selector (Background / Status / Done) chooses which role to edit, and colors are picked by tapping circular swatches; the last (custom) swatch opens a color-picker dialog with a tap/drag HSV gradient box (saturation/value area + hue strip).
- Compact/Calendar toggle uses a reveal-on-expand transition: switching to Calendar animate-scrolls up so the newly inserted red missed-day cards come into view; switching to Compact eases back to today.
- Removed the top-left "Today: workout done / not logged" status on the Workout tab.
- Workout day page: removed the non-working edit-mode date picker and mark-done toggle (done is session-based); the rename pencil now sits next to the workout title and only shows in edit mode.
- Internal cleanup: removed the obsolete `SchedulePage` enum, the one-time "gap" mechanism, and the orphaned `updateDayDateAndPushForward`/`updatePlannedDate`.

### v1.92 current behavior
- The Workout tab has ONE view (Schedule and Infinity are merged). It is a single vertical timeline of `Day n - Date` cards spanning the whole Day 1-7 cycle and past cycles, chronological (oldest at top, today/upcoming toward the bottom, auto-scrolled to keep today in view).
- Default (Compact) view skips missed days and hides the workout name; it always extends the current cycle forward through its last day (day 7) with projected upcoming dates, and never shows more than the current cycle ahead.
- Top-right Calendar/Compact toggle: Calendar (expanded) view inserts every missed day as a red card and adds the workout name to each card (`Day n - Date - Workout`).
- Color coding, today highlight, and done ticks are preserved. Interactions: tap today to start; tap a future day to open its plan; double-tap a done day to remove it (confirm); tap a missed day (Calendar) to backfill a workout.
- Rest days are completable and self-advancing: when a rest day is "up next" you can tap it to mark it done (logs an empty rest session), and if a rest day's date passes without action the cycle auto-advances (an empty rest session is logged for it) so a rest day is never stuck as up-next or shown as "missed".

### v1.91 current behavior
- Adding a set mid-session now accepts wheel-picker input for the newly added set (reps/weight selections pad the in-memory list instead of being dropped).
- Infinity: a logged workout can no longer be removed by long-press/hold; removal requires a deliberate double-tap on a done day, which then shows a confirm dialog. A plain single-tap on a done day does nothing (prevents accidental deletions with the phone in pocket).
- Schedule "Up next" no longer skips rest days: after the last training day, the immediate next day in the cycle (including a rest day) is shown as Up next.
- Schedule header shows a today indicator: "Today: workout done" (highlighted) or "Today: not logged yet".

### v1.90 current behavior
- Schedule tab: Day 1-7 plan/cycle view with position tracking (Up next highlighted, passed days ticked, rest labelled) and a "N of M done this cycle" header; not week-bound. Long-press a day to add a one-time gap after it (visual spacing, auto-clears on completion); long-press a gap to remove.
- Infinity tab: factual history calendar, chronological (oldest at top, today at bottom, auto-scrolled), today always highlighted; each date shows the workout done (tick) or "No workout" gap; tap a past day to backfill (mark a workout) or remove it.
- Done is one definition everywhere: a finished session on a date drives Insights ratios, Progress Graphs, and Schedule/Infinity ticks; deleting a day's session clears its mark.
- Insights: no Refresh button; "Delete Set" wording; rolling last-7-day (`n/7`) and rolling last-30-day (`n/30`) ratios; exercise chips ordered by the workout's template sequence.
- Progress Graphs (Beta): opened from the end of the Insights page; consistency rings, weekly-frequency bars, per-exercise weight/reps line charts (native Compose Canvas).
- Settings grouped into sections: Appearance, Data, Advanced.
- Active session: Skip logs 0 reps and stays re-selectable to undo; rest interval shown under the focused exercise; content scrolls so Log/Skip stay reachable; long-press a set to remove; sets can be 0; add/remove exercise and sets mid-session; k/n moved to app bar and title hidden; bottom nav hidden (focus mode) so only Back → confirm exits; 3s motivational banner.
- Weights are plain numbers (`wt(kg)` table column); seed template weights are numeric only; Treadmill ends every training day.

### Historical snapshot notes (pre-1.90)

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
