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

## Version 1.60 (2026-07-04)
- Change summary:
  - Adjusted schedule/infinity day-card typography and metadata placement to match left/right tab requirements.
  - Fixed infinity card header alignment so main text and date remain horizontally aligned.
- Why changed:
  - The prior date-side layout created a visual split where date appeared top-right while primary text looked vertically offset.
- UX impact:
  - Left `Schedule` tab now shows only `Today`/`Day N` labels without inline date and keeps that label right-aligned with larger emphasis.
  - Right `Infinity` tab now hides exercise-count text, keeps date as a side header value, and aligns it with primary card text.
  - Card content scans more cleanly without top-right/left-bottom misalignment.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.61 (2026-07-04)
- Change summary:
  - Removed `Today` text from schedule and infinity day-card labels.
  - Standardized card labels to always show default `Day N` format.
- Why changed:
  - Keep day-card naming consistent and predictable across both tabs.
- UX impact:
  - Users now see `Day 1`, `Day 2`, etc. in both `Schedule` and `Infinity` cards even for the current day.
  - Existing current-day visual emphasis still applies via styling and completion metadata.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.62 (2026-07-04)
- Change summary:
  - Applied the same metadata simplification to the left `Schedule` tab by removing exercise-count text from day cards.
- Why changed:
  - Keep the left and right schedule pages visually consistent and reduce card clutter.
- UX impact:
  - `Schedule` cards now show day label and workout title without the extra exercise-count line.
  - Card hierarchy now matches the cleaner style already used in `Infinity` cards.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.63 (2026-07-04)
- Change summary:
  - Updated left `Schedule` cards to show date on the left and `Day N` text on the right.
  - Added a fixed-width date slot for left-tab cards to prevent width jitter across different dates.
- Why changed:
  - Improve visual consistency and scanning by keeping date width stable and preserving right-aligned day labels.
- UX impact:
  - Left-tab card headers no longer shift as dates change length.
  - Date appears in a stable left column while day text remains right aligned.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.64 (2026-07-04)
- Change summary:
  - Removed duplicate top inset stacking for `Settings` and `Workout Day Detail` routes.
  - Normalized top spacing for workout start/exercise flows and nested settings sub-pages.
- Why changed:
  - These routes used both root scaffold top padding and nested app-bar insets, creating an unnecessary empty gap at the top.
- UX impact:
  - Settings root and sub-pages now begin directly under their own app bar without extra blank top space.
  - Workout day, exercise list, and active workout start flows now align closer to the top bar with consistent spacing.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.65 (2026-07-04)
- Change summary:
  - Replaced active-workout header text `Logged x/n exercises` with compact `x/n Done` status.
  - Moved this progress indicator into the exercise chip row to avoid consuming a separate line.
  - Made Infinity quick-jump `Today` button text bold.
- Why changed:
  - Improve space efficiency in the active workout header and make progress status faster to scan.
  - Increase visual prominence of Infinity's jump-to-today action.
- UX impact:
  - Active workout top card now shows `1/n Done` inline with chips.
  - Users get one extra visual line for chips/content by removing standalone progress line.
  - `Today` call-to-action on Infinity page is easier to notice.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.66 (2026-07-04)
- Change summary:
  - Kept active-workout `x/n Done` progress always visible by moving it outside the horizontally scrollable chip strip.
  - Placed progress in a fixed header slot beside the strip on the same page.
- Why changed:
  - Progress status scrolled out of view when chip strip moved, reducing continuity during workout flow.
- UX impact:
  - `x/n Done` remains visible at all times while users scroll exercises.
  - Exercise strip remains scrollable without taking the progress indicator with it.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.67 (2026-07-04)
- Change summary:
  - Increased text size of the pinned active-workout `x/n Done` strip.
- Why changed:
  - Improve readability of progress status during active workout flow.
- UX impact:
  - `x/n Done` status remains fixed and is now easier to read at a glance.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.68 (2026-07-04)
- Change summary:
  - Increased text size for the workout exercise-strip chip labels.
  - Further increased text size for the pinned `x/n Done` strip to keep both strips visually consistent.
- Why changed:
  - Improve readability for both progress and exercise-strip labels during active workout flow.
- UX impact:
  - `x/n Done` is easier to read at a glance.
  - Exercise strip labels remain scrollable but are now clearer and more legible.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.69 (2026-07-04)
- Change summary:
  - Added root back-navigation behavior so system back from `Insights` or `Settings` goes to Workout home first.
  - Added exit confirmation dialog when system back is pressed on Workout home.
- Why changed:
  - Prevent accidental app exits from non-home tabs and align with common app back behavior.
- UX impact:
  - Back on `Insights`/`Settings` returns to Workout `Schedule` page.
  - Back on Workout home now prompts `Do you want to exit?` with Cancel/Exit actions.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.70 (2026-07-04)
- Change summary:
  - Added a fixed `i` button in the active workout strip to access remarks for the currently selected exercise.
  - Added explicit remark dialog (`<exercise name> remark`) opened from that `i` action.
- Why changed:
  - Make associated remarks available on demand during workout selection flow without expanding exercise cards.
- UX impact:
  - Users can view selected exercise remarks at any time from the strip area.
  - Remark access is explicit and non-intrusive, preserving strip readability.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.71 (2026-07-04)
- Change summary:
  - Increased visual prominence of Infinity `Today` quick-jump button.
  - Switched from outlined treatment to higher-contrast filled CTA styling with larger button presence.
- Why changed:
  - Improve discoverability and tap confidence for the `Today` jump action.
- UX impact:
  - `Today` action stands out more clearly on Infinity page.
  - Faster recognition of jump control while scrolling long cycle lists.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.72 (2026-07-04)
- Change summary:
  - Added data-driven Insights trend cards built from logged sessions and set logs.
  - Added weekly consistency trend (8-week view), session-duration trend, and rep-adherence trend.
- Why changed:
  - Convert existing workout logs into actionable progress signals instead of only static ratios.
- UX impact:
  - Insights now highlights trend direction and recent-window deltas.
  - Mini weekly bar visuals make progress direction easier to scan quickly.
- Data/model impact:
  - No schema change.
  - Insights now consumes live `set_logs` observations in addition to sessions.
- Migration notes (if any):
  - None.

---

## Version 1.73 (2026-07-07)
- Change summary:
  - Removed `Session Duration Trend` card from Insights.
  - Removed percentage suffix from top `Trailing 7 Day Ratio` and `This Month Ratio` displays.
- Why changed:
  - Keep Insights focused on only the trends currently needed.
  - Simplify ratio readability by showing only count form.
- UX impact:
  - Insights now shows consistency and rep-adherence trend cards only.
  - Top ratio values read as plain `done/total` without `%`.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.74 (2026-07-07)
- Change summary:
  - Added double-tap confirm behavior directly on number-wheel scroll values.
  - Kept existing Save button behavior unchanged.
