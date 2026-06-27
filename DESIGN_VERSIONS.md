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
