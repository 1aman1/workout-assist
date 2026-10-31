package com.example.workoutassist.data

import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private const val MIN_SETS = 0
private const val MAX_SETS = 8
private const val MIN_REPS = 1
private const val MAX_REPS = 50
private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

data class ExerciseDraft(
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val remarks: String
)

data class ExerciseModel(
    val id: Long,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val plannedRepsBySet: List<Int>,
    val plannedWeightBySet: List<String>,
    val remarks: String,
    val isDone: Boolean,
    // Order within the one day's list this model was read from - not a global order.
    val position: Int,
    // Every day this exercise is currently linked to; empty means it's unassigned/pooled.
    val dayNumbers: Set<Int>
)

data class WorkoutDayModel(
    val dayNumber: Int,
    val workoutName: String,
    val plannedDateEpochDay: Long,
    val completedForDateEpochDay: Long?,
    val isCompleted: Boolean,
    val exercises: List<ExerciseModel>
)

data class BackupSnapshot(
    val days: List<TemplateDayEntity>,
    val exercises: List<ExerciseEntity>,
    val sessions: List<WorkoutSessionEntity>,
    val logs: List<SetLogEntity>,
    val links: List<ExerciseDayLinkEntity>
)

class WorkoutRepository(private val dao: WorkoutDao) {
    fun observeDays(): Flow<List<WorkoutDayModel>> {
        return combine(dao.observeDays(), dao.observeExercises(), dao.observeLinks()) { days, exercises, links ->
            val exerciseById = exercises.associateBy { it.id }
            val linksByDay = links.groupBy { it.dayNumber }
            val dayNumbersByExercise = links.groupBy({ it.exerciseId }) { it.dayNumber }
                .mapValues { (_, dayNumbers) -> dayNumbers.toSet() }
            days.map { day ->
                val dayLinks = linksByDay[day.dayNumber].orEmpty().sortedBy { it.position }
                WorkoutDayModel(
                    dayNumber = day.dayNumber,
                    workoutName = day.workoutName,
                    plannedDateEpochDay = day.plannedDateEpochDay,
                    completedForDateEpochDay = day.completedForDateEpochDay,
                    isCompleted = day.completedForDateEpochDay == day.plannedDateEpochDay,
                    exercises = dayLinks.mapNotNull { link ->
                        exerciseById[link.exerciseId]?.toModel(
                            position = link.position,
                            dayNumbers = dayNumbersByExercise[link.exerciseId].orEmpty()
                        )
                    }
                )
            }
        }
    }

    // Every exercise in the pool, each carrying the full set of days it's currently
    // linked to (empty if unassigned) - used by the exercise-pool management screen.
    fun observeAllExercises(): Flow<List<ExerciseModel>> {
        return combine(dao.observeExercises(), dao.observeLinks()) { exercises, links ->
            val dayNumbersByExercise = links.groupBy({ it.exerciseId }) { it.dayNumber }
                .mapValues { (_, dayNumbers) -> dayNumbers.toSet() }
            exercises
                .sortedBy { it.name.lowercase() }
                .map { it.toModel(position = 0, dayNumbers = dayNumbersByExercise[it.id].orEmpty()) }
        }
    }

    fun observeSessions(): Flow<List<WorkoutSessionEntity>> {
        return dao.observeSessions()
    }

    fun observeSetLogs(): Flow<List<SetLogEntity>> {
        return dao.observeSetLogs()
    }

    suspend fun ensureSeedData() {
        if (dao.countDays() > 0) {
            return
        }

        seedDefaultTemplateFromToday()
    }

    suspend fun resetAllDataAndSeedFromToday() {
        dao.replaceAllData(
            days = emptyList(),
            exercises = emptyList(),
            sessions = emptyList(),
            logs = emptyList(),
            links = emptyList()
        )

        seedDefaultTemplateFromToday()
    }

