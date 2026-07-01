# Workout Assist Design Versions

This document is the single source of truth for product and UX design decisions.

How to use:
- Keep old versions immutable.
- Add only incremental updates in a new version section.
- Use date and short rationale for each change.

---

## Version 1.0 (2026-06-27)

### 1. Product Goal
- Utility-first Android app for passive workout tracking.
- Fast logging during workout with minimum taps.
- Local-only persistence (no auth, no cloud).

### 2. App Structure
- Home: schedule-like day stack.
- Day Detail: exercise list and template editing.
- Active Logging (inside Day Detail): set-by-set actual logging.
- History: deferred design details for now.

### 3. Home Screen Design
- Schedule-inspired stacked list similar to Google schedule style.
- Today card is visually dominant (thick/larger).
- Past/future cards are smaller and lighter.
- Card content emphasis: workout name first.
- Tap a day card to open Day Detail.

### 4. Day Selection
- Manual day pick by user.
- Not auto-selected by weekday logic for workout execution.

### 5. Workout Template Model
- One repeating 7-day loop template.
- A day contains multiple exercises.
- Each exercise fields:
  - Name
  - Sets (1..8)
  - Reps (1..50)
  - Interval (seconds)
  - Planned weight (string/optional)

### 6. Reordering and Edit Rules
- Reorder via hamburger/drag handle is enabled only in edit mode.
- Reorder persists for future sessions.
- Delete action is only available in edit mode.

### 7. Active Workout Rules
- Start workout from Day Detail.
- Freeze mode defaults ON at workout start.
- If freeze is ON during active workout:
  - Delete is hidden.
  - Destructive/template-structure actions are blocked.
- Set logging flow:
  - Show planned set target.
  - User logs actual reps done.
  - Move to next set automatically until configured sets complete.
  - After last set for an exercise, prompt to move to next exercise.

### 8. Finish Flow
- End-of-day workout asks confirmation dialog first.
- After confirm, show summary dialog/screen.

### 9. Seed Data Requirement
- App ships with one default template entry so UI is visible on first launch.
- User should not need to create first workout just to understand app.

### 10. Seed Template (Default Sample)
- Day title: Upper A (sample)
- Exercises:
  1. Incline Dumbbell Press (30 deg), 3 sets x 12 reps, interval 90s, weight 17.5 kg
  2. Machine/Cable Chest Fly, 3 sets x 12 reps, interval 60s, weight 35 kg
  3. Lat Pulldown, 3 sets x 12 reps, interval 90s, weight 45 kg
  4. Seated Cable Row, 3 sets x 12 reps, interval 90s, weight 38 kg
  5. Arm Superset (Biceps + Triceps), 2 sets x 10 reps, interval 60s
  6. Finish Crunches, 2 sets x 15 reps, interval 45s

### 11. Out of Scope (Deferred)
- Graphs and analytics (separate page/tab later).
- Timer automation behavior.
- Cloud sync and auth.

### 12. Notes for Next Iteration (Suggestion Backlog)
- Replace up/down swap controls with true drag-and-drop reorder.
- Add wheel picker input mode for sets/reps editing.
- Add explicit workout session history screen with planned vs actual comparison cards.

### 13. Follow good coding implementation guidelines so our projects is readable, extensible, maintainable.

---

## Version 1.1 (2026-06-27)
- Change summary:
  - App display name updated to GudHealth.
  - Launcher icon replaced with a custom health and fitness mark.
  - Adaptive icon monochrome asset added for modern launcher theming.
  - Non-v26 fallback launcher resources added for older Android support.
- Why changed:
  - Establish clear product identity early.
  - Improve first impression on app drawer and home screen.
- UX impact:
  - Users now see GudHealth as app label.
  - Launcher icon is easier to recognize and aligned with workout plus wellness use case.
- Data/model impact:
  - No data schema or behavior change.
- Migration notes (if any):
  - Existing installs may need launcher refresh or reinstall to immediately show updated icon on some launchers.

---

## Version 1.2 (2026-06-27)
- Change summary:
  - Finalized day labels and execution model as Day 1 to Day 7 manual pick loop.
  - Finalized edit behavior: full exercise edit supports name, sets, reps, interval, and planned weight.
  - Finalized active logging payload: store actual reps and actual weight per set.
  - Finalized freeze safety rule: while freeze is ON during active workout, template edits are blocked.
  - Confirmed history graphs remain deferred to later versions.
- Why changed:
  - Remove ambiguity before continuing feature work.
  - Keep in-workout interactions safe and low-friction.
- UX impact:
  - Users can manually pick any loop day.
  - Edit mode is predictable and complete when enabled.
  - Active workout flow captures richer real performance data.
  - Freeze mode prevents accidental structure changes during training.
- Data/model impact:
  - Session logging now expects both actual reps and actual weight.
  - No auth/cloud dependency introduced; local persistence remains the source of truth.
- Migration notes (if any):
  - None for current dev phase.

---