- Why changed:
  - Reduce friction when adjusting wheel values by allowing confirmation from the same interaction area.
- UX impact:
  - Users can either tap `Save` or double tap inside the wheel value area to confirm.
  - Value-picking flow is faster and more direct.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.75 (2026-07-07)
- Change summary:
  - In active workout, logging a focused exercise now auto-selects the next unfinished exercise.
  - Exercise chip strip now auto-scrolls to keep that next focused exercise visible.
- Why changed:
  - Remove repetitive manual horizontal scrolling after each `Log Exercise` action.
- UX impact:
  - Session flow moves forward automatically through remaining exercises.
  - Less friction and fewer taps during workout logging.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.76 (2026-07-07)
- Change summary:
  - Added stable page command names for all main app pages and settings sub-pages.
  - Exposed these names in a new `Page Command Names` block under Settings.
- Why changed:
  - Make page targeting easier and more consistent for command-style requests.
- UX impact:
  - Users can quickly reference canonical page names from Settings.
  - Reduces ambiguity when giving navigation/edit commands.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.77 (2026-07-07)
- Change summary:
  - Added horizontal swipe navigation on `workout.day` header for same-workout cycle hopping.
  - Left swipe moves to previous cycle occurrence; right swipe moves to next cycle occurrence.
- Why changed:
  - Remove repeated manual navigation friction when reviewing previous/next occurrence of the same workout.
- UX impact:
  - Example: from Chest workout day, swipe left jumps to last Chest cycle and swipe right jumps to next Chest cycle.
  - Header now surfaces viewed date and cycle label to make current occurrence explicit.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.78 (2026-07-07)
- Change summary:
  - Added conditional `Today` button on `workout.day` while browsing non-current cycle occurrences.
  - `Today` action resets cycle view back to current cycle occurrence.
- Why changed:
  - Provide a quick way to return after swiping through previous/next workout cycles.
- UX impact:
  - While viewing left/right cycle history, `Today` appears for one-tap return.
  - On current cycle page, `Today` button is hidden to avoid redundant controls.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.79 (2026-07-07)
- Change summary:
  - Removed left/right swipe cycle navigation from `workout.day` header.
  - Removed cycle-view `Today` return button tied to that swipe navigation.
  - Added a dropped-feature note file to keep deferred ideas documented.
- Why changed:
  - Simplify `workout.day` interactions and avoid accidental cycle navigation.
- UX impact:
  - `workout.day` now stays on the fixed selected date with no cycle hopping gestures.
  - Users can revisit the removed idea later through `docs/DROPPED_FEATURES.md`.

---

## Version 1.107 (2026-08-22)
- Change summary:
  - Bugfix: `WorkoutRepository.renameWorkout(dayNumber, workoutName)` only updated `template_days.workoutName`; each `WorkoutSessionEntity` stores its own `workoutName` **snapshot** taken at session-start/finish time, so a rename left already-logged sessions carrying the old name. Since Workout Insights groups history by `workoutName` (`trackedWorkoutNames`, `dayNumberByWorkoutName` in `InsightsScreen`), the same day then showed as two separate tracked workouts (old name and new name) even though `dayNumber` was unchanged.
  - Fix: added `WorkoutDao.renameSessionsForDay(dayNumber, workoutName)` (`UPDATE workout_sessions SET workoutName = :workoutName WHERE dayNumber = :dayNumber`); `renameWorkout` now calls it right after updating the template, relabeling every past session for that day in one shot.
- Why changed:
  - Renaming a workout day should only change its display name, not fragment its logged history.
- UX impact:
  - Workout Insights shows one combined entry per day again after a rename. Existing installs where a rename already split history (before this fix) self-heal the next time that day is renamed again (even re-saving the same name merges old-name and new-name sessions, since the update matches by `dayNumber`).
- Data/model impact:
  - No schema/migration change (existing `workout_sessions.workoutName` column, new query only).
- Migration notes (if any):
  - None.

## Version 1.106 (2026-08-22)
- Change summary:
  - `buildMomentumEntries` now always includes **today** as an entry, tagged with a new `MomentumDayStatus` (`DONE` / `MISS` / `PENDING`) alongside its existing `epochDay`/`value`. If today is already logged it's `DONE` (unchanged running-streak value); otherwise it's `PENDING` (value `0`, outcome not yet known) — including the previously-early-returned empty-history case, which now returns a single `PENDING` entry for today instead of an empty list.
  - `MomentumCandleChart` and `MomentumLineChart` both take a `pendingColor` + `pendingLast: Boolean` and render the final candle/point/segment in that pending color (a blue, `Color(0xFF2563EB)`) instead of the usual green/red when today hasn't been logged yet. Once the workout is logged the entry becomes a normal green `DONE` bar on the next recomposition; if the day passes unlogged it becomes a normal red `MISS` bar the next time the graph builds (today shifts forward), with no special-casing needed since `buildMomentumEntries` is recomputed from the current date each time.
  - The graph header's **Inspect** `TextButton` was replaced with an icon-only `IconButton` using `Icons.AutoMirrored.Rounded.KeyboardArrowRight` (the same chevron used by `SettingsNavCard`), so expanding the inspector now reads as a chevron affordance rather than a labeled button.
  - The main card's streak summary chips gained a **Current streak** chip (using the existing `routineStreak`) placed before **Best streak**, with **Breaks this month** last. The inspector's Consistency block was reordered the same way: Current streak, Best streak, Breaks this month, Breaks last 3 months, then Active days / Streaks / Avg streak / Longest gap (unchanged).
- Why changed:
  - Showing today on the graph (rather than omitting it until logged) keeps the chart's rightmost entry meaningful at a glance; a neutral pending color avoids implying a miss before the day is over. Current streak is the more actionable, time-sensitive number so it now leads both streak summaries.
- UX impact:
  - The streak graph's last bar is blue on an unlogged today, flips to green once you log a workout, and turns red like any other miss if you let the day pass. The expand affordance is now an icon, consistent with Settings' navigation chevrons.
- Data/model impact:
  - `MomentumEntry` gained a required `status: MomentumDayStatus` field (no default) — all call sites updated. No persistence/schema change.
- Migration notes (if any):
  - None.

