package com.example.workoutassist.data

import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private const val MIN_SETS = 1
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
    val dayNumber: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val plannedRepsBySet: List<Int>,
    val plannedWeightBySet: List<String>,
    val remarks: String,
    val position: Int,
    val isDone: Boolean
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
    val logs: List<SetLogEntity>
)

class WorkoutRepository(private val dao: WorkoutDao) {
    fun observeDays(): Flow<List<WorkoutDayModel>> {
        return dao.observeDaysWithExercises().map { rows ->
            rows.map { row ->
                WorkoutDayModel(
                    dayNumber = row.day.dayNumber,
                    workoutName = row.day.workoutName,
                    plannedDateEpochDay = row.day.plannedDateEpochDay,
                    completedForDateEpochDay = row.day.completedForDateEpochDay,
                    isCompleted = row.day.completedForDateEpochDay == row.day.plannedDateEpochDay,
                    exercises = row.exercises
                        .sortedBy { it.position }
                        .map { it.toModel() }
                )
            }
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
            logs = emptyList()
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

        dao.insertExercises(
            listOf(
                seedExercise(1, 1, "Barbell Bench Press (Main Lift)", 3, 6, 180, "60-65 kg"),
                seedExercise(1, 2, "Incline Barbell Press", 3, 6, 150, "50-55 kg"),
                seedExercise(1, 3, "Chest Dips (Lean Forward)", 3, 8, 120, "Bodyweight"),
                seedExercise(1, 4, "Triceps (Skullcrusher or Rope Pulldown)", 3, 10, 90),
                seedExercise(1, 5, "Finish Crunches", 3, 15, 45, "Bodyweight"),

                seedExercise(2, 1, "Deadlift (Main Lift)", 3, 5, 180, "Technique focus"),
                seedExercise(2, 2, "Barbell Row", 3, 6, 150, "45-55 kg"),
                seedExercise(2, 3, "Pull-Ups or Lat Pulldown", 3, 8, 120),
                seedExercise(2, 4, "Biceps Barbell Curl", 3, 8, 90, "20-30 kg"),
                seedExercise(2, 5, "Finish Crunches", 3, 15, 45, "Bodyweight"),

                seedExercise(4, 1, "Overhead Press", 3, 6, 150, "15-20 kg"),
                seedExercise(4, 2, "Lateral Raises", 4, 12, 75, "7.5 kg"),
                seedExercise(4, 3, "Rear Delt Raises", 3, 12, 75, "20-30 kg"),
                seedExercise(4, 4, "Arms Superset (DB Curl + Triceps Pushdown)", 3, 12, 60),
                seedExercise(4, 5, "Finish Crunches", 3, 15, 45, "Bodyweight"),

                seedExercise(5, 1, "Incline Dumbbell Press (30 deg)", 3, 12, 90, "15-17.5 kg"),
                seedExercise(5, 2, "Machine/Cable Chest Fly", 3, 12, 60, "35 kg"),
                seedExercise(5, 3, "Lat Pulldown", 3, 12, 90, "38-45 kg"),
                seedExercise(5, 4, "Seated Cable Row", 3, 12, 90, "38-45 kg"),
                seedExercise(5, 5, "Arm Superset (Biceps + Triceps)", 3, 12, 60),
                seedExercise(5, 6, "Finish Crunches", 3, 15, 45, "Bodyweight"),

                seedExercise(6, 1, "Barbell Squat", 3, 6, 150, "50-80 kg"),
                seedExercise(6, 2, "Romanian Deadlift", 2, 8, 120),
                seedExercise(6, 3, "Calf Raises", 3, 15, 60),
                seedExercise(6, 4, "Finish Crunches", 3, 15, 45, "Bodyweight")
            )
        )
    }

    suspend fun renameWorkout(dayNumber: Int, workoutName: String) {
        val clean = workoutName.trim().ifEmpty { "Day $dayNumber Workout" }
        dao.updateWorkoutName(dayNumber, clean)
    }

    suspend fun addExercise(dayNumber: Int, draft: ExerciseDraft) {
        val clean = draft.sanitized()
        val existing = dao.getExercisesForDay(dayNumber)
        val nextPosition = (existing.maxOfOrNull { it.position } ?: 0) + 1
        val plannedRepsBySet = List(clean.sets) { clean.reps }
        val plannedWeightBySet = List(clean.sets) { clean.plannedWeight }

        dao.insertExercise(
            ExerciseEntity(
                dayNumber = dayNumber,
                name = clean.name,
                sets = clean.sets,
                reps = clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = clean.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(plannedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(plannedWeightBySet),
                remarks = clean.remarks,
                position = nextPosition,
                isDone = false
            )
        )
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
                dayNumber = exercise.dayNumber,
                name = clean.name,
                sets = clean.sets,
                reps = plannedRepsBySet.firstOrNull() ?: clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = plannedWeightBySet.firstOrNull() ?: clean.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(plannedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(plannedWeightBySet),
                remarks = clean.remarks,
                position = exercise.position,
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
                dayNumber = exercise.dayNumber,
                name = exercise.name,
                sets = exercise.sets,
                reps = updatedRepsBySet.firstOrNull() ?: exercise.reps,
                intervalSeconds = exercise.intervalSeconds,
                plannedWeight = updatedWeightBySet.firstOrNull() ?: exercise.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(updatedRepsBySet),
                plannedWeightBySetJson = encodeWeightBySetJson(updatedWeightBySet),
                remarks = exercise.remarks,
                position = exercise.position,
                isDone = exercise.isDone
            )
        )
    }

    suspend fun deleteExercise(exercise: ExerciseModel) {
        dao.deleteExercise(
            ExerciseEntity(
                id = exercise.id,
                dayNumber = exercise.dayNumber,
                name = exercise.name,
                sets = exercise.sets,
                reps = exercise.reps,
                intervalSeconds = exercise.intervalSeconds,
                plannedWeight = exercise.plannedWeight,
                plannedRepsBySetJson = encodeRepsBySetJson(
                    exercise.plannedRepsBySet
                        .normalizeRepsBySet(expectedSets = exercise.sets, fallbackValue = exercise.reps)
                ),
                plannedWeightBySetJson = encodeWeightBySetJson(
                    exercise.plannedWeightBySet
                        .normalizeWeightBySet(expectedSets = exercise.sets, fallbackValue = exercise.plannedWeight)
                ),
                remarks = exercise.remarks,
                position = exercise.position,
                isDone = exercise.isDone
            )
        )
        normalizePositions(exercise.dayNumber)
    }

    suspend fun updateDayDateAndPushForward(dayNumber: Int, newDateEpochDay: Long) {
        val days = dao.getAllDays().sortedBy { it.dayNumber }
        if (days.none { it.dayNumber == dayNumber }) {
            return
        }

        for (day in days) {
            val offset = day.dayNumber - dayNumber
            dao.updatePlannedDate(day.dayNumber, newDateEpochDay + offset)
        }
    }

    suspend fun setExerciseDone(exerciseId: Long, isDone: Boolean) {
        dao.updateExerciseDone(exerciseId, isDone)
    }

    suspend fun setWorkoutDone(dayNumber: Int, plannedDateEpochDay: Long, isDone: Boolean) {
        dao.updateWorkoutDone(
            dayNumber = dayNumber,
            completedForDateEpochDay = if (isDone) plannedDateEpochDay else null
        )
    }

    suspend fun moveExercise(dayNumber: Int, exerciseId: Long, moveBy: Int) {
        val exercises = dao.getExercisesForDay(dayNumber).sortedBy { it.position }.toMutableList()
        val fromIndex = exercises.indexOfFirst { it.id == exerciseId }
        if (fromIndex < 0) {
            return
        }

        val targetIndex = fromIndex + moveBy
        if (targetIndex !in exercises.indices) {
            return
        }

        val fromItem = exercises[fromIndex]
        exercises[fromIndex] = exercises[targetIndex]
        exercises[targetIndex] = fromItem

        exercises.forEachIndexed { index, exercise ->
            val expected = index + 1
            if (exercise.position != expected) {
                dao.updateExercisePosition(exercise.id, expected)
            }
        }
    }

    suspend fun startSession(day: WorkoutDayModel): Long {
        return dao.insertSession(
            WorkoutSessionEntity(
                dayNumber = day.dayNumber,
                workoutName = day.workoutName,
                startedAt = System.currentTimeMillis(),
                finishedAt = null
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
                actualReps = actualReps.coerceIn(MIN_REPS, MAX_REPS),
                plannedWeight = plannedWeight,
                actualWeight = actualWeight.trim(),
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun finishSession(sessionId: Long) {
        dao.finishSession(sessionId, System.currentTimeMillis())
    }

    suspend fun updateSetLogEntry(logId: Long, actualReps: Int, actualWeight: String) {
        dao.updateSetLogEntry(
            logId = logId,
            actualReps = actualReps.coerceIn(MIN_REPS, MAX_REPS),
            actualWeight = actualWeight.trim()
        )
    }

    suspend fun deleteSetLogEntry(logId: Long, sessionId: Long) {
        dao.deleteSetLogEntry(logId)
        dao.deleteSessionIfNoLogs(sessionId)
    }

    suspend fun deleteExerciseHistoryForSession(sessionId: Long, exerciseId: Long) {
        dao.deleteExerciseLogsForSession(
            sessionId = sessionId,
            exerciseId = exerciseId
        )
        dao.deleteSessionIfNoLogs(sessionId)
    }

    suspend fun exportBackupSnapshot(): BackupSnapshot {
        return BackupSnapshot(
            days = dao.getAllDays().sortedBy { it.dayNumber },
            exercises = dao.getAllExercises(),
            sessions = dao.getAllSessions(),
            logs = dao.getAllSetLogs()
        )
    }

    suspend fun importBackupSnapshot(snapshot: BackupSnapshot) {
        dao.replaceAllData(
            days = snapshot.days.sortedBy { it.dayNumber },
            exercises = snapshot.exercises.sortedWith(compareBy(ExerciseEntity::dayNumber, ExerciseEntity::position)),
            sessions = snapshot.sessions.sortedBy { it.id },
            logs = snapshot.logs.sortedBy { it.id }
        )
    }

    private suspend fun normalizePositions(dayNumber: Int) {
        dao.getExercisesForDay(dayNumber)
            .sortedBy { it.position }
            .forEachIndexed { index, exercise ->
                dao.updateExercisePosition(exercise.id, index + 1)
            }
    }
}

private fun ExerciseEntity.toModel(): ExerciseModel {
    val normalizedRepsBySet = decodeRepsBySetJson(plannedRepsBySetJson)
        .normalizeRepsBySet(expectedSets = sets, fallbackValue = reps)
    val normalizedWeightBySet = decodeWeightBySetJson(plannedWeightBySetJson)
        .normalizeWeightBySet(expectedSets = sets, fallbackValue = plannedWeight)

    return ExerciseModel(
        id = id,
        dayNumber = dayNumber,
        name = name,
        sets = sets,
        reps = normalizedRepsBySet.firstOrNull() ?: reps,
        intervalSeconds = intervalSeconds,
        plannedWeight = normalizedWeightBySet.firstOrNull() ?: plannedWeight,
        plannedRepsBySet = normalizedRepsBySet,
        plannedWeightBySet = normalizedWeightBySet,
        remarks = remarks,
        position = position,
        isDone = isDone
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

private fun seedExercise(
    dayNumber: Int,
    position: Int,
    name: String,
    sets: Int,
    reps: Int,
    intervalSeconds: Int,
    plannedWeight: String = "",
    remarks: String = ""
): ExerciseEntity {
    val normalizedSets = sets.coerceIn(MIN_SETS, MAX_SETS)
    val normalizedReps = reps.coerceIn(MIN_REPS, MAX_REPS)
    val normalizedWeight = plannedWeight.trim()

    return ExerciseEntity(
        dayNumber = dayNumber,
        name = name,
        sets = normalizedSets,
        reps = normalizedReps,
        intervalSeconds = intervalSeconds.coerceAtLeast(0),
        plannedWeight = normalizedWeight,
        plannedRepsBySetJson = encodeRepsBySetJson(List(normalizedSets) { normalizedReps }),
        plannedWeightBySetJson = encodeWeightBySetJson(List(normalizedSets) { normalizedWeight }),
        remarks = remarks,
        position = position,
        isDone = false
    )
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