## Version 1.3 (2026-06-27)
- Change summary:
  - Replaced first-launch seed from a single sample day to a full 7-day reference template.
  - Seed now mirrors the provided practical split:
    - Day 1: Chest Heavy + Triceps
    - Day 2: Back Heavy + Biceps
    - Day 3: Recovery / Light Day
    - Day 4: Shoulders Priority + Arms
    - Day 5: Upper Body Pump
    - Day 6: Legs Maintenance
    - Day 7: Rest Day
  - Added representative exercise lists for training days, including finisher crunches.
  - Explicitly kept values approximate for planning iteration speed.
- Why changed:
  - Make first-run app state closer to real usage immediately.
  - Allow plan development directly inside the app without manual setup overhead.
- UX impact:
  - New users see a near-real weekly structure on first launch.
  - Early app sessions are more meaningful for flow and UX evaluation.
- Data/model impact:
  - No schema changes.
  - Seed content changed significantly (names, day labels, exercise lists).
- Migration notes (if any):
  - Existing installs with old seeded DB keep existing local data.
  - To view the new default seed immediately on an existing install, clear app data or reinstall in debug.

---

## Version 1.4 (2026-06-27)
- Change summary:
  - Added explicit planned date for each workout day in the 7-day plan.
  - Added reschedule behavior: when one day date is changed, all following days are pushed ahead sequentially.
  - Replaced old day-of-week highlight logic with real-date highlight based on planned date and today date.
  - Moved workout title below the top app bar in day detail for improved readability.
  - Added swipe actions on exercise rows:
    - Right swipe marks exercise done.
    - Left swipe undoes done state.
  - Done exercises now render in gray tone and persist state.
  - Standardized visible date format to compact dd-MMM style (example: 13-Jun), and made date text tappable to change.
- Why changed:
  - Align app flow with real-world missed-day and push-forward scheduling behavior.
  - Reduce top-bar crowding and make day detail easier to scan.
  - Make completion tracking fast with gesture-first interactions.
- UX impact:
  - Users can tap the displayed date and quickly reschedule.
  - Schedule reflects the real upcoming plan, not weekday mapping.
  - Completed exercises are visually distinct and easy to undo.
- Data/model impact:
  - Template day model now stores plannedDateEpochDay.
  - Exercise model now stores isDone.
  - Database version incremented to support new persisted fields.
- Migration notes (if any):
  - Existing debug installs may recreate local database due destructive migration fallback.

---

## Version 1.5 (2026-06-27)
- Change summary:
  - Exercise list rows in workout detail are now collapsed by default.
  - Each row shows exercise name first, with a right-side triangle toggle for expand/collapse.
  - Sets/reps, interval, planned weight, done label, and current-set indicator are shown only when expanded.
- Why changed:
  - Reduce visual noise in longer workout lists.
  - Keep focus on exercise names while still allowing quick access to details.
- UX impact:
  - Users can scan list faster and open details only when needed.
  - Triangle icon direction now communicates collapsed vs expanded state.
- Data/model impact:
  - No schema or data changes.
- Migration notes (if any):
  - None.

---

## Version 1.6 (2026-06-27)
- Change summary:
  - Adjusted date reschedule behavior to rebalance the full 7-day sequence around the edited day.
  - Ensured only one workout card is highlighted as today at a time.
  - Updated done gesture behavior to right-swipe toggle only.
  - Disabled left-swipe action on exercise rows.
- Why changed:
  - Prevent overlapping dates after editing a middle day.
  - Keep today highlight deterministic and singular.
  - Simplify done flow with one consistent gesture for done and undo.
- UX impact:
  - Editing any day date now keeps Day 1 to Day 7 in strict continuous one-day increments.
  - Users no longer see multiple today highlights.
  - Users can swipe right to set done and swipe right again to undo.
- Data/model impact:
  - No schema changes.
  - Scheduling update logic changed from forward-only to full-sequence rebalance.
- Migration notes (if any):
  - Existing stored dates are normalized on the next user date edit.

---

## Version 1.7 (2026-06-27)
- Change summary:
  - Fixed right-swipe done toggle to work repeatedly without leaving and re-entering the screen.
  - Added rename mechanism for schedule screen title (top app bar text is now user-editable).
  - Added system back handling so pressing Android back from day detail returns to schedule screen instead of exiting app.
- Why changed:
  - Resolve workflow friction reported during rapid workout interactions.
  - Allow lightweight personalization of home screen naming.
  - Align in-app back navigation with user expectation.
- UX impact:
  - Done/not-done toggling is reliable for repeated swipes.
  - Users can rename schedule title directly from schedule top bar.
  - Android back now behaves as in-app navigation first.
- Data/model impact:
  - No Room schema changes.
  - Schedule title is stored in local shared preferences.
- Migration notes (if any):
  - Existing users keep default schedule title until they rename it.

---