## Version 1.105 (2026-08-20)
- Change summary:
  - Insights streak visualization now defaults to a new **Streak Momentum** graph (replacing the routine triangle). A pure helper `buildMomentumEntries(completedDays, todayEpochDay, windowDays=120)` produces per-calendar-day `MomentumEntry(epochDay, value)` over the recent window: each completed day carries its running streak (1,2,3,...), each missed day is `0` (so consecutive misses each show), with a leading `0` the day before the first run and trailing `0`s for confirmed misses up to yesterday (today, if still unlogged, isn't a miss). Rendered as a horizontally scrollable line or candle chart with a Y-axis (streak scale) and an x-axis of **dates** (day-of-month, via `epochDayToDayOfMonth`); candle misses draw red (a 1-unit red tick for a continuing miss, a full drop for the first miss). Auto-anchors right (latest). Best streak is the all-time max run length (`streakRunLengths`), independent of the windowed graph.
  - An **Inspect** button on the graph header opens a full-screen `Dialog` inspector (taller 300dp chart, horizontal scroll, right-anchored) showing a Consistency metrics block and a streak-length histogram. Inline chart height 180dp. (Double-tap to inspect was removed in favor of the explicit button.)
  - During an active session, once an exercise is logged its chip greys out (`Modifier.alpha(0.4f)`) so the remaining un-logged exercises stand out while scrolling; re-selecting a logged chip un-greys it for editing.
  - Streak-graph preferences live in a **Settings > Streak graph** subpage (opened via an Options button, like Labels/Theme). It holds two persisted toggles: **Classic triangle graph** (`KEY_CLASSIC_STREAK_GRAPH`, default off = momentum; on = `RoutineBatteryBar`) and **Stock-market candles** (`KEY_MOMENTUM_STOCK_MODE`, default off = a line chart (`MomentumLineChart`); on = `MomentumCandleChart`, green climbs a day / red crashes to zero). `InsightsScreen` reads both as `useClassicStreakGraph` + `stockMode`; the momentum UI is a `StreakMomentumGraph` composable. New `SettingsView.STREAK_GRAPH_OPTIONS`.
  - The momentum graph title is an editable label (Settings > Labels > "Streak graph title", `KEY_TITLE_STREAK`, default "Streak momentum") driving the inline graph and inspector titles. The inspector adds a numeric x-axis (`MomentumValueAxis`) that prints the streak value under each bar/candle (candles use the day-over-day closes, so the leading reset zero is dropped for alignment). The Insights Progress Graphs card was trimmed to just its title + Open button (description removed).
  - The double-tap inspector also lists a scrollable **Consistency** metrics block plus a **streak-length histogram**. Metrics (first is breaks): Breaks this month / last 3 months (calendar-month windows via `startOfMonthEpochDay`; a "break" = the first missed day after a run that has already ended, from `streakBreakDays`, not days-missed), Current streak, Best streak, Active days, Streaks (# of runs), Avg streak, Longest gap (`longestStreakGap`). The histogram counts runs of length 1..7+ (`streakRunLengths` + `streakLengthHistogram`). All helpers are pure and unit-tested; the inspector content scrolls vertically.
  - Settings polish: the subpage entries (Labels, Streak graph, Theme, Page Command Names) are now single tappable title rows (`SettingsNavCard`, title + chevron, no description or separate "Options" button — the description lives inside each subpage). The inspector chart height was reduced to match the inline graph (180dp, no vertical scroll — horizontal scroll only). The Insights "N days to get back on routine" line was removed (the "You're on routine" line stays). The active-session Skip button returned to its own full-width row between Log Exercise and Show Session Actions (no longer a 20:80 split with the actions toggle).
  - Settings > Appearance adds a **Default schedule view** chooser (Compact/Calendar, `KEY_DEFAULT_SCHEDULE_CALENDAR`, default Compact); `ScheduleScreen` gained `defaultCalendarView` initializing its `expanded` state.
  - Bugfix — exiting a session no longer counts as completed: `exitWorkoutModeAndLeave` now calls a new `WorkoutRepository.abandonSession(id)` (deletes the started session; its set logs cascade via FK) instead of `finishSession`. Completion (`finishedAt`) is set only by the Hold-to-Finish action, keeping Insights/streak/schedule "done" accurate.
  - Reps fill-down fix: `updateSetRepsSelection` now applies the picked reps only to the tapped set (reps vary per set); weight (`updateSetWeightSelection`) still ladders down to later sets.
  - Small UI: workout day title centered (`TextAlign.Center`); `ExerciseSetTable` headers `reps`/`wgt` -> `REPS`/`WGT`; active-session rest text moved below the exercise title and capitalized to `Rest 1m30s`.
  - Internal refactor (no behavior change): extracted shared pure helpers into `AppFormatters` (`stripWeightUnit`, `computeRoutineStreak`, `completedInWindow`, `buildMomentumEntries`) and split the 2268-line `WorkoutDayScreen.kt` god file into focused files (`WorkoutActivePage.kt`, `WorkoutSessionControls.kt`, `ExerciseRow.kt`, `WorkoutDialogs.kt`). Added JUnit tests (`AppFormattersTest`) covering the streak/window/weight helpers.
- Why changed:
  - A momentum graph reads "trend" better than a static triangle and scales to long streaks; abandoning (not finishing) an exited session fixes false "completed" days; reps rarely repeat across sets so they shouldn't auto-fill.
- UX impact:
  - Insights opens on today's streak trend with a live current-streak count; a fun Stocks mode and a double-tap inspector; the classic triangle is one toggle away; exiting mid-workout never inflates your streak.
- Data/model impact:
  - New prefs `KEY_CLASSIC_STREAK_GRAPH` (Bool, default false) and `KEY_DEFAULT_SCHEDULE_CALENDAR` (Bool, default false). New repo API `abandonSession` (delete-only; cascades set logs). No schema change.
- Migration notes (if any):
  - None.

## Version 1.104 (2026-08-14)
- Change summary:
  - Ladder set fill-down during a session: `updateSetRepsSelection` / `updateSetWeightSelection` now apply the picked value to the chosen set AND copy it down to every later set (indices `setIndex..exercise.sets - 1`), so logging set 1 pre-fills the rest. Only the explicitly chosen set is marked in `editedSetIndexesByExerciseId`; subsequent per-set edits fill down from their own index. Saving the exercise persists the filled values as the actuals.
  - The Insights adherence ratio bars (`RatioBatteryBar`, last-7 and last-30) now paint their empty cells with the themed missed-banner color instead of a muted surface, matching the routine bar. Added a `remainingColor` parameter (passed `bannerColor` at both call sites).
  - The Insights short ("Last N days") ratio window is now user-editable (5–15): double-tapping the bar opens a `NumberWheelDialog`; the value persists in prefs (`KEY_INSIGHTS_SHORT_WINDOW`, default 7) and drives the label, `total`, and the done-count window. `RatioBatteryBar` gained an optional `onDoubleTap`; `InsightsScreen` gained `shortWindowDays` + `onShortWindowChange`.
  - Active-session top-bar stopwatches changed from a 50:50 split to 40:60 (Total `weight(0.4f)`, Rest `weight(0.6f)`).
  - The focused-exercise rest text was reorganized: moved to the left of the exercise name on one `Row` and simplified from "Rest 1m 30s between sets" to a compact `rest 1m30s` / `rest 2m` / `rest 45s`.
  - The rest (interval) timer now refreshes 2s after a set is logged instead of instantly: logging only bumps `intervalResetSignal`; a `LaunchedEffect` waits `delay(2000)` before resetting `intervalStartMillis` and flashing, so the final rest duration stays readable. Rapid logs re-schedule the reset to 2s after the latest.
  - Each app part now has a friendly `name` in `AppPageCommand` (new field) and the Settings > Page Commands list shows `name · command` with the description below. Added entries for `settings.backup` (Backup & Restore) and `settings.about` (version details + What's new) so every part is listed.
  - The rest timer now blinks while it holds the final duration during the 2s pre-reset window (signalling the imminent refresh), replacing the single post-reset flash; the focused-exercise rest label font was bumped to `titleMedium` for legibility.
  - Expandable exercise card (workout day) reorganized: `interval` and `remarks` are inline (`interval : value`, `remarks : value`) instead of stacked. `ExerciseSetTable` dropped its boxed cells and surface `Card` — reps render as `xN` and weight as `N kg` in plain text on the card background, with tightened spacing (cell 58dp, gap 4dp, label 44dp). Removed now-unused imports (`Card`, `CardDefaults`, `BorderStroke`, `background`, `RoundedCornerShape`).
  - The active-session focused-exercise card lost its `3.dp` elevation shadow and semi-transparent fill; it now uses a solid `secondaryContainer` color so the shadow no longer bleeds behind the exercise title.
  - The Insights routine streak target (`cycleLength`) is now user-overridable (5–15) by double-tapping the triangle (opens a `NumberWheelDialog`), persisted in `KEY_ROUTINE_WINDOW` (0 = use plan length). `RoutineBatteryBar` gained an optional `onDoubleTap`; `InsightsScreen` gained `routineWindowOverride` + `onRoutineWindowChange`. (Both the triangle and the ratio bar use double-tap.)
  - Listed all graphs in Page Commands: added `insights.routine` (Routine Streak), `insights.ratios` (Adherence Ratios), `graphs.consistency` (Consistency Rings), `graphs.frequency` (Weekly Frequency), `graphs.exercise` (Exercise Trends).
  - Added an in-session exercise history peek: double-tapping the focused exercise title (`combinedClickable` `onDoubleClick`) opens a confirm dialog ("View past sessions?" / Cancel / Yes), and confirming shows a temporary `Dialog` overlay of that exercise's prior sessions (grouped from `setLogs` by `sessionId`, excluding `activeSessionId`, matched by `exerciseName`). Each session renders its logged sets with the shared compact `ExerciseSetTable` (read-only reps/wgt rows). `WorkoutDayScreen`/`WorkoutActivePage` gained `setLogs` (threaded from `WorkoutAssistApp`) and `activeSessionId` params. The overlay is fully temporary — back or the close button dismisses it; the active session stays composed underneath (no tab/navigation change). New page command `workout.session.history`.
  - Weight display dropped the `kg` suffix everywhere it shows in exercise cards: `ExerciseSetTable` cells and the active-session `WorkoutSetEditRow` labels strip a trailing `kg` (case-insensitive) for display only; stored values keep their units.
  - Today's due-today schedule marker changed from a pulsing orange flame (`LocalFireDepartment`) to a pulsing angry red emoji (😡); the flame icon import was removed from `ScheduleScreen`.
  - The streak-triangle done-day flames were bumped one size (14dp → 18dp; `fireHeadroom` 18dp → 22dp so top-of-slope flames don't clip).
  - In the active session, Skip and Show/Hide Session Actions now share one `Row` at a 20:80 weight split (Skip `weight(0.2f)`, actions `weight(0.8f)`).
- Why changed:
  - Fewer taps while logging (most sets repeat the same reps/weight); consistent "done vs. remaining" coloring across all Insights bars; a configurable recent-days window; more room for the rest timer; a cleaner rest label; and a moment to read the rest duration before it resets.
- UX impact:
  - Set 1's values ripple down to later sets automatically; ratio bars read consistently and the short window is tap-to-change; the two timers are 40:60; rest reads as "rest 1m30s" beside the exercise; the rest timer lingers 2s before zeroing.
- Data/model impact:
  - New prefs `KEY_INSIGHTS_SHORT_WINDOW` (Int, default 7) and `KEY_ROUTINE_WINDOW` (Int, default 0 = use plan length); everything else is in-session/presentation state.
- Migration notes (if any):
  - None.

## Version 1.103 (2026-07-27)
- Change summary:
  - Turning off Edit mode now shows a "Save changes?" dialog when edits were made (`showSaveEditsPrompt`). Because template edits persist live, entering Edit mode captures a snapshot of `day.exercises` (`editTemplateSnapshot`); **Discard** calls the new `WorkoutRepository.restoreDayExercises(dayNumber, snapshot)` which removes exercises added during the edit, updates surviving exercises in place (same id), and re-inserts exercises deleted during the edit (fresh id; history keeps its `exerciseName`), then normalizes positions. **Save** keeps the live changes. Dismissing the dialog cancels and stays in Edit mode.
  - The post-save backup prompt no longer exports directly: its confirm button is now "Go to settings" and calls a new `onRequestGoToSettings` callback (replacing `onRequestExport`) that leaves day-detail (`currentScreen = SCHEDULE`) and switches to the Settings tab (`selectedTab = SETTINGS`). "Later" dismisses.
  - Added a numeric bottom axis under the Insights routine triangle: a `Row` of `cycleLength` centered labels counting down left-to-right (`total downTo 1`, e.g. 7 6 5 4 3 2 1). The label at the start of the banner-colored region reads as the days remaining to get back on routine.
  - Moved the Progress Graphs (Beta) entry point from Settings > Analytics to the end of the Insights home page (a card with an "Open" button). `InsightsScreen` gained an `onOpenGraphs` callback (wired to `showGraphsPage = true`); the Settings "Analytics" section and its `onOpenGraphs` parameter were removed. The graphs page itself is unchanged.
  - Added per-day fire markers to the Insights routine triangle: for each completed day in the streak, an orange `Icons.Rounded.LocalFireDepartment` (same marker as "due today") is placed just above the green hypotenuse at that step's center. The triangle is wrapped in a `BoxWithConstraints` (extra `fireHeadroom` above the `triangleHeight` so top-of-slope icons don't clip); icon positions are computed from `centerXFrac = (i + 0.5) / total` and the hypotenuse `edgeYFrac = 1 - centerXFrac`.
  - Added a compact streak strip to the Workout (home) title row: a new `StreakBricks(streak, total, remainingColor)` renders `cycleLength` fixed-size bricks (15x10dp) under the plan title — streak bricks in the Done/primary color, the rest in `bannerColor`. `ScheduleScreen` computes `routineStreak`/`cycleLength` locally (same consecutive-recent-days logic as Insights). Visual only; the earlier `1/7` count was dropped from the Insights triangle header too.
  - Made the exercise remark editable during an active session: the focused-exercise `i` dialog changed from read-only text to an `OutlinedTextField` with Save/Cancel. Save calls a new `updateFocusedExerciseRemark` in `WorkoutDayScreen` (threaded to `WorkoutActivePage` as `onUpdateRemark`) which persists via `repository.updateExercise` with a remark-only draft. Remarks are template-level (`ExerciseEntity.remarks`), so the edit carries over to the next session of the same workout (this mapping was already the case; only the in-session editability was new).
  - Added a thin `HorizontalDivider` between the Schedule header (plan title + streak strip + toggle) and the scrolling list, so the header reads as a separate strip instead of blending into the scroll (mirrors the bottom nav's edge).
  - Added horizontal swipe navigation between root tabs: a `detectHorizontalDragGestures` on the root content Box (past a ~72dp threshold) moves `selectedTab` by ordinal — swipe left = next tab (Workout -> Insights -> Settings), swipe right = previous. Gated by `tabSwipeEnabled` (off during an active session, in `DAY_DETAIL`, and while the graphs overlay `showGraphsPage` is shown) so it doesn't fight the exercise strip / charts. Tab content is wrapped in an `AnimatedContent` (`tween(280)` slide + fade) whose direction follows the ordinal change, so both swipes and bottom-nav taps slide in the travel direction.
  - Removed the `streak/total` count text from the Insights routine triangle header (the two-color triangle + axis + fire markers already convey it).
- Why changed:
  - Users wanted an explicit keep/discard choice on leaving Edit mode, and a backup nudge that routes to the Settings export area rather than firing the export immediately.
- UX impact:
  - Editing is undoable via Discard; the backup step is a gentle redirect to Settings.
- Data/model impact:
  - New repository method `restoreDayExercises`; no schema or backup-mapping change. Caveat: an exercise deleted during an edit and then restored via Discard gets a new row id (past set-log rows keep their `exerciseName`).
- Migration notes (if any):
  - None.

---

## Version 1.102 (2026-07-23)
- Change summary:
  - The bottom "Hold to Finish Workout" button (in Session Actions) now finishes via a press-and-hold fill animation on the button itself (an `Animatable` progress fill over ~1.2s, matching the exit hold) and calls finish directly when complete. The separate "Finish day workout?" confirm dialog was removed, along with its `showFinishConfirm` state. Releasing early cancels.
  - Schedule `ScheduleEntryCard` body is a single line: Day (fixed 52dp width) | Date (fixed 64dp width) | workout name (`weight(1f)`), all vertically centered in one `Row`. The fixed Day/Date widths keep every workout name left-aligned to the same column for view consistency. The DUE tap-hint sits just below this line.
  - Removed the date from the active-workout top bar (the `actions` slot), so during a session the top bar shows only the two stopwatches; the edit-mode `Switch` still shows when not in a session.
  - Merged the two Insights routine bars (streak + days-to-routine) into one full-width **smooth triangle** (`RoutineBatteryBar`, drawn on a `Canvas`): a right triangle rising left-to-right, split vertically at `streak / cycleLength` — the left part fills in the Done/primary color (streak) and the remaining part fills in the themed missed-banner color (days to get back on routine), with an anti-aliased hypotenuse (no cells/steps). `bannerColor` is threaded from `WorkoutAssistApp` into `InsightsScreen`. The stat label was shortened to "routine" (`DEFAULT_TITLE_ROUTINE`); the header still shows `streak/total`. Below the bar: "You're on routine" when complete, else the "N days to get back on routine" caption.
  - The two stopwatches now differentiate by icon instead of text labels: `TopBarStopwatch` dropped the "Total"/"Rest" caption and shows a leading icon (`Icons.Rounded.Timer` for total, tinted `onSurfaceVariant`; `Icons.Rounded.RestartAlt` for the resetting rest timer, tinted `primary`) beside a larger `headlineSmall` monospace time. Layout changed from a `Column` (label over time) to a single centered `Row` (icon + time). Rest still flashes on reset.
- Why changed:
  - Consistency: finishing and exiting are both session-ending, so they now share the deliberate hold gesture; the aligned card columns make the schedule easier to scan.
- UX impact:
  - Finish requires a short hold; schedule rows read as neat aligned columns.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.101 (2026-07-23)
- Change summary:
  - Added two in-session stopwatches to the workout day top bar, laid out as a 50/50 split (`TopBarStopwatch` blocks separated by a `VerticalDivider`):
    - "Total": counts from the moment the workout starts (set when `workoutActive` becomes true).
    - "Rest": counts the interval since the last set log; resets to 0:00 whenever a set's reps or weight is saved (`updateSetRepsSelection` / `updateSetWeightSelection`), and briefly flashes (a ~250ms primary-tinted background pulse via `intervalResetSignal` -> `intervalFlash`) on each reset.
  - Both are `m:ss`, monospace, and are NOT persisted (a `LaunchedEffect(workoutActive)` ticks `nowMillis` every 500ms; elapsed seconds derived from `System.currentTimeMillis()` deltas). New helper `formatStopwatch(totalSeconds)`.
  - Dropped the `logged/total` count (e.g. "2/8") from the active-workout top bar; the date remains on the right (actions slot).
- Why changed:
  - Users wanted a session timer and a rest/interval timer at a glance without leaving the workout; the empty title slot was the natural home.
- UX impact:
  - Two glanceable clocks during a workout; the Rest timer's flash confirms a set was logged.
- Data/model impact:
  - None (timers are ephemeral, in-memory only).
- Migration notes (if any):
  - None.

---

## Version 1.100 (2026-07-23)
- Change summary:
  - Replaced the schedule done-tick (`CheckCircle`) with an achievement marker: completed training days show a medal (`MilitaryTech`) tinted with the Done/Actions theme color (green by default) on a soft circular badge; auto-logged rest days show a muted moon (`Bedtime`). Uses the existing `isRestDay` flag on `ScheduleEntryCard`.
- Why changed:
  - A tick reads as a checklist; a medal gives an earned/achievement feel, while a calmer rest marker keeps the medal meaningful.
- UX impact:
  - More rewarding "done" state in Compact and Calendar.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.99 (2026-07-23)
- Change summary:
  - The exit-active-session confirmation now uses a press-and-hold gesture instead of a tap. Added a `HoldToConfirmButton` (a filling progress button using `Animatable` + `detectTapGestures` onPress/tryAwaitRelease); the "Exit workout mode?" dialog's confirm is "Hold to exit" (~1.2s to complete). Back and the top-bar back still open this dialog; "Stay"/scrim dismiss keep the session.
- Why changed:
  - Users reported sessions ending unexpectedly (stray or pocket touch). A deliberate hold can't be triggered accidentally.
- UX impact:
  - Safer mid-session exit; intentional action required.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.98 (2026-07-23)
- Change summary:
  - Made the Back-to-routine stat text editable in Settings > Labels: added `routineTitle`, `daysToRoutineText`, and `onRoutineText` to `AppLabels` (defaults "Back to routine", "days to get back on routine", "You're on routine"), persisted in SharedPreferences and threaded to `InsightsScreen`.
  - Dropped the number's singular/plural special-case (the suffix is now a single user-defined label).
- Why changed:
  - Consistency with the app's fully-editable label system; lets users phrase the routine nudge themselves.
- UX impact:
  - Customizable routine wording.
- Data/model impact:
  - No schema change; three new title/text preferences.
- Migration notes (if any):
  - None.

---

## Version 1.97 (2026-07-23)
- Change summary:
  - Added a "Back to routine" stat to the Insights home card. It is an on-plan streak: the number of consecutive most-recent days that each have a logged session, measured up to today (or yesterday while today is still unlogged, so the in-progress day isn't penalized). Because rest days auto-log, the streak only breaks when a scheduled workout day is missed.
  - Shows `daysToRoutine = max(0, cycleLength - streak)` as "N day(s) to get back on routine" plus the current streak, or "You're on routine" once the streak reaches one full cycle (cycleLength = number of template days, default 7).
- Why changed:
  - Chose a metric that rewards showing up now (the old rolling-missed-days idea only shrank as misses aged out, not with effort). The streak is action-responsive, deterministic ("do N more days"), dynamic (a miss resets it), and maps to the Day 1-7 cycle.
- UX impact:
  - A clear, motivating recovery target after a gap.
- Data/model impact:
  - None (derived from existing finished sessions).
- Migration notes (if any):
  - None.

---

## Version 1.96 (2026-07-23)
- Change summary:
  - The missed-day banner text is now editable in Settings > Labels ("Missed banner text", default "Missed · tap to add"); added `missedBannerText` to `AppLabels` and persisted it.
  - Added a 4th theme role, "Missed banner": a full `BANNER_THEME_OPTIONS` set + custom color, resolved to `bannerThemeColor` and passed to `ScheduleScreen`; the missed-day cards and Compact domino pips now use this color (was hardcoded `0xFFBF360C`). The banner card text auto-contrasts against the color.
- Why changed:
  - Let users fully customize the most prominent alert element (missed days) in both wording and color.
- UX impact:
  - The missed-day banner matches the user's chosen palette and copy.
- Data/model impact:
  - No schema change. New SharedPreferences: banner theme id/custom hex and the missed banner text (defaults preserve the current red + copy).
- Migration notes (if any):
  - None.

---

## Version 1.95 (2026-07-23)
- Change summary:
  - Workout Insights moved off the Insights home into its own in-tab page: the home shows ratios + a "Workout Insights" Open button; the button navigates to the per-workout history view with a back arrow and a `BackHandler`.
  - Settings > Labels expanded to edit all page/subpage titles: added editable Insights title, Workout Insights title, Progress Graphs title, Theme title, Labels title, and Page Commands title (alongside the existing plan title, Compact/Calendar buttons, and Workout/Insights/Settings tabs).
  - Introduced an `AppLabels` data class; `SettingsScreen` now takes `labels: AppLabels` and `onLabelsSaved: (AppLabels) -> Unit` instead of a positional 6-tuple. The Settings subpage top-bar titles and the Insights/Progress Graphs titles now read from these labels.
  - Added `insights.workout` to the page command list.
- Why changed:
  - Declutter the Insights tab and make the detailed workout history a focused page; let users rename every visible page/subpage title.
- UX impact:
  - Cleaner Insights landing; consistent, fully-customizable titles.
- Data/model impact:
  - No schema change. Six new title preferences persisted in SharedPreferences with defaults matching the current copy.
- Migration notes (if any):
  - None.

---

## Version 1.94 (2026-07-23)
- Change summary:
  - Settings export/import feedback moved to the top of the page with an auto-scroll-to-top on new feedback (was rendered below the Advanced section, easy to miss).
  - Insights top ratios changed from plain `n/7` and `n/30` text to horizontal battery-style step bars (7 and 30 cells; filled = done days) with the `n/total` shown beside the label.
  - Workout Insights dropdown now shows the cycle day number as a prefix (`Day N - Name`) for both the selected button and the menu items (`FinishedWorkoutSessionSnapshot` gained `dayNumber`; a `dayNumberByWorkoutName` map is passed to the card).
  - Removed the workout-day right-swipe-to-mark-done gesture and dropped its code: `SwipeToDismissBox` wrapper, `SwipeHintBackground`, the achievement popup, `canToggleExerciseDone`/`viewedDateIsCompleted`, and the now-orphaned `WorkoutRepository.setExerciseDone` / `WorkoutDao.updateExerciseDone`.
  - Removed the date shown on the workout-day header (non-session view).
  - Moved Page Command Names behind an Options button (new `SettingsView.PAGE_COMMANDS`); refreshed the `PAGE_COMMAND_NAMES` descriptions and added `settings.pagecommands`.
- Why changed:
  - Make backup feedback actually visible; make consistency easier to read at a glance; make the insights selector clearer; remove a non-obvious/accidental swipe action and screen clutter; declutter Advanced settings.
- UX impact:
  - Clearer feedback, ratios, and selector; fewer accidental "done" toggles; tidier Settings.
- Data/model impact:
  - No schema change. Removed the unused `updateExerciseDone` DAO query and `setExerciseDone` repository method (the `exercises.isDone` column remains).
- Migration notes (if any):
  - None.

---

## Version 1.93 (2026-07-17)
- Change summary (merged-view refinements + cleanup on top of 1.92):
  - Timeline cards now always show the workout title (Compact and Calendar); removed the "Done/Upcoming" status label. Today keeps "Today · tap to start"; done days keep the tick.
  - Single-tap a done workout opens it in the workout page; double-tap still removes it (confirm).
  - Backfill dialog now includes the rest day (day 7) as a markable option (logs a session with no set logs).
  - Compact view shows missed-day runs between two dates as a thin red "domino" strip (one pip per missed day); Calendar keeps full red missed-day cards.
  - Compact/Calendar transition uses the default `animateItem()` fade-in/placement animations with the exit fade disabled (`fadeOutSpec = null`) so removed red missed-day cards/pips are dropped immediately instead of lingering behind the stack; an earlier custom 1.5s timing and the today-pivot re-anchor were reverted in favor of reveal-on-expand (below).
  - The two page labels were repurposed as the Compact/Calendar toggle button text and are renamable in Settings > Labels (defaults "Compact"/"Calendar").
  - The Workout tab header title is now editable in Settings > Labels as "Plan title" (backed by the existing persisted/backed-up `scheduleTitle`; default changed to "Your plan").
  - Settings > Theme redesigned: replaced the three stacked option-lists + always-visible RGB sliders with a horizontal role selector (Background / Status / Done) plus tappable circular color swatches; the custom swatch opens a color-picker dialog with a tap/drag HSV gradient box (a saturation/value gradient area plus a hue strip, drawn with Canvas). `CUSTOM_THEME_OPTION_ID` is now `internal`.
  - Compact/Calendar transition switched to reveal-on-expand: on expand it animate-scrolls up to reveal the inserted missed-day cards; on collapse it eases back toward today (replaces the today-pivot re-anchor, which left the change off-screen at the default position).
  - Rest-day handling: a rest day that is "up next" (DUE) can be tapped to mark it done (logs an empty rest session, advancing the cycle to Day 1), and if a rest day's scheduled date passes without action the app auto-logs an empty rest session for it (WorkoutAssistApp LaunchedEffect) so the cycle rolls forward instead of the rest day staying stuck as up-next or being rendered as "missed".
  - Removed the top-left "Today: workout done / not logged" status on the Workout tab.
  - Workout day page: removed the non-working edit-mode date picker and mark-done toggle; moved the rename pencil next to the workout title (shown only in edit mode).
- Why changed:
  - Make each card self-explanatory (title + date), make missed days glanceable in Compact, make the mode switch feel deliberate, and drop controls that no longer match the session-based "done" model.
- UX impact:
  - Fewer, clearer affordances; the rename action is discoverable next to the title; the workout view no longer shows a redundant/broken status or edit toggle.
- Data/model impact:
  - No schema change. `ScheduleScreen` gained `completedDayNumberByDate` (already added in 1.92) and dropped the unused `highlightedTodayDayNumber`/gap params. Removed dead repo/DAO members `updateDayDateAndPushForward`/`updatePlannedDate` and the `SchedulePage` enum.
- Migration notes (if any):
  - None.

---

## Version 1.92 (2026-07-17)
- Change summary:
  - Merged the Schedule and Infinity sub-pages into a single Workout-tab view (`workout.schedule`); removed the two-tab page switcher and the `workout.infinity` command.
  - The merged view is one chronological timeline of `Day n - Date` cards covering the entire Day 1-7 cycle and past cycles (not week-bound), auto-scrolled to keep today in view.
  - Default (Compact) mode skips missed days and hides workout names, and always extends the current cycle forward through its last day (day 7) with projected upcoming dates. It never projects beyond the current cycle.
  - A top-right Calendar/Compact toggle expands to a calendar mode that inserts every missed day as a red card and adds the workout name to each card (`Day n - Date - Workout`).
  - Preserved: color coding, today highlight, done ticks. Interactions: tap today to start, tap a future day to open its plan, double-tap a done day to remove (confirm), tap a missed day (Calendar) to backfill.
- Why changed:
  - One view does both jobs (quick cycle overview + factual history), removing the need to switch tabs; the compact default stays tidy while Calendar surfaces missed days on demand.
- UX impact:
  - Fewer taps to see "where am I in the cycle" and "what did I miss"; the plan always shows the full current cycle through day 7.
- Data/model impact:
  - No schema change. `ScheduleScreen` now also receives `completedDayNumberByDate` (date to cycle day-number map derived from sessions). The one-time gap system (long-press add/remove) is no longer surfaced in this view; gap callbacks remain but are unused.
- Migration notes (if any):
  - None.

---

## Version 1.91 (2026-07-17)
- Change summary (bug-fix batch):
  - Mid-session add-set now takes input: reps/weight wheel-picker selections for a newly added set are applied instead of being dropped (selection updates pad the in-memory list up to the new set index).
  - Infinity (`workout.infinity`) logged workouts can no longer be removed by long-press/hold. Removal now requires a deliberate double-tap on a done day, which opens a confirm dialog. A single-tap on a done day does nothing.
  - Schedule (`workout.schedule`) "Up next" no longer skips rest days: after the last training day, the immediate next day in the cycle (rest included) is shown as Up next (e.g. after Legs → Day 7 Rest, not back to Day 1).
  - Schedule header shows a today indicator: "Today: workout done" (highlighted) or "Today: not logged yet".
- Why changed:
  - Fix a data-entry bug (new sets ignored input), prevent accidental deletions of logged workouts when the phone is in a pocket, and make the plan position honest about upcoming rest days.
- UX impact:
  - Adding a set mid-session works as expected. Removing a logged day is intentional-only (double-tap + confirm). The plan reflects the true next day and today's completion state at a glance.
- Data/model impact:
  - None (no schema change).
- Migration notes (if any):
  - None.

---

## Version 1.90 (2026-07-16)
- Change summary (consolidates schedule/insights/session/analytics work since 1.89; versions 1.80–1.89 were code-only and are rolled up here):
  - Schedule tab (`workout.schedule`) is now a Day 1–7 plan/cycle view that tracks position ("Up next" highlighted, passed days ticked, rest days labelled) with a "N of M workouts done this cycle" header. It is no longer bound to a calendar week.
  - One-time cycle gaps: long-press any day → "Add a gap day after Day N?"; gap rows render below that day as a slim, darker "Gap day" bar; long-press a gap → remove prompt. Gaps are visual spacing only (do not change "Up next") and auto-clear when a workout is completed.
  - Infinity tab (`workout.infinity`) is now a factual history calendar: chronological (oldest at top, today at bottom, auto-scrolled), each date shows the workout done (with tick) or "No workout" gap. Today is always highlighted (done or due).
  - Backfill past dates: tap a past day in the timeline → mark a workout you did (records a backdated session using planned reps/weights) or remove it. Fills gaps from offline days.
  - "Done" is a single definition everywhere: a finished session exists on that date drives Insights ratios, Progress Graphs, and Schedule/Infinity ticks. Deleting a day's session clears its mark.
  - Insights: removed the cosmetic Refresh Stats button; renamed "Delete Record" → "Delete Set"; monthly ratio is now a rolling last-30-days (n/30) to match the rolling last-7 (n/7); exercise chips are ordered by the workout's template sequence (first exercise leftmost).
  - Progress Graphs (Beta): new full page (native Compose Canvas, no chart dependency) with consistency rings (last 7 / last 30), weekly-frequency bars, and per-exercise weight/reps line charts. Opened from Settings → Analytics.
  - Settings grouped into sections: Appearance (Labels, Theme), Data (Backup & Restore), Advanced (Page Command Names), Analytics (Progress Graphs).
  - Active session: "Skip" now logs 0 reps for each set (a real save) and skipped/logged exercises stay re-selectable to undo/redo within the session; rest interval is shown under the focused exercise; the focused card scrolls so Log/Skip stay reachable; long-press a set to remove it; sets can be 0 when editing; add/remove exercise and sets mid-session; k/n progress moved to the app bar and the session title is hidden; the bottom navigation bar is hidden during a session (focus mode) so only Back → confirm exits; motivational banner shows for 3 seconds.
  - Workout-day expanded table shows the weight column as `wt(kg)` with plain numbers; seed template weights are plain numbers only; Treadmill added to the end of every training day.
- Why changed:
  - Separate the two real jobs: Schedule = the Day 1–7 plan and where-am-I; Infinity = a factual, editable history with visible gaps. Make missed days recoverable (backfill) and the plan flexible (one-time gaps) without week-binding.
- UX impact:
  - Missing days no longer loses workouts: "Up next" rolls to the missed workout, gaps show on the timeline, and you can backfill any past date.
- Data/model impact:
  - New repository operations: `logBackdatedWorkout`, `removeWorkoutOnDate`, `clearExerciseLogsForSession`, `removeExerciseSet`, plus one-workout-per-day enforcement on finish. New DAO: `getSessionById`, `deleteSessionById`, `clearWorkoutDoneForDay`, `clearWorkoutDoneForDate`. `MIN_SETS` lowered to 0 and `logSet` allows 0 reps.
- Migration notes (if any):
  - No schema version change. Numeric seed weights and the end-of-cycle Treadmill only apply to fresh installs / reseed / re-import.

- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.80 (2026-07-07)
- Change summary:
  - Updated expanded exercise details on `workout.day` to a table format.
  - Table uses two rows: `reps` and `wgt`, with each column representing one set.
  - Removed interval from the expanded table presentation.
- Why changed:
  - Make expanded exercise details faster to scan and closer to set-by-set mental model.
- UX impact:
  - Users now see values as a compact matrix (`reps`/`wgt`) instead of line-by-line fields.
  - Set count labels are no longer shown in expanded details because columns imply set order.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.81 (2026-07-07)
- Change summary:
  - Removed compact metric chips from collapsed exercise cards on `workout.day`.
  - Kept reps/wgt table visible only in expanded state.
  - Made reps/wgt table values editable via the same wheel picker flow (in edit mode).
- Why changed:
  - Reduce card clutter in collapsed state and keep edits focused in one expanded UI.
- UX impact:
  - Collapsed exercise rows are cleaner and more scan-friendly.
  - Users can tap reps/wgt cells in expanded table to open wheel picker and update values.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.82 (2026-07-07)
- Change summary:
  - Removed the two extra Insights deep metrics (consistency trend and rep-adherence trend), keeping ratio metrics.
  - Added workout-specific Insights history with workout selector chips.
  - Added date-column history grid for selected workout showing last up to 8 same workouts.
- Why changed:
  - Make Insights actionable per workout before starting a session.
  - Reduce noise by keeping only ratio summary + workout-specific history.
- UX impact:
  - Users can tap a workout name (for example Chest) and review recent same-workout performance by date.
  - Insights now emphasizes "last 4-8 same workouts" context over generic global trends.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.83 (2026-07-07)
- Change summary:
  - Added one-time production reset on first launch after update: clears persisted workout/session data and reseeds from today.
  - Removed Room destructive migration fallback from database builder.
- Why changed:
  - Start with a clean production baseline from today's date.
  - Prevent silent destructive data loss in future schema upgrades.
- UX impact:
  - First launch after update resets existing in-app workout/session data and starts schedule from today.
  - Future updates will require explicit migrations instead of automatic destructive wipes.
- Data/model impact:
  - No schema shape change.
  - Runtime startup behavior includes one-time data reset marker in preferences.
- Migration notes (if any):
  - Existing local data is intentionally reset once in this release.

---

## Version 1.84 (2026-07-07)
- Change summary:
  - Added true per-set persistence for workout-day template planning values.
  - Reps/wgt expanded table edits now target the tapped set cell only.
  - Extended exercise schema/model with per-set planned reps and per-set planned weight arrays.
  - Updated set logging to read planned reps/weight from the matching set index.
  - Updated backup export/import to include per-set arrays while preserving compatibility with older scalar-only backups.
- Why changed:
  - Previous workout-day table edits were coupled: changing one set value updated all sets.
  - Users need each set's plan to be independently editable and safely persisted.
- UX impact:
  - Editing set 2 reps or weight no longer changes set 1 (or any other set).
  - Expanded workout-day table now behaves like a true per-set planner.
  - Existing backups still import; new backups preserve richer per-set template data.
- Data/model impact:
  - Room database version increased to 5 with migration adding per-set JSON columns for exercises.
  - Exercise domain model now includes planned values per set and keeps scalar reps/weight as compatibility aliases.
  - Set-log planned values now align with the exact set number instead of a single shared exercise value.
- Migration notes (if any):
  - Existing rows migrate with new columns and continue to resolve planned values through compatibility defaults until users edit per-set values.

---

## Version 1.85 (2026-07-07)
- Change summary:
  - Simplified Insights top ratio presentation to values only.
  - Removed ratio label text from the two top metrics.
- Why changed:
  - Users asked for a cleaner ratio row showing only compact count values.
- UX impact:
  - Insights now shows top counts like `2/7    10/31` without extra labels.
  - Ratio readability is faster with less text clutter.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.86 (2026-07-07)
- Change summary:
  - Replaced Insights workout history selection chips with a dropdown selector.
- Why changed:
  - Keep the selector compact and less visually noisy while preserving the same filtering behavior.
- UX impact:
  - Users now choose workout history target from a dropdown list instead of horizontal chips.
  - Selected workout remains visible in the control and updates the same 4-8 session history grid.
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.87 (2026-07-07)
- Change summary:
  - Replaced the workout history summary grid (`sets`, `ex`, overall `reps`) with day-level `weight x reps` entries.
  - Updated Insights history selection focus to exercise-level progression using a dropdown selector.
- Why changed:
  - Users need quick date-by-date recall of what weight and reps were performed, not aggregate counts.
- UX impact:
  - For each logged date, Insights now shows compact lines like `50 x6` and `60 x6x6`.
  - Set count is naturally inferred from entry repetitions without extra summary rows.
  - History remains focused on recent dates (last up to 8).
- Data/model impact:
  - None.
- Migration notes (if any):
  - None.

---

## Version 1.88 (2026-07-07)
- Change summary:
  - Added pointed set-entry edit in Insights exercise history (fix wrong set data directly).
  - Added set-entry delete in Insights for targeted cleanup.
  - Added complete date-entry delete for selected exercise and selected date only.
- Why changed:
  - Users need precise correction tools for wrong set logs without affecting same workout history on other dates.
- UX impact:
  - Inside each date section, users can edit reps/weight for an individual set entry.
  - Users can delete only one set entry, or delete the whole selected-date entry for selected exercise.
  - Deleting one date entry does not remove same-workout entries from other dates.
- Data/model impact:
  - No schema changes.
  - Added DAO/repository operations for targeted set-log update/delete and exercise-date scoped delete.
- Migration notes (if any):
  - None.

---

## Version 1.89 (2026-07-07)
- Change summary:
  - Changed Insights selection flow to workout-first dropdown (day-level workout), then exercise selection within that workout.
  - Reworked history display into stacked date cards ordered newest to oldest.
- Why changed:
  - Users requested workout-level entry point (like schedule workout grouping) before drilling into exercise history.
  - Card stack layout improves scanability of recent-to-older history.
- UX impact:
  - Users first choose a workout (for example Chest day), then pick a specific exercise from that workout.
  - Selected exercise history appears as stacked date cards with recent date at top and older dates below.
  - Existing pointed set edit/delete and date-scoped delete controls remain available inside each card.
- Data/model impact:
  - No schema changes.
  - Query/filter logic now scopes exercise options and date history by selected workout.
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