    private suspend fun seedDefaultTemplateFromToday() {

        val firstPlannedDate = currentDateEpochDay()
        val seedDayNames = listOf(
            "Chest Heavy + Triceps",
            "Back Heavy + Biceps",
            "Recovery / Light Day",
            "Shoulders Priority + Arms",
            "Upper Body Pump",
            "Legs Maintenance",
            "Rest Day"
        )

        dao.upsertDays(
            seedDayNames.mapIndexed { index, workoutName ->
                TemplateDayEntity(
                    dayNumber = index + 1,
                    workoutName = workoutName,
                    plannedDateEpochDay = firstPlannedDate + index
                )
            }
        )

        listOf(
                seedExercise(1, 1, "Barbell Bench Press (Main Lift)", 3, 6, 180, "60"),
                seedExercise(1, 2, "Incline Barbell Press", 3, 6, 150, "50"),
                seedExercise(1, 3, "Chest Dips (Lean Forward)", 3, 8, 120),
                seedExercise(1, 4, "Triceps (Skullcrusher or Rope Pulldown)", 3, 10, 90),
                seedExercise(1, 5, "Finish Crunches", 3, 15, 45),
                seedExercise(1, 6, "Treadmill", 1, 1, 0, "", "10-15 min"),

                seedExercise(2, 1, "Deadlift (Main Lift)", 3, 5, 180),
                seedExercise(2, 2, "Barbell Row", 3, 6, 150, "45"),
                seedExercise(2, 3, "Pull-Ups or Lat Pulldown", 3, 8, 120),
                seedExercise(2, 4, "Biceps Barbell Curl", 3, 8, 90, "20"),
                seedExercise(2, 5, "Finish Crunches", 3, 15, 45),
                seedExercise(2, 6, "Treadmill", 1, 1, 0, "", "10-15 min"),

                seedExercise(4, 1, "Overhead Press", 3, 6, 150, "15"),
                seedExercise(4, 2, "Lateral Raises", 4, 12, 75, "7.5"),
                seedExercise(4, 3, "Rear Delt Raises", 3, 12, 75, "20"),
                seedExercise(4, 4, "Arms Superset (DB Curl + Triceps Pushdown)", 3, 12, 60),
                seedExercise(4, 5, "Finish Crunches", 3, 15, 45),
                seedExercise(4, 6, "Treadmill", 1, 1, 0, "", "10-15 min"),

                seedExercise(5, 1, "Incline Dumbbell Press (30 deg)", 3, 12, 90, "15"),
                seedExercise(5, 2, "Machine/Cable Chest Fly", 3, 12, 60, "35"),
                seedExercise(5, 3, "Lat Pulldown", 3, 12, 90, "38"),
                seedExercise(5, 4, "Seated Cable Row", 3, 12, 90, "38"),
                seedExercise(5, 5, "Arm Superset (Biceps + Triceps)", 3, 12, 60),
                seedExercise(5, 6, "Finish Crunches", 3, 15, 45),
                seedExercise(5, 7, "Treadmill", 1, 1, 0, "", "10-15 min"),

                seedExercise(6, 1, "Barbell Squat", 3, 6, 150, "50"),
                seedExercise(6, 2, "Romanian Deadlift", 2, 8, 120),
                seedExercise(6, 3, "Calf Raises", 3, 15, 60),
                seedExercise(6, 4, "Finish Crunches", 3, 15, 45),
                seedExercise(6, 5, "Treadmill", 1, 1, 0, "", "10-15 min")
            )
            .forEach { seed ->
                val exerciseId = dao.insertExercise(seed.entity)
                dao.insertLink(ExerciseDayLinkEntity(exerciseId = exerciseId, dayNumber = seed.dayNumber, position = seed.position))
            }
    }

    suspend fun renameWorkout(dayNumber: Int, workoutName: String) {
        val clean = workoutName.trim().ifEmpty { "Day $dayNumber Workout" }
        dao.updateWorkoutName(dayNumber, clean)
        // Also relabel this day's past sessions so Workout Insights keeps showing one
        // combined history for the day instead of splitting old-name / new-name entries.
        dao.renameSessionsForDay(dayNumber, clean)
    }