## Version 1.8 (2026-06-27)
- Change summary:
  - Removed mixed reorder affordances in exercise rows and kept arrow-button reordering as the single mechanism.
  - Expanded exercise details now render as individual fields:
    - set : value
    - reps : value
    - weight : value
    - interval : value sec
  - Added touch-to-edit behavior for these expanded fields in edit mode.
  - Each field opens a wheel-style picker (Android NumberPicker) for quick up/down selection.
- Why changed:
  - Reduce UI confusion by avoiding duplicate reorder controls.
  - Improve speed of editing numeric workout attributes directly in context.
- UX impact:
  - Users use one clear reorder pattern (arrows) in edit mode.
  - Users can tap each expanded metric and adjust value with alarm-clock-like wheel interaction.
- Data/model impact:
  - No schema changes.
  - Existing update path is reused through repository exercise update API.
- Migration notes (if any):
  - None.

---

## Version 1.9 (2026-06-27)
- Change summary:
  - Replaced arrow-based exercise reordering with long-press drag-and-drop in edit mode.
  - Drag handle is now the single reorder control and supports moving item position within the list directly.
  - Existing wheel-based quick-edit fields remain available in expanded row details.
- Why changed:
  - Improve reorder ergonomics and match expected mobile list interaction pattern.
  - Reduce repeated taps required by arrow-only movement.
- UX impact:
  - Users can long-press drag handle and move an exercise to target position in one gesture.
  - Reorder remains restricted to edit-capable context.
- Data/model impact:
  - No schema changes.
  - Reuses existing repository position update flow.
- Migration notes (if any):
  - None.

---

## Version 1.10 (2026-06-27)
- Change summary:
  - Removed day-number text from workout detail top bar title for cleaner in-workout view.
  - Removed redundant "Open" chip from schedule cards.
  - Added workout-level done tick toggle inside workout detail.
  - Added schedule-level accomplished indicator (check + done text) driven by the same persisted workout done state.
  - Stored completion against the planned date (`completedForDateEpochDay`) for resilient behavior when dates shift.
- Why changed:
  - Reduce navigation chrome noise while inside workout detail.
  - Remove duplicate affordances and keep schedule cards focused.
  - Ensure completion is a single source of truth shared between detail and schedule screens.
- UX impact:
  - Users see less clutter in detail top app bar.
  - Schedule list is cleaner and still fully tappable.
  - Marking workout done in detail is reflected immediately on schedule cards.
- Data/model impact:
  - Added `completedForDateEpochDay` to template day data.
  - Added repository/DAO support for toggling workout completion per planned date.
  - Room DB version incremented.
- Migration notes (if any):
  - Debug installs may recreate local DB due destructive migration fallback.

---

## Version 1.11 (2026-06-27)
- Change summary:
  - Removed expanded-row "Done" text after exercise swipe toggle.
  - Restricted exercise done swipe toggle to past and current-date workouts; future-date workouts no longer allow the toggle gesture.
  - Kept exercise metrics behind row expand/collapse control and auto-collapsed rows when edit/drag (hamburger handle) mode is active.
- Why changed:
  - Reduce repeated visual noise once completion is already represented by row style and swipe state.
  - Prevent users from accidentally marking future workouts as completed.
  - Improve reorder ergonomics by prioritizing compact rows while dragging.
- UX impact:
  - Expanded exercise details focus on metrics only.
  - Future workouts do not expose exercise done toggling via swipe.
  - Entering edit mode collapses open metric sections for easier scanning and drag operations.
- Data/model impact:
  - No schema changes.
  - Uses existing per-exercise completion persistence; only UI interaction eligibility changed.
- Migration notes (if any):
  - None.

---

## Version 1.12 (2026-06-27)
- Change summary:
  - Date change affordance in workout detail now appears only when edit mode is enabled and workout is not active.
  - Replaced top-bar "Edit" text label with pencil icon beside the edit switch.
- Why changed:
  - Reduce accidental date edits in normal viewing mode.
  - Simplify top-bar wording and rely on iconography for cleaner UI.
- UX impact:
  - Users see plain date text by default.
  - "Tap to change" date appears only in edit context.
  - Top bar no longer shows explicit "Edit" text keyword.
- Data/model impact:
  - No schema or persistence changes.
- Migration notes (if any):
  - None.

---

## Version 1.13 (2026-06-27)
- Change summary:
  - Removed redundant pencil icon shown next to the edit mode switch in workout detail top bar.
- Why changed:
  - Prevent duplicate edit affordance visual noise (rename pencil + edit pencil).
- UX impact:
  - Top bar now shows a single clear edit icon (rename action) and a clean edit mode switch.
- Data/model impact:
  - No schema or state model changes.
- Migration notes (if any):
  - None.

---

## Version 1.14 (2026-06-27)
- Change summary:
  - Auto-marks workout complete when swipe-done marks all exercises as done.
  - Added achievement popup after the final exercise completion auto-completes the workout.
  - Workout done toggle icon in workout detail header is now visible only in edit mode.
- Why changed:
  - Keep workout-level completion synchronized with exercise-level completion behavior.
  - Provide immediate positive feedback when the full workout is completed.
  - Reduce accidental workout-level completion toggles in non-edit browsing mode.
