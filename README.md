# Workout Assist

A lean, **local-only Android app for fast workout logging during training**. It is built to get out of your way: a few taps to log a set, a clear view of where you are in your training cycle, and simple progress insights — all stored on-device with no account and no cloud.

---

## What it does

Workout Assist follows a repeating **7-day training template** (Day 1 → Day 7, with a rest day) and helps you:

- **Log workouts fast during a session** — planned vs. actual reps/weight per set, wheel-picker inputs, skip, per-set edits, and a distraction-free "focus mode" that hides the bottom navigation while you train.
- **See your whole cycle at a glance** — a single Workout timeline of Day / Date / Workout cards spanning the current cycle and past cycles.
- **Track consistency** — missed days, backfilling past days, and automatic handling of rest days.
- **Review progress** — rolling adherence ratios and per-exercise history, plus a beta graphs page.
- **Own your data** — everything is local; export/import a JSON backup whenever you want.

---

## Key features

### Workout tab (Schedule)
- **One merged view** with a top-right **Compact ⇄ Calendar** toggle:
  - **Compact** — a tidy timeline that skips missed days and collapses runs of missed days into thin red "domino" pips between cards. It always extends the current cycle forward to its last day.
  - **Calendar** — expands to show every day, rendering missed days as red cards you can tap to **backfill** a workout you did (or a rest day).
- **Today is always in view**; tap today to start it, tap a past done workout to open it, double-tap a done day to remove it (with confirm).
- **Rest days are smart** — mark a rest day done with a tap, and if a rest day passes untouched the cycle auto-advances so it's never stuck or wrongly shown as "missed."

### In-session logging
- Planned vs. actual capture at the **set level** (reps and optional weight).
- Add/remove exercises and sets mid-session; long-press a set to remove it.
- **Skip** logs a real 0-rep entry and stays re-selectable so you can undo within the session.
- **Fill-down** — the weight you set on a set copies down to the later sets (reps stay per-set, since they usually vary), so you only change what differs.
- Two top-bar **stopwatches** (not saved), told apart by icon: a **Total** session timer and a **Rest** interval timer that resets (with a quick flash) each time you save a set.
- **Finish** and **exit** are press-and-hold (a filling button) to prevent an accidental tap ending your session. Exiting mid-session discards the started session, so only a finished workout counts as done.

### Insights & Analytics
- Rolling adherence ratios shown as battery-style step bars: an adjustable recent window (tap to set 5–15 days, default 7) plus a fixed **last-30-day** bar.
- **Back-to-routine streak** — an on-plan streak toward one full cycle; shows how many days are left to get back on routine, or "You're on routine" once the streak covers a full cycle.
- **Streak Momentum graph** — a horizontally scrollable graph that climbs 1 per workout day and drops to 0 on a missed day; today always shows too, in blue while its workout is still pending, turning green once logged (or red like any other miss if the day passes unlogged). Opens on today, and the chevron icon expands it into a full-screen inspector with your current + best streak, a streak-length breakdown, and consistency stats. Choose the style (momentum or classic triangle) and look (line or stock-market candles) under Settings > Streak graph. Optional **"Falling miss gaps"** toggle makes a miss run crash progressively below zero (0, -1, -2, ...) instead of flatlining, while a new streak after the gap always restarts fresh at 1 — today's still-pending candle stretches from that depth up to +1 as one long blue candle to show recovery is still possible. The pending color is themeable too (Settings > Theme > Pending candle). The inspector view uses a fixed scale like a stock chart (no auto-zoom) and pans both horizontally and vertically, opening anchored on today's value (and re-anchoring there when you zoom), with a zoom in/out control and a bigger, wider chart area; the compact card itself always stays pinned on today and isn't manually scrollable. Date labels sit right on the chart's zero axis, flipping above or below it per day so they never overlap the candles/line. The compact card uses a fixed -5..+5 y-axis window (instead of auto-fitting to the data) and stays anchored on today. The inspector hides date labels at 100% zoom or below to avoid overlap, showing them again once zoomed in past 100%.
- **Workout Insights** sub-page — per-workout, date-wise exercise history (e.g. `50 x6, 60 x6x6`) with per-set edit/delete, opened from the Insights home.
- **Progress Graphs (Beta)** — consistency rings, weekly-frequency bars, and per-exercise weight/reps line charts (native Compose Canvas).

### Personalization
- **Themes** — four role-based colors (Background / Status / Done / Missed-banner) picked from tappable swatches, plus a custom **HSV gradient color picker**.
- **Editable labels** — rename all page and subpage titles, the Compact/Calendar toggle, the bottom tabs, the missed-day banner text, and the Back-to-routine stat texts in Settings → Labels.
- **View preferences** — choose the default schedule view (Compact or Calendar) in Settings → Appearance, and switch the streak visualization between the momentum graph and the classic triangle (plus bars vs. stock-market candles) in Settings → Streak graph.
- **Backup & Restore** — export/import all local data as JSON with clear success/failure feedback.

---

## Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Persistence:** Room (SQLite), local-only
- **Build:** Gradle (KSP for Room)
- **Min SDK:** 24 · **Target SDK:** 36

---

## Getting started

### Prerequisites
- Android Studio (latest stable) or the Android SDK + JDK 11+.

### Build & run

Windows (PowerShell):

```powershell
.\gradlew.bat :app:assembleDebug     # build a debug APK
.\gradlew.bat :app:installDebug      # build and install on a connected device/emulator
```

macOS / Linux:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Or open the project in Android Studio and press **Run**.

### Tests & checks

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

---

## Project structure

```
app/                     Android application module
  src/main/java/com/example/workoutassist/
    ui/                  Compose screens & components (Schedule, Workout day, Insights, Settings, Graphs)
    data/                Room entities, DAO, database, repository
docs/                    Design docs (DESIGN.md, DESIGN_VERSIONS.md)
```

---

## Privacy

Workout Assist is **local-first**: there is no authentication, no analytics, and no cloud sync. Your workout data stays on your device unless you explicitly export a backup file.

---

## Documentation

- [docs/DESIGN.md](docs/DESIGN.md) — current product snapshot (authoritative behavior).
- [docs/DESIGN_VERSIONS.md](docs/DESIGN_VERSIONS.md) — versioned changelog of design/UX decisions.