    // Always appends at the end of the cycle (highest dayNumber); reordering the
    // cycle sequence itself is a separate, not-yet-built feature.
    suspend fun addDay(workoutName: String): Int {
        val existingDays = dao.getAllDays()
        val clean = workoutName.trim()
        val nextDayNumber = (existingDays.maxOfOrNull { it.dayNumber } ?: 0) + 1
        val nextPlannedDate = (existingDays.maxOfOrNull { it.plannedDateEpochDay } ?: currentDateEpochDay()) + 1
        dao.upsertDays(
            listOf(
                TemplateDayEntity(
                    dayNumber = nextDayNumber,
                    workoutName = clean.ifEmpty { "Day $nextDayNumber Workout" },
                    plannedDateEpochDay = nextPlannedDate
                )
            )
        )
        return nextDayNumber
    }

    suspend fun addExercise(dayNumber: Int, draft: ExerciseDraft) {
        val clean = draft.sanitized()
        val nextPosition = (dao.getLinksForDay(dayNumber).maxOfOrNull { it.position } ?: 0) + 1
        val plannedRepsBySet = List(clean.sets) { clean.reps }
        val plannedWeightBySet = List(clean.sets) { clean.plannedWeight }

        val exerciseId = dao.insertExercise(
            ExerciseEntity(
                name = clean.name,
                sets = clean.sets,
                reps = clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = clean.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(plannedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(plannedWeightBySet),
                remarks = clean.remarks,
                isDone = false
            )
        )
        dao.insertLink(ExerciseDayLinkEntity(exerciseId = exerciseId, dayNumber = dayNumber, position = nextPosition))
    }

    suspend fun updateExercise(exercise: ExerciseModel, draft: ExerciseDraft) {
        val clean = draft.sanitized()
        val existingRepsBySet = exercise.plannedRepsBySet
            .normalizeRepsBySet(expectedSets = exercise.sets, fallbackValue = exercise.reps)
        val existingWeightBySet = exercise.plannedWeightBySet
            .normalizeWeightBySet(expectedSets = exercise.sets, fallbackValue = exercise.plannedWeight)

        val plannedRepsBySet = if (clean.reps != exercise.reps) {
            List(clean.sets) { clean.reps }
        } else {
            existingRepsBySet.normalizeRepsBySet(expectedSets = clean.sets, fallbackValue = clean.reps)
        }
        val plannedWeightBySet = if (clean.plannedWeight != exercise.plannedWeight) {
            List(clean.sets) { clean.plannedWeight }
        } else {
            existingWeightBySet.normalizeWeightBySet(expectedSets = clean.sets, fallbackValue = clean.plannedWeight)
        }

        dao.updateExercise(
            ExerciseEntity(
                id = exercise.id,
                name = clean.name,
                sets = clean.sets,
                reps = plannedRepsBySet.firstOrNull() ?: clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = plannedWeightBySet.firstOrNull() ?: clean.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(plannedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(plannedWeightBySet),
                remarks = clean.remarks,
                isDone = exercise.isDone
            )
        )
    }

    suspend fun removeExerciseSet(exercise: ExerciseModel, setIndex: Int) {
        if (exercise.sets <= MIN_SETS) {
            return
        }
        if (setIndex !in 0 until exercise.sets) {
            return
        }

        val repsBySet = exercise.plannedRepsBySet
            .normalizeRepsBySet(expectedSets = exercise.sets, fallbackValue = exercise.reps)
            .toMutableList()
        val weightBySet = exercise.plannedWeightBySet
            .normalizeWeightBySet(expectedSets = exercise.sets, fallbackValue = exercise.plannedWeight)
            .toMutableList()
        repsBySet.removeAt(setIndex)
        weightBySet.removeAt(setIndex)

        dao.updateExercise(
            ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                sets = exercise.sets - 1,
                reps = repsBySet.firstOrNull() ?: exercise.reps,
                intervalSeconds = exercise.intervalSeconds,
                plannedWeight = weightBySet.firstOrNull() ?: exercise.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(repsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(weightBySet),
                remarks = exercise.remarks,
                isDone = exercise.isDone
            )
        )
    }

    suspend fun updateExerciseSetPlan(
        exercise: ExerciseModel,
        setIndex: Int,
        plannedReps: Int? = null,
        plannedWeight: String? = null
    ) {
        if (setIndex !in 0 until exercise.sets) {
            return
        }

        val updatedRepsBySet = exercise.plannedRepsBySet
            .normalizeRepsBySet(expectedSets = exercise.sets, fallbackValue = exercise.reps)
            .toMutableList()
        val updatedWeightBySet = exercise.plannedWeightBySet
            .normalizeWeightBySet(expectedSets = exercise.sets, fallbackValue = exercise.plannedWeight)
            .toMutableList()

        plannedReps?.let { value ->
            updatedRepsBySet[setIndex] = value.coerceIn(MIN_REPS, MAX_REPS)
        }
        plannedWeight?.let { value ->
            updatedWeightBySet[setIndex] = value.trim()
        }

        dao.updateExercise(
            ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                sets = exercise.sets,
                reps = updatedRepsBySet.firstOrNull() ?: exercise.reps,
                intervalSeconds = exercise.intervalSeconds,
                plannedWeight = updatedWeightBySet.firstOrNull() ?: exercise.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(updatedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(updatedWeightBySet),
                remarks = exercise.remarks,
                isDone = exercise.isDone
            )
        )
    }

    // Unassigns an exercise from one day (used by the day edit screen's delete action).
    // The exercise entity itself is NOT deleted - it stays linked to any other days it's
    // on, or falls into the unassigned pool if this was its only day.
    suspend fun unassignExerciseFromDay(dayNumber: Int, exerciseId: Long) {
        dao.deleteLink(exerciseId, dayNumber)
        normalizePositions(dayNumber)
    }

    // Permanently deletes an exercise (and all its day links/history FK) - used from the
    // unassigned pool, not from a day's edit screen (which only unassigns).
    suspend fun deleteExercisePermanently(exerciseId: Long) {
        dao.getExerciseById(exerciseId)?.let { dao.deleteExercise(it) }
    }

    suspend fun assignExerciseToDay(exerciseId: Long, dayNumber: Int) {
        val nextPosition = (dao.getLinksForDay(dayNumber).maxOfOrNull { it.position } ?: 0) + 1
        dao.insertLink(ExerciseDayLinkEntity(exerciseId = exerciseId, dayNumber = dayNumber, position = nextPosition))
    }

    // Bulk "which days is this on" update - adds missing links (appended) and removes
    // links no longer in dayNumbers, renormalizing each affected day's positions.
    suspend fun setExerciseDayAssignments(exerciseId: Long, dayNumbers: Set<Int>) {
        val currentDayNumbers = dao.getLinksForExercise(exerciseId).map { it.dayNumber }.toSet()
        val toRemove = currentDayNumbers - dayNumbers
        val toAdd = dayNumbers - currentDayNumbers

        toRemove.forEach { dayNumber -> dao.deleteLink(exerciseId, dayNumber) }
        toAdd.forEach { dayNumber -> assignExerciseToDay(exerciseId, dayNumber) }
        toRemove.forEach { dayNumber -> normalizePositions(dayNumber) }
    }

    suspend fun setWorkoutDone(dayNumber: Int, plannedDateEpochDay: Long, isDone: Boolean) {
        dao.updateWorkoutDone(
            dayNumber = dayNumber,
            completedForDateEpochDay = if (isDone) plannedDateEpochDay else null
        )
    }

    // Swaps two days' workout identity (name, assigned exercises, logged history) while
    // each day's own dayNumber/plannedDateEpochDay slot stays fixed - "Day N" labels simply
    // auto-realign since the content moved, not the slot numbering itself.
    suspend fun swapDays(dayNumberA: Int, dayNumberB: Int) {
        if (dayNumberA == dayNumberB) return
        val days = dao.getAllDays()
        val dayA = days.firstOrNull { it.dayNumber == dayNumberA } ?: return
        val dayB = days.firstOrNull { it.dayNumber == dayNumberB } ?: return
        dao.swapDays(dayNumberA, dayNumberB, workoutNameForA = dayB.workoutName, workoutNameForB = dayA.workoutName)
        normalizePositions(dayNumberA)
        normalizePositions(dayNumberB)
    }

    // Moves a day up/down by one position in the dayNumber-ordered sequence, swapping it
    // with its neighbor (used by the day-focus reorder UI's move up/down controls).
    suspend fun moveDay(dayNumber: Int, moveBy: Int) {
        val ordered = dao.getAllDays().sortedBy { it.dayNumber }
        val fromIndex = ordered.indexOfFirst { it.dayNumber == dayNumber }
        if (fromIndex < 0) return
        val targetIndex = fromIndex + moveBy
        if (targetIndex !in ordered.indices) return
        swapDays(ordered[fromIndex].dayNumber, ordered[targetIndex].dayNumber)
    }

    suspend fun moveExercise(dayNumber: Int, exerciseId: Long, moveBy: Int) {
        val links = dao.getLinksForDay(dayNumber).sortedBy { it.position }.toMutableList()
        val fromIndex = links.indexOfFirst { it.exerciseId == exerciseId }
        if (fromIndex < 0) {
            return
        }

        val targetIndex = fromIndex + moveBy
        if (targetIndex !in links.indices) {
            return
        }

        val fromItem = links[fromIndex]
        links[fromIndex] = links[targetIndex]
        links[targetIndex] = fromItem

        links.forEachIndexed { index, link ->
            val expected = index + 1
            if (link.position != expected) {
                dao.updateLinkPosition(link.exerciseId, dayNumber, expected)
            }
        }
    }

    // Restore a day's template to a pre-edit snapshot (used to discard live edits).
    // Exercises linked during the edit (present now, absent in the snapshot) are unlinked
    // from this day only (never deleted - they may still belong to other days, or just
    // fall back into the pool); surviving exercises are updated in place (same id) and
    // re-linked at their snapshot position; exercises whose entity was permanently deleted
    // during the edit are re-created (fresh id, but past history keeps its exerciseName).
    suspend fun restoreDayExercises(dayNumber: Int, snapshot: List<ExerciseModel>) {
        val currentLinks = dao.getLinksForDay(dayNumber)
        val snapshotIds = snapshot.mapTo(HashSet()) { it.id }

        currentLinks.filter { link -> link.exerciseId !in snapshotIds }.forEach { link ->
            dao.deleteLink(link.exerciseId, dayNumber)
        }

        snapshot.forEach { model ->
            val repsBySet = model.plannedRepsBySet
                .normalizeRepsBySet(expectedSets = model.sets, fallbackValue = model.reps)
            val weightBySet = model.plannedWeightBySet
                .normalizeWeightBySet(expectedSets = model.sets, fallbackValue = model.plannedWeight)
            val existingEntity = dao.getExerciseById(model.id)
            val entity = ExerciseEntity(
                id = existingEntity?.id ?: 0L,
                name = model.name,
                sets = model.sets,
                reps = repsBySet.firstOrNull() ?: model.reps,
                intervalSeconds = model.intervalSeconds,
                plannedWeight = weightBySet.firstOrNull() ?: model.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(repsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(weightBySet),
                remarks = model.remarks,
                isDone = model.isDone
            )
            val exerciseId = if (existingEntity != null) {
                dao.updateExercise(entity)
                existingEntity.id
            } else {
                dao.insertExercise(entity)
            }
            dao.insertLink(ExerciseDayLinkEntity(exerciseId = exerciseId, dayNumber = dayNumber, position = model.position))
        }

        normalizePositions(dayNumber)
    }

    suspend fun startSession(day: WorkoutDayModel, advancesCycle: Boolean = true): Long {
        return dao.insertSession(
            WorkoutSessionEntity(
                dayNumber = day.dayNumber,
                workoutName = day.workoutName,
                startedAt = System.currentTimeMillis(),
                finishedAt = null,
                advancesCycle = advancesCycle
            )
        )
    }

    suspend fun logSet(
        sessionId: Long,
        exercise: ExerciseModel,
        setNumber: Int,
        actualReps: Int,
        actualWeight: String
    ) {
        val setIndex = (setNumber - 1).coerceAtLeast(0)
        val plannedReps = exercise.plannedRepsBySet
            .getOrElse(setIndex) { exercise.reps }
            .coerceIn(MIN_REPS, MAX_REPS)
        val plannedWeight = exercise.plannedWeightBySet
            .getOrElse(setIndex) { exercise.plannedWeight }

        dao.insertSetLog(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                setNumber = setNumber,
                plannedReps = plannedReps,
                actualReps = actualReps.coerceIn(0, MAX_REPS),
                plannedWeight = plannedWeight,
                actualWeight = actualWeight.trim(),
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun finishSession(sessionId: Long) {
        val finishedAt = System.currentTimeMillis()
        dao.finishSession(sessionId, finishedAt)

        // One workout per calendar day: the latest finished session overwrites any
        // earlier finished session recorded for the same day (its set logs cascade away).
        val finishedDay = timestampMillisToLocalEpochDay(finishedAt)
        dao.getAllSessions()
            .filter { other ->
                other.id != sessionId &&
                    other.finishedAt != null &&
                    timestampMillisToLocalEpochDay(other.finishedAt) == finishedDay
            }
            .forEach { stale -> dao.deleteSessionById(stale.id) }
    }

    // Abandon an in-progress session: delete the session (its set logs cascade away) so a
    // workout that was exited rather than completed never counts as done.
    suspend fun abandonSession(sessionId: Long) {
        dao.deleteSessionById(sessionId)
    }

    // Records a completed workout on a past (or any) date using its planned reps/weights.
    // Enforces one workout per day by replacing anything already on that date.
    // `advancesCycle = false` logs it as a substitute for the day that's actually due
    // (e.g. doing day 5's workout instead of the due day 3), so it counts toward the
    // streak but the due day stays owed instead of the cycle jumping past it.
    suspend fun logBackdatedWorkout(day: WorkoutDayModel, dateEpochDay: Long, advancesCycle: Boolean = true) {
        removeWorkoutOnDate(dateEpochDay)
        val whenMillis = epochDayToLocalMiddayMillis(dateEpochDay)
        val sessionId = dao.insertSession(
            WorkoutSessionEntity(
                dayNumber = day.dayNumber,
                workoutName = day.workoutName,
                startedAt = whenMillis,
                finishedAt = whenMillis,
                advancesCycle = advancesCycle
            )
        )
        day.exercises.forEach { exercise ->
            val reps = exercise.plannedRepsBySet
            val weights = exercise.plannedWeightBySet
            for (setIndex in 0 until exercise.sets) {
                dao.insertSetLog(
                    SetLogEntity(
                        sessionId = sessionId,
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        setNumber = setIndex + 1,
                        plannedReps = reps.getOrElse(setIndex) { exercise.reps },
                        actualReps = reps.getOrElse(setIndex) { exercise.reps },
                        plannedWeight = weights.getOrElse(setIndex) { exercise.plannedWeight },
                        actualWeight = weights.getOrElse(setIndex) { exercise.plannedWeight },
                        loggedAt = whenMillis + (setIndex + 1) * 1000L
                    )
                )
            }
        }
    }

    // Records a one-off activity that isn't on the schedule at all (e.g. "Badminton").
    // Never advances the cycle: `fallbackDayNumber` (the day that's actually due) is only
    // stored to satisfy the dayNumber FK, so that day stays owed for next time.
    suspend fun logCustomActivity(name: String, fallbackDayNumber: Int, dateEpochDay: Long) {
        removeWorkoutOnDate(dateEpochDay)
        val whenMillis = epochDayToLocalMiddayMillis(dateEpochDay)
        dao.insertSession(
            WorkoutSessionEntity(
                dayNumber = fallbackDayNumber,
                workoutName = name,
                startedAt = whenMillis,
                finishedAt = whenMillis,
                advancesCycle = false,
                isCustomActivity = true
            )
        )
    }

    // Clears any completed workout recorded on the given date.
    suspend fun removeWorkoutOnDate(dateEpochDay: Long) {
        dao.getAllSessions()
            .filter { session ->
                val finishedAt = session.finishedAt
                finishedAt != null && timestampMillisToLocalEpochDay(finishedAt) == dateEpochDay
            }
            .forEach { session -> dao.deleteSessionById(session.id) }
        dao.clearWorkoutDoneForDate(dateEpochDay)
    }

    private fun epochDayToLocalMiddayMillis(dateEpochDay: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = dateEpochDay * MILLIS_PER_DAY
        val local = Calendar.getInstance()
        local.clear()
        local.set(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH),
            12,
            0,
            0
        )
        local.set(Calendar.MILLISECOND, 0)
        return local.timeInMillis
    }

    suspend fun updateSetLogEntry(logId: Long, actualReps: Int, actualWeight: String) {
        dao.updateSetLogEntry(
            logId = logId,
            actualReps = actualReps.coerceIn(MIN_REPS, MAX_REPS),
            actualWeight = actualWeight.trim()
        )
    }

    suspend fun deleteSetLogEntry(logId: Long, sessionId: Long) {
        val session = dao.getSessionById(sessionId)
        dao.deleteSetLogEntry(logId)
        dao.deleteSessionIfNoLogs(sessionId)
        clearWorkoutDoneIfSessionRemoved(session)
    }

    suspend fun deleteExerciseHistoryForSession(sessionId: Long, exerciseId: Long) {
        val session = dao.getSessionById(sessionId)
        dao.deleteExerciseLogsForSession(
            sessionId = sessionId,
            exerciseId = exerciseId
        )
        dao.deleteSessionIfNoLogs(sessionId)
        clearWorkoutDoneIfSessionRemoved(session)
    }

    // Removes an exercise's logs within an active session without touching the session
    // itself — used to replace prior logs when re-logging or un-skipping.
    suspend fun clearExerciseLogsForSession(sessionId: Long, exerciseId: Long) {
        dao.deleteExerciseLogsForSession(
            sessionId = sessionId,
            exerciseId = exerciseId
        )
    }

    private suspend fun clearWorkoutDoneIfSessionRemoved(session: WorkoutSessionEntity?) {
        // When a finished session is fully removed, also clear the matching
        // "workout done" mark on its template day so the green tick clears too.
        val finishedAt = session?.finishedAt ?: return
        if (dao.getSessionById(session.id) != null) {
            return
        }
        val finishedDay = timestampMillisToLocalEpochDay(finishedAt)
        dao.clearWorkoutDoneForDay(session.dayNumber, finishedDay)
    }

    suspend fun exportBackupSnapshot(): BackupSnapshot {
        return BackupSnapshot(
            days = dao.getAllDays().sortedBy { it.dayNumber },
            exercises = dao.getAllExercises(),
            sessions = dao.getAllSessions(),
            logs = dao.getAllSetLogs(),
            links = dao.getAllLinks()
        )
    }

    suspend fun importBackupSnapshot(snapshot: BackupSnapshot) {
        dao.replaceAllData(
            days = snapshot.days.sortedBy { it.dayNumber },
            exercises = snapshot.exercises.sortedBy { it.id },
            sessions = snapshot.sessions.sortedBy { it.id },
            logs = snapshot.logs.sortedBy { it.id },
            links = snapshot.links.sortedWith(compareBy(ExerciseDayLinkEntity::dayNumber, ExerciseDayLinkEntity::position))
        )
    }

    private suspend fun normalizePositions(dayNumber: Int) {
        dao.getLinksForDay(dayNumber)
            .sortedBy { it.position }
            .forEachIndexed { index, link ->
                val expected = index + 1
                if (link.position != expected) {
                    dao.updateLinkPosition(link.exerciseId, dayNumber, expected)
                }
            }
    }
}

private fun ExerciseEntity.toModel(position: Int, dayNumbers: Set<Int>): ExerciseModel {
    val normalizedRepsBySet = decodeRepsBySetJson(plannedRepsBySetJson)
        .normalizeRepsBySet(expectedSets = sets, fallbackValue = reps)
    val normalizedWeightBySet = decodeWeightBySetJson(plannedWeightBySetJson)
        .normalizeWeightBySet(expectedSets = sets, fallbackValue = plannedWeight)

    return ExerciseModel(
        id = id,
        name = name,
        sets = sets,
        reps = normalizedRepsBySet.firstOrNull() ?: reps,
        intervalSeconds = intervalSeconds,
        plannedWeight = normalizedWeightBySet.firstOrNull() ?: plannedWeight,
        plannedRepsBySet = normalizedRepsBySet,
        plannedWeightBySet = normalizedWeightBySet,
        remarks = remarks,
        isDone = isDone,
        position = position,
        dayNumbers = dayNumbers
    )
}

private fun ExerciseDraft.sanitized(): ExerciseDraft {
    val cleanName = name.trim().ifEmpty { "New Exercise" }
    return copy(
        name = cleanName,
        sets = sets.coerceIn(MIN_SETS, MAX_SETS),
        reps = reps.coerceIn(MIN_REPS, MAX_REPS),
        intervalSeconds = intervalSeconds.coerceAtLeast(0),
        plannedWeight = plannedWeight.trim(),
        remarks = remarks.trim()
    )
}

private data class SeedExercise(val dayNumber: Int, val position: Int, val entity: ExerciseEntity)

private fun seedExercise(
    dayNumber: Int,
    position: Int,
    name: String,
    sets: Int,
    reps: Int,
    intervalSeconds: Int,
    plannedWeight: String = "",
    remarks: String = ""
): SeedExercise {
    val normalizedSets = sets.coerceIn(MIN_SETS, MAX_SETS)
    val normalizedReps = reps.coerceIn(MIN_REPS, MAX_REPS)
    val normalizedWeight = plannedWeight.trim()

    val entity = ExerciseEntity(
        name = name,
        sets = normalizedSets,
        reps = normalizedReps,
        intervalSeconds = intervalSeconds.coerceAtLeast(0),
        plannedWeight = normalizedWeight,
        plannedRepsBySetJson = encodeRepsBySetJson(List(normalizedSets) { normalizedReps }),
        plannedWeightBySetJson = encodeWeightBySetJson(List(normalizedSets) { normalizedWeight }),
        remarks = remarks,
        isDone = false
    )
    return SeedExercise(dayNumber = dayNumber, position = position, entity = entity)
}

private fun encodeRepsBySetJson(values: List<Int>): String {
    return JSONArray(values.map { value -> value.coerceIn(MIN_REPS, MAX_REPS) }).toString()
}

private fun encodeWeightBySetJson(values: List<String>): String {
    return JSONArray(values.map { value -> value.trim() }).toString()
}

private fun decodeRepsBySetJson(rawJson: String): List<Int> {
    if (rawJson.isBlank()) {
        return emptyList()
    }

    return runCatching {
        val array = JSONArray(rawJson)
        buildList {
            for (index in 0 until array.length()) {
                add(array.optInt(index, 0))
            }
        }
    }.getOrDefault(emptyList())
}

private fun decodeWeightBySetJson(rawJson: String): List<String> {
    if (rawJson.isBlank()) {
        return emptyList()
    }

    return runCatching {
        val array = JSONArray(rawJson)
        buildList {
            for (index in 0 until array.length()) {
                add(array.optString(index, ""))
            }
        }
    }.getOrDefault(emptyList())
}

private fun List<Int>.normalizeRepsBySet(expectedSets: Int, fallbackValue: Int): List<Int> {
    val safeSetCount = expectedSets.coerceIn(MIN_SETS, MAX_SETS)
    val fallback = fallbackValue.coerceIn(MIN_REPS, MAX_REPS)

    return List(safeSetCount) { index ->
        getOrNull(index)?.coerceIn(MIN_REPS, MAX_REPS) ?: fallback
    }
}

private fun List<String>.normalizeWeightBySet(expectedSets: Int, fallbackValue: String): List<String> {
    val safeSetCount = expectedSets.coerceIn(MIN_SETS, MAX_SETS)
    val fallback = fallbackValue.trim()

    return List(safeSetCount) { index ->
        getOrNull(index)?.trim() ?: fallback
    }
}

private fun currentDateEpochDay(): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}

private fun timestampMillisToLocalEpochDay(timestampMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}