- UX impact:
  - Completing the final pending exercise by swipe shows a confirmation popup and updates schedule/detail done state.
  - Workout done/manual override icon is hidden unless edit mode is ON.
- Data/model impact:
  - No schema changes.
  - Reuses existing workout completion persistence (`setWorkoutDone`) when auto-completing.
- Migration notes (if any):
  - None.

---

## Version 1.15 (2026-06-27)
- Change summary:
  - Moved workout date display/control into the top-right header area on the same line as workout title.
  - Updated pre-workout CTA from a left-aligned outlined button to a full-width primary "Start Workout" bar.
- Why changed:
  - Free up vertical/header space and improve at-a-glance scan by keeping key metadata on one line.
  - Increase tap target clarity and visual priority for the primary start action.
- UX impact:
  - Date now appears on the right side in workout detail.
  - Start action is centered visually as a full-width primary button when workout is not active.
- Data/model impact:
  - No schema or state model changes.
- Migration notes (if any):
  - None.

---

## Version 1.16 (2026-06-27)
- Change summary:
  - Applied a visual-only UI refresh across schedule and workout detail screens.
  - Added subtle gradient page backgrounds, modernized card depth/borders, and refined top app bar styling.
  - Upgraded primary action presentation (button elevation/shape polish) and card hierarchy for exercise/logger sections.
  - Expanded app theme tokens and typography scale for stronger visual hierarchy.
- Why changed:
  - Align the app look and feel with modern mobile UI patterns while keeping interaction flow unchanged.
  - Improve readability, perceived polish, and touch-target clarity.
- UX impact:
  - Cleaner depth layering and more intentional spacing/typography.
  - Better visual focus on key actions and active workout context.
  - No navigation or behavior changes.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.17 (2026-06-27)
- Change summary:
  - Inside workout day detail, date display/control is now shown only when edit mode is enabled.
  - On the main schedule page, list scrolling behavior is made explicit for overflow scenarios.
- Why changed:
  - Reduce visual clutter in normal workout viewing state and keep edit-only metadata/actions grouped.
  - Ensure main page remains scrollable whenever content exceeds viewport.
- UX impact:
  - Workout date is hidden when edit mode is OFF in day detail.
  - Main schedule continues to show dates and supports scroll when needed.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.18 (2026-06-27)
- Change summary:
  - Added an always-visible compact metric strip inside each exercise card (S, R, W, Rest).
  - Kept expanded exercise details for full view while maintaining current edit interactions.
  - Added current-set progress token in the compact strip for the active exercise.
- Why changed:
  - Improve glanceability during workouts without forcing row expansion.
  - Keep fast metric edits accessible in edit-capable context.
- UX impact:
  - Users can scan key exercise targets directly from collapsed cards.
  - In edit mode, tapping compact chips opens existing wheel editors.
  - Expanded section remains available for detailed metric context.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.19 (2026-06-27)
- Change summary:
  - Enabling edit mode now triggers collapse of expanded exercise cards.
  - Added workout mode back-exit confirmation flow: back press during active workout asks confirmation before exiting.
  - Workout mode exits only on explicit confirmation.
- Why changed:
  - Reduce visual clutter and accidental edit friction when switching into edit context.
  - Prevent accidental loss of active workout context due unintended back navigation.
- UX impact:
  - Edit mode opens in a compact list state for faster scan/reorder.
  - Pressing back in workout mode opens an exit confirmation dialog (Stay/Exit).
  - Exiting workout mode returns to schedule only after user confirmation.
- Data/model impact:
  - No schema changes.
  - Active session finish call is reused when user confirms workout mode exit.
- Migration notes (if any):
  - None.

---

## Version 1.20 (2026-06-27)
- Change summary:
  - Start Workout is now disabled when edit mode is enabled.
  - Added bottom tab bar with Workout (default) and Settings tabs.
  - Added full local backup export/import in Settings.
- Why changed:
  - Prevent accidental session starts while user is editing template details.
  - Provide explicit place for app-level actions without cluttering workout screens.
  - Enable state portability and recovery for local-only usage.
- UX impact:
  - Users must turn off edit mode before starting workout.
  - App now opens on Workout tab and can switch to Settings via bottom nav.
  - Settings offers Export to file and Import from file with status feedback.
  - Back from Settings returns to Workout tab.
- Data/model impact:
  - Added repository/DAO backup primitives for all persisted tables.
  - Backup JSON stores schedule title and all Room-backed entities.
  - Import replaces local tables transactionally to restore snapshot state.
- Migration notes (if any):
  - No schema change required.

---

## Version 1.21 (2026-06-27)
- Change summary:
  - Added a visible right-side scrollbar indicator on the Workout schedule list.
- Why changed:
  - Improve discoverability that more workout cards are available below the fold.
- UX impact:
  - Users now get a clear scroll affordance on the schedule page.
  - Scroll thumb position reflects list position while moving.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.22 (2026-06-27)
