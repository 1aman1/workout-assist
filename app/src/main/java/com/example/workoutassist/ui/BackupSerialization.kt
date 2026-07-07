package com.example.workoutassist.ui

import android.content.Context
import android.net.Uri
import com.example.workoutassist.data.BackupSnapshot
import com.example.workoutassist.data.ExerciseEntity
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.TemplateDayEntity
import com.example.workoutassist.data.WorkoutRepository
import com.example.workoutassist.data.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val BACKUP_FORMAT_VERSION = 1

internal data class ImportedAppState(
    val scheduleTitle: String
)

internal fun generateBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())
    return "workout-assist-backup-$timestamp.json"
}

internal suspend fun exportBackupToUri(
    context: Context,
    repository: WorkoutRepository,
    scheduleTitle: String,
    outputUri: Uri
) {
    val snapshot = repository.exportBackupSnapshot()
    val payload = buildBackupJson(scheduleTitle = scheduleTitle, snapshot = snapshot)

    withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openOutputStream(outputUri, "wt")
            ?: error("Unable to open destination file")
        stream.bufferedWriter().use { writer ->
            writer.write(payload)
        }
    }
}

internal suspend fun importBackupFromUri(
    context: Context,
    repository: WorkoutRepository,
    inputUri: Uri
): ImportedAppState {
    val text = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(inputUri)
            ?: error("Unable to open selected file")
        stream.bufferedReader().use { reader ->
            reader.readText()
        }
    }

    val parsed = parseBackupJson(text)
    if (parsed.snapshot.days.isEmpty()) {
        error("Backup does not include workout days")
    }

    repository.importBackupSnapshot(parsed.snapshot)
    return ImportedAppState(scheduleTitle = parsed.scheduleTitle)
}

private data class BackupPayload(
    val scheduleTitle: String,
    val snapshot: BackupSnapshot
)

private fun buildBackupJson(scheduleTitle: String, snapshot: BackupSnapshot): String {
    return JSONObject()
        .put("formatVersion", BACKUP_FORMAT_VERSION)
        .put("scheduleTitle", scheduleTitle)
        .put("exportedAt", System.currentTimeMillis())
        .put(
            "templateDays",
            JSONArray().apply {
                snapshot.days.forEach { day ->
                    put(
                        JSONObject()
                            .put("dayNumber", day.dayNumber)
                            .put("workoutName", day.workoutName)
                            .put("plannedDateEpochDay", day.plannedDateEpochDay)
                            .put("completedForDateEpochDay", day.completedForDateEpochDay)
                    )
                }
            }
        )
        .put(
            "exercises",
            JSONArray().apply {
                snapshot.exercises.forEach { exercise ->
                    put(
                        JSONObject()
                            .put("id", exercise.id)
                            .put("dayNumber", exercise.dayNumber)
                            .put("name", exercise.name)
                            .put("sets", exercise.sets)
                            .put("reps", exercise.reps)
                            .put("intervalSeconds", exercise.intervalSeconds)
                            .put("plannedWeight", exercise.plannedWeight)
                            .put("plannedRepsBySetJson", exercise.plannedRepsBySetJson)
                            .put("plannedWeightBySetJson", exercise.plannedWeightBySetJson)
                            .put("remarks", exercise.remarks)
                            .put("position", exercise.position)
                            .put("isDone", exercise.isDone)
                    )
                }
            }
        )
        .put(
            "workoutSessions",
            JSONArray().apply {
                snapshot.sessions.forEach { session ->
                    put(
                        JSONObject()
                            .put("id", session.id)
                            .put("dayNumber", session.dayNumber)
                            .put("workoutName", session.workoutName)
                            .put("startedAt", session.startedAt)
                            .put("finishedAt", session.finishedAt)
                    )
                }
            }
        )
        .put(
            "setLogs",
            JSONArray().apply {
                snapshot.logs.forEach { log ->
                    put(
                        JSONObject()
                            .put("id", log.id)
                            .put("sessionId", log.sessionId)
                            .put("exerciseId", log.exerciseId)
                            .put("exerciseName", log.exerciseName)
                            .put("setNumber", log.setNumber)
                            .put("plannedReps", log.plannedReps)
                            .put("actualReps", log.actualReps)
                            .put("plannedWeight", log.plannedWeight)
                            .put("actualWeight", log.actualWeight)
                            .put("loggedAt", log.loggedAt)
                    )
                }
            }
        )
        .toString(2)
}

