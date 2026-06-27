package com.example.workoutassist.data

import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
    val plannedWeight: String
)

data class ExerciseModel(
    val id: Long,
    val dayNumber: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val position: Int,
    val isDone: Boolean
)

data class WorkoutDayModel(
    val dayNumber: Int,
    val workoutName: String,
    val plannedDateEpochDay: Long,
    val isCompleted: Boolean,
    val exercises: List<ExerciseModel>
)

class WorkoutRepository(private val dao: WorkoutDao) {
    fun observeDays(): Flow<List<WorkoutDayModel>> {
        return dao.observeDaysWithExercises().map { rows ->
            rows.map { row ->
                WorkoutDayModel(
                    dayNumber = row.day.dayNumber,
                    workoutName = row.day.workoutName,
                    plannedDateEpochDay = row.day.plannedDateEpochDay,
                    isCompleted = row.day.completedForDateEpochDay == row.day.plannedDateEpochDay,
                    exercises = row.exercises
                        .sortedBy { it.position }
                        .map { it.toModel() }
                )
            }
        }
    }

    suspend fun ensureSeedData() {
        if (dao.countDays() > 0) {
            return
        }

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
        dao.insertExercise(
            ExerciseEntity(
                dayNumber = dayNumber,
                name = clean.name,
                sets = clean.sets,
                reps = clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = clean.plannedWeight,
                position = nextPosition,
                isDone = false
            )
        )
    }

    suspend fun updateExercise(exercise: ExerciseModel, draft: ExerciseDraft) {
        val clean = draft.sanitized()
        dao.updateExercise(
            ExerciseEntity(
                id = exercise.id,
                dayNumber = exercise.dayNumber,
                name = clean.name,
                sets = clean.sets,
                reps = clean.reps,
                intervalSeconds = clean.intervalSeconds,
                plannedWeight = clean.plannedWeight,
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
        dao.insertSetLog(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                setNumber = setNumber,
                plannedReps = exercise.reps,
                actualReps = actualReps.coerceIn(MIN_REPS, MAX_REPS),
                plannedWeight = exercise.plannedWeight,
                actualWeight = actualWeight.trim(),
                loggedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun finishSession(sessionId: Long) {
        dao.finishSession(sessionId, System.currentTimeMillis())
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
    return ExerciseModel(
        id = id,
        dayNumber = dayNumber,
        name = name,
        sets = sets,
        reps = reps,
        intervalSeconds = intervalSeconds,
        plannedWeight = plannedWeight,
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
        plannedWeight = plannedWeight.trim()
    )
}

private fun seedExercise(
    dayNumber: Int,
    position: Int,
    name: String,
    sets: Int,
    reps: Int,
    intervalSeconds: Int,
    plannedWeight: String = ""
): ExerciseEntity {
    return ExerciseEntity(
        dayNumber = dayNumber,
        name = name,
        sets = sets.coerceIn(MIN_SETS, MAX_SETS),
        reps = reps.coerceIn(MIN_REPS, MAX_REPS),
        intervalSeconds = intervalSeconds.coerceAtLeast(0),
        plannedWeight = plannedWeight,
        position = position,
        isDone = false
    )
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