- Change summary:
  - Increased right-swipe completion hint contrast on exercise cards.
  - Centered number wheel picker in quick-edit dialog.
- Why changed:
  - Improve readability of swipe action feedback.
  - Fix visual alignment issue where picker appeared left-shifted while editing chip values.
- UX impact:
  - Swipe-to-complete feedback is now more visible.
  - Quick-edit wheel appears centered in dialog for clearer interaction focus.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.23 (2026-06-27)
- Change summary:
  - Updated launcher icon to a black dumbbell symbol on a turquoise background.
  - Slightly increased swipe-to-complete hint darkness for better contrast.
- Why changed:
  - Align app branding with workout context using a clear dumbbell mark.
  - Improve visibility of swipe action affordance in bright conditions.
- UX impact:
  - Home screen/app drawer icon is more distinctive and gym-specific.
  - Right-swipe completion feedback appears darker and easier to read.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.24 (2026-06-27)
- Change summary:
  - Replaced day-screen back text button with a back arrow icon.
  - Updated quick-edit wheel behavior: interval now steps by 15 seconds, weight now steps by 0.5 kg.
  - Applied role-driven color updates: white background, turquoise status surfaces, green done/action surfaces.
- Why changed:
  - Improve navigation clarity and reduce button text noise.
  - Match picker stepping behavior to workout editing expectations.
  - Make highlight/done presentation consistent and theme-driven.
- UX impact:
  - Back affordance is now icon-based in the workout day top bar.
  - Editing interval values jumps in 15-second increments.
  - Editing weight values supports 0.5 kg precision.
  - Exercise status visuals use consistent turquoise surfaces; done/actions are green.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.25 (2026-06-27)
- Change summary:
  - Added small app version label in Settings bottom-right corner.
  - Added tap-to-open version details dialog.
  - App package version updated to 1.25 to align with latest design version.
- Why changed:
  - Provide standard in-app version discoverability.
  - Make release details accessible without leaving the app.
- UX impact:
  - Users can quickly see the current app version in Settings.
  - Tapping the version shows compact release highlights dialog.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.26 (2026-06-27)
- Change summary:
  - Added export prompt when user turns off edit mode after making template changes.
  - Prompt offers Export now or Later.
- Why changed:
  - Encourage backup after editing template data without forcing export every time.
- UX impact:
  - If user edits template values and then disables edit mode, a dialog asks whether to export backup.
  - Export action reuses existing backup flow; user can also skip with Later.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.27 (2026-06-27)
- Change summary:
  - Added role-based theme customization controls in Settings.
  - Users can now pick colors for Background, Status (exercise cards), and Done/Actions surfaces.
  - Theme selections are persisted locally and applied immediately across the app.
- Why changed:
  - Allow lightweight visual personalization while preserving the established white/turquoise/green defaults.
  - Keep theme control centralized in Settings without affecting workout behavior.
- UX impact:
  - New Theme Colors card appears in Settings with single-select chips and color swatches.
  - Switching a color option updates app surfaces in real time.
  - Defaults remain white background, turquoise status, and green done/actions.
- Data/model impact:
  - Added SharedPreferences keys for three theme role selections.
  - No Room schema/data changes.
- Migration notes (if any):
  - Existing users automatically use default role colors until changed in Settings.

---

## Version 1.28 (2026-07-01)
- Change summary:
  - Upgraded backup import/export status to styled feedback cards (success and failure) in Settings.
  - Added exercise remarks field and surfaced it in expanded exercise card details only.
  - Added remarks to exercise add/edit dialog and persisted it in local storage and backup JSON.
- Why changed:
  - Improve clarity and confidence for backup operations with stronger visual feedback.
  - Capture exercise-specific notes without cluttering compact metric chips.
- UX impact:
  - Users now see a clear titled status card with icon and dismiss action after import/export.
  - Exercise cards show remarks only when expanded; collapsed cards remain scan-friendly.
  - Add/Edit exercise dialog now supports entering remarks.
- Data/model impact:
  - Added `remarks` column to exercises table (Room schema version 4 with migration from 3).
  - Backup export/import now includes remarks for each exercise.
- Migration notes (if any):
  - Existing exercises default to empty remarks after migration.

---

## Version 1.29 (2026-07-01)
- Change summary:
  - Added page partition at the top of Schedule screen with `Schedule` and `Infinity` labels.
  - `Schedule` remains the default section and preserves the existing Day 1-7 card UI.
  - Tapping either page label switches between sections in-place.
- Why changed:
  - Use the top empty area to split the schedule landing into named sections.
  - Keep current workflow untouched while enabling expansion into alternate flows.
- UX impact:
  - Users can switch sections by tapping `Schedule` or `Infinity` at the top.
  - Day 1-7 behavior remains unchanged under `Schedule`.
  - `Infinity` is reachable from the same screen and users can switch back instantly.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.30 (2026-07-01)
- Change summary:
  - Upgraded `Infinity` from placeholder to an extended free-scroll schedule window.
  - Infinity now repeats the same Day 1-7 templates across many cycles.
  - Scrolling upward shows earlier cycle days; scrolling downward shows later cycle days.