private fun parseBackupJson(text: String): BackupPayload {
    val root = JSONObject(text)
    val formatVersion = root.optInt("formatVersion", 0)
    if (formatVersion <= 0) {
        error("Unsupported backup format")
    }

    val scheduleTitle = root.optString("scheduleTitle", DEFAULT_SCHEDULE_TITLE)

    val days = root.optJSONArray("templateDays").toTemplateDays()
    val exercises = root.optJSONArray("exercises").toExercises()
    val sessions = root.optJSONArray("workoutSessions").toSessions()
    val logs = root.optJSONArray("setLogs").toSetLogs()

    return BackupPayload(
        scheduleTitle = scheduleTitle,
        snapshot = BackupSnapshot(
            days = days,
            exercises = exercises,
            sessions = sessions,
            logs = logs
        )
    )
}

private fun JSONArray?.toTemplateDays(): List<TemplateDayEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val day = getJSONObject(index)
            add(
                TemplateDayEntity(
                    dayNumber = day.getInt("dayNumber"),
                    workoutName = day.optString("workoutName", "Day ${day.optInt("dayNumber", index + 1)} Workout"),
                    plannedDateEpochDay = day.getLong("plannedDateEpochDay"),
                    completedForDateEpochDay = if (day.isNull("completedForDateEpochDay")) {
                        null
                    } else {
                        day.getLong("completedForDateEpochDay")
                    }
                )
            )
        }
    }
}

private fun JSONArray?.toExercises(): List<ExerciseEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val exercise = getJSONObject(index)
            val sets = exercise.optInt("sets", 3)
            val reps = exercise.optInt("reps", 12)
            val plannedWeight = exercise.optString("plannedWeight", "")
            val plannedRepsBySetJson = exercise.optString("plannedRepsBySetJson", "")
                .ifBlank { buildRepsBySetJsonFallback(sets = sets, reps = reps) }
            val plannedWeightBySetJson = exercise.optString("plannedWeightBySetJson", "")
                .ifBlank { buildWeightBySetJsonFallback(sets = sets, plannedWeight = plannedWeight) }

            add(
                ExerciseEntity(
                    id = exercise.optLong("id", 0L),
                    dayNumber = exercise.getInt("dayNumber"),
                    name = exercise.optString("name", "Exercise"),
                    sets = sets,
                    reps = reps,
                    intervalSeconds = exercise.optInt("intervalSeconds", 90),
                    plannedWeight = plannedWeight,
                    plannedRepsBySetJson = plannedRepsBySetJson,
                    plannedWeightBySetJson = plannedWeightBySetJson,
                    remarks = exercise.optString("remarks", ""),
                    position = exercise.optInt("position", index + 1),
                    isDone = exercise.optBoolean("isDone", false)
                )
            )
        }
    }
}

private fun buildRepsBySetJsonFallback(sets: Int, reps: Int): String {
    val safeSets = sets.coerceAtLeast(1)
    return JSONArray().apply {
        repeat(safeSets) {
            put(reps)
        }
    }.toString()
}

private fun buildWeightBySetJsonFallback(sets: Int, plannedWeight: String): String {
    val safeSets = sets.coerceAtLeast(1)
    return JSONArray().apply {
        repeat(safeSets) {
            put(plannedWeight)
        }
    }.toString()
}

private fun JSONArray?.toSessions(): List<WorkoutSessionEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val session = getJSONObject(index)
            add(
                WorkoutSessionEntity(
                    id = session.optLong("id", 0L),
                    dayNumber = session.getInt("dayNumber"),
                    workoutName = session.optString("workoutName", "Workout"),
                    startedAt = session.optLong("startedAt", System.currentTimeMillis()),
                    finishedAt = if (session.isNull("finishedAt")) {
                        null
                    } else {
                        session.getLong("finishedAt")
                    }
                )
            )
        }
    }
}

private fun JSONArray?.toSetLogs(): List<SetLogEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val log = getJSONObject(index)
            add(
                SetLogEntity(
                    id = log.optLong("id", 0L),
                    sessionId = log.getLong("sessionId"),
                    exerciseId = log.optLong("exerciseId", 0L),
                    exerciseName = log.optString("exerciseName", "Exercise"),
                    setNumber = log.optInt("setNumber", 1),
                    plannedReps = log.optInt("plannedReps", 0),
                    actualReps = log.optInt("actualReps", 0),
                    plannedWeight = log.optString("plannedWeight", ""),
                    actualWeight = log.optString("actualWeight", ""),
                    loggedAt = log.optLong("loggedAt", System.currentTimeMillis())
                )
            )
        }
    }
}