- Why changed:
  - Make Infinity a practical extension of Schedule instead of a static alternate page.
  - Provide a larger browsing window while preserving existing weekly structure.
- UX impact:
  - Schedule remains default and unchanged for current-week focus.
  - Infinity provides continuous browsing of repeated cycle days in both directions.
  - Users can tap page names to swap between sections and return instantly.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.31 (2026-07-01)
- Change summary:
  - Added a small `Today` quick-jump button in Infinity section.
  - Tapping the button scrolls Infinity list back to today's day entry.
- Why changed:
  - Improve navigation speed in long Infinity scrolling windows.
  - Reduce effort to re-center after browsing far earlier/later cycles.
- UX impact:
  - Infinity now has a bottom-right `Today` action.
  - Users can instantly return to current-day entry while staying in Infinity view.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.32 (2026-07-01)
- Change summary:
  - Replaced small page chips with a larger 50-50 segmented switch for `Schedule` and `Infinity`.
  - Each page option now occupies half width of the top control area.
- Why changed:
  - Improve readability and touch ergonomics of page switching.
  - Better use of the top area with clearer partitioned navigation.
- UX impact:
  - Switching between Schedule and Infinity is easier and more visually distinct.
  - Active page is highlighted inside a wider segmented control.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.33 (2026-07-01)
- Change summary:
  - Removed the top Workout Schedule header section from schedule landing.
  - Added Settings controls to rename both segmented page labels (`Schedule` and `Infinity`).
- Why changed:
  - Reduce wasted vertical space and focus the screen on section switching.
  - Allow users to personalize page names directly from Settings.
- UX impact:
  - Top area now starts with only the two main page buttons.
  - Users can edit both labels in Settings and see changes reflected on the switcher.
- Data/model impact:
  - Added SharedPreferences keys for schedule/infinity page labels.
- Migration notes (if any):
  - Existing installs default to "Schedule" and "Infinity" until changed.

---

## Version 1.34 (2026-07-01)
- Change summary:
  - Restored schedule title rename through a compact pencil action beside the page switcher.
  - Kept the Workout Schedule header section removed as requested.
- Why changed:
  - Preserve rename convenience after removing the header/title row.
- UX impact:
  - Top area still stays minimal (switcher-first) while keeping quick rename access.
- Data/model impact:
  - No schema/data model changes.
- Migration notes (if any):
  - None.

---

## Version 1.35 (2026-07-01)
- Change summary:
  - Removed the compact schedule pencil action from the top section.
  - Removed custom right-edge scrollbar indicators from Schedule and Infinity lists.
- Why changed:
  - Keep the schedule landing cleaner and focused on just the two page buttons.
  - Reduce visual noise while retaining native list scrolling behavior.
- UX impact:
  - Top area now shows only the `Schedule` and `Infinity` segmented buttons.
  - Lists still scroll normally, but without custom scrollbar overlays.
- Data/model impact:
  - No schema or persistence changes.
- Migration notes (if any):
  - None.

---

## Version 1.36 (2026-07-01)
- Change summary:
  - Removed extra top spacing inside Workout Day screen content.
- Why changed:
  - Align Workout Day spacing with Home/Schedule layout so content starts tighter.
- UX impact:
  - First workout card now starts immediately after top app bar content area without additional gap.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.37 (2026-07-01)
- Change summary:
  - Removed top app bar inset gap on Workout Day screen.
- Why changed:
  - Ensure the back arrow and top actions begin without unnecessary top spacing.
- UX impact:
  - Workout Day header now aligns tighter to the top, matching Home behavior expectations.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.38 (2026-07-01)
- Change summary:
  - Updated Workout Day exercise list container to use remaining-height layout.
- Why changed:
  - Prevent visible lower-screen blank strip caused by oversized list measurement in the detail column.
- UX impact:
  - Exercise section now occupies the available area under the header more naturally.
  - Reduces perceived unused space near the lower part of Workout Day screen.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.39 (2026-07-01)
- Change summary:
  - Made Workout Day list bottom padding adaptive to floating action button visibility.
- Why changed:
  - Remove leftover lower empty strip when edit FAB is not present.
- UX impact:
  - In normal view mode, list ends closer to bottom without excessive trailing space.
  - In edit mode, list still keeps enough bottom clearance for FAB.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.40 (2026-07-01)
- Change summary:
  - Updated Infinity card completion rendering to compare against each virtual date.
- Why changed:
  - Avoid repeating `Done` markers on all historical/future cycle copies of a day template.
- UX impact:
  - `Done` appears only on the specific Infinity date that was completed.
  - Past/future repeated instances of the same day no longer inherit completion badge incorrectly.
- Data/model impact:
  - Added completion-date exposure in UI model (`WorkoutDayModel.completedForDateEpochDay`) for rendering.
- Migration notes (if any):
  - None.

---

## Version 1.41 (2026-07-01)
- Change summary:
  - Added `Insights` as a new bottom tab between `Workout` and `Settings`.
  - Made bottom tab labels (`Workout`, `Insights`, `Settings`) user-renamable.
  - Moved label renaming into a dedicated nested `Settings -> Labels -> Options` flow (similar to Theme options).
- Why changed:
  - Expand primary navigation with an insights section.
  - Centralize and scale label customization in a cleaner settings structure.
- UX impact:
  - Bottom navigation now has three tabs.
  - Users can rename both schedule page buttons and bottom tab labels from one Labels options screen.
  - Renamed labels apply directly to bottom navigation and schedule page switcher.
- Data/model impact:
  - Added SharedPreferences keys for root tab labels.
- Migration notes (if any):
  - Existing installs default to `Workout`, `Insights`, `Settings` until changed.

---

## Version 1.42 (2026-07-01)
- Change summary:
  - Refined Insights metrics to be strictly session-level (finished sessions only).
- Why changed:
  - Match expected behavior: ratios should represent workout sessions, not exercise/day aggregates.
- UX impact:
  - Insights now shows rolling done-session ratios such as `2/7` and `13/31`.
  - Day/exercise summary fields were removed from Insights to avoid mixed-level interpretation.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.43 (2026-07-01)
- Change summary:
  - Made `My Ratio` (last 7 days) the leading metric in Insights.
- Why changed:
  - Prioritize the primary user ratio at the top of the Insights screen.
  - Keep the ratio strictly based on finished workout sessions.
- UX impact:
  - Insights now shows `My Ratio` first as a prominent `done/7` value with percentage.
  - 31-day and work:break metrics remain available as secondary context.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.44 (2026-07-01)
- Change summary:
  - Added per-setting RGB color pickers in Theme options.
- Why changed:
  - Enable direct color customization for each theme role instead of only fixed presets.
- UX impact:
  - Theme options now provide sliders for Background, Status, and Done/Actions custom colors.
  - Adjusting sliders auto-selects `Custom` for that role and updates app colors immediately.
  - Preset chips remain available alongside the new picker.
- Data/model impact:
  - Added SharedPreferences keys for persisted custom color hex values by theme role.
- Migration notes (if any):
  - Existing installs keep current preset selections; custom color values use role defaults until edited.

---

## Version 1.45 (2026-07-01)
- Change summary:
  - Revamped Start Workout into a dedicated focused workout page.
- Why changed:
  - Improve one-hand usability and remove keyboard-heavy set logging while training.
  - Let users explicitly choose which exercise to focus first instead of auto-focusing by default.
- UX impact:
  - Entering workout mode now emphasizes an exercise-focus flow rather than inline text fields.
  - Focused exercise shows `(1 + n)` data rows: one read-only planned reps row plus one editable row per set.
  - Set values are chosen via wheel picker dialogs (same interaction style as existing reps/sets wheel edits).
  - Back navigation during active workout remains confirmation-gated before exiting.
- Data/model impact:
  - None (session and set-log persistence remains unchanged).
- Migration notes (if any):
  - None.

---

## Version 1.46 (2026-07-01)
- Change summary:
  - Disabled back navigation while workout Edit mode is active.
- Why changed:
  - Prevent accidental exits while users are intentionally editing workout templates.
- UX impact:
  - Top back button is disabled (gray/inactive) during Edit mode.
  - System back is consumed during Edit mode and no longer navigates away.
  - Normal back behavior resumes once Edit mode is turned off.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.47 (2026-07-01)
- Change summary:
  - Removed extra helper headings from workout start page.
- Why changed:
  - Reduce visual noise and keep focus on exercise chips and set rows.
- UX impact:
  - Hidden labels: `Pick exercise to focus`, `Focused Exercise`, and `Data section (...)`.
  - Workout start page remains functionally the same with a cleaner look.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.48 (2026-07-01)
- Change summary:
  - Styled workout planned-reps row as grayed/read-only.
- Why changed:
  - Improve visual affordance that planned reps are informational and not editable.
- UX impact:
  - Planned reps row text now appears muted/gray in workout start page data section.
  - Editable set rows remain visually stronger for clearer interaction focus.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.49 (2026-07-01)
- Change summary:
  - Added Insights `Refresh Stats` button with circular refresh action.
  - Reduced Insights metrics to only trailing 7-day ratio and this-month ratio.
  - Removed post-finish workout summary stats popup.
- Why changed:
  - Keep Insights focused on the most useful ratio signals.
  - Provide an explicit refresh action for stats.
  - Reduce interruption after finishing workout sessions.
- UX impact:
  - Insights top area now offers `Refresh Stats` with rotating refresh icon action.
  - Insights card now displays only: `Trailing 7 Day Ratio` and `This Month Ratio`.
  - Finishing a workout closes the active session directly without extra summary dialog.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.50 (2026-07-01)
- Change summary:
  - Updated Insights this-month ratio denominator to total days in current month.
- Why changed:
  - Align this-month ratio with expected definition: done sessions divided by days in month.
- UX impact:
  - `This Month Ratio` now displays values like `done/30` or `done/31` (month length based).
  - Early in month, percentage may appear lower because denominator is full month length.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.51 (2026-07-01)
- Change summary:
  - Separated `Finish Workout` from default active-session view.
  - Increased `Log Exercise` touch target and anchored it lower on the active-session page.
  - Added explicit `Show Session Actions` reveal step before showing `Finish Workout`.
- Why changed:
  - Reduce accidental finish taps during logging.
  - Improve one-handed reach and finger-target comfort for the primary logging action.
- UX impact:
  - Active session now prioritizes a large, full-width bottom `Log Exercise` button.
  - `Finish Workout` is hidden by default and appears only after tapping `Show Session Actions`.
  - Logging the final exercise no longer auto-opens finish confirmation; finishing remains explicit.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.52 (2026-07-01)
- Change summary:
  - Updated active-session `Finish Workout` to long-press gesture.
  - Kept `Finish Workout` always enabled inside revealed session actions.
- Why changed:
  - Further reduce accidental finish activation while preserving quick intentional access.
  - Avoid blocking finish based on logged exercise count.
- UX impact:
  - `Finish Workout` remains hidden by default behind `Show Session Actions`.
  - After revealing session actions, users must long-press `Finish Workout` to trigger finish confirmation.
  - Finish is available even when no exercises are logged.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.53 (2026-07-01)
- Change summary:
  - Added random motivational start message when a workout session begins.
  - Message auto-dismisses after 2 seconds and supports early close with `X`.
- Why changed:
  - Add a light, fun engagement moment at workout start without adding friction.
  - Keep interruption minimal with short timeout and manual override.
- UX impact:
  - On `Start Workout`, one of three motivational lines is shown in random order.
  - Message is visible briefly (2s) and can be dismissed sooner via close icon.
  - Core workout flow (focus chips, set rows, log/finish actions) remains unchanged.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.54 (2026-07-01)
- Change summary:
  - Fixed residual top empty-space across app pages by normalizing inset handling.
  - Applied minimal code-quality cleanup by using the same Scaffold inset policy across root and nested page scaffolds.
- Why changed:
  - Ensure no page keeps unexpected blank strip above content/title.
  - Improve consistency and maintainability of layout behavior.
- UX impact:
  - Settings top blank space is removed.
  - Schedule, Insights, Settings, and Workout Day render with consistent top alignment.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.55 (2026-07-01)
- Change summary:
  - Added edited-state tracking for active-session set rows.
  - Edited set rows now show explicit `Edited` indicator and highlighted styling.
- Why changed:
  - Prevent confusion where already-updated set rows looked identical to untouched rows.
  - Reduce accidental re-editing of sets users already changed.
- UX impact:
  - After confirming a set value in wheel picker, that set row remains marked as `Edited`.
  - Edited rows use stronger border/background/value emphasis compared with untouched rows.
  - Logged exercise flow and persistence remain unchanged.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.56 (2026-07-01)
- Change summary:
  - Rolled back forced zero-inset overrides on root/settings/workout scaffolds and top app bars.
- Why changed:
  - Recent inset normalization caused top content to render pulled-up and hard to touch on some pages/devices.
  - Prioritized immediate usability rollback.
- UX impact:
  - Top content touchability is restored with default inset handling.
  - Pages may temporarily show prior top spacing behavior until a safer cross-device inset strategy is reintroduced.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.57 (2026-07-01)
- Change summary:
  - Center-aligned focused exercise name in the workout session set-info card.
- Why changed:
  - Improve visual focus and hierarchy while reviewing set rows during session logging.
- UX impact:
  - Exercise title now appears centered above planned reps and editable set rows.
  - Set editing behavior remains unchanged.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.58 (2026-07-01)
- Change summary:
  - Removed explicit `Edited` text badge from active-session set rows.
  - Kept edited row visual emphasis (border/background/value styling).
- Why changed:
  - Reduce visual clutter while preserving the accidental re-edit prevention cue.
- UX impact:
  - Edited rows are still clearly distinguishable, but without textual `Edited` tag.
  - Set editing flow and behavior remain unchanged.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.59 (2026-07-01)
- Change summary:
  - Removed Template Frozen/Unlocked lock toggle from active workout UI.
  - Simplified template edit gating to remain blocked during active workout without freeze mode state.
- Why changed:
  - Freeze control provided little practical value in current flow and added UI clutter/confusion.
- UX impact:
  - Lock icon and freeze label are no longer shown during workout sessions.
  - Template edits still remain blocked while workout is active.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Versioning Rule
- Every product/UI naming decision must be appended as a new version section.
- Do not rewrite past version content; add only incremental deltas.
- Keep each version entry with: Change summary, Why changed, UX impact, Data/model impact, Migration notes.

## Version Template (Copy for next increments)

## Version X.Y (YYYY-MM-DD)
- Change summary:
- Why changed:
- UX impact:
- Data/model impact:
- Migration notes (if any):
