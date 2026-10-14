package com.example.workoutassist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM template_days ORDER BY dayNumber ASC")
    fun observeDays(): Flow<List<TemplateDayEntity>>

    @Query("SELECT * FROM exercises ORDER BY id ASC")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise_day_links ORDER BY dayNumber ASC, position ASC")
    fun observeLinks(): Flow<List<ExerciseDayLinkEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY id DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM set_logs ORDER BY id DESC")
    fun observeSetLogs(): Flow<List<SetLogEntity>>

    @Query("SELECT COUNT(*) FROM template_days")
    suspend fun countDays(): Int

    @Query("SELECT * FROM template_days ORDER BY dayNumber ASC")
    suspend fun getAllDays(): List<TemplateDayEntity>

    @Query("SELECT * FROM exercises ORDER BY id ASC")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercise_day_links ORDER BY dayNumber ASC, position ASC")
    suspend fun getAllLinks(): List<ExerciseDayLinkEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY id ASC")
    suspend fun getAllSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM set_logs ORDER BY id ASC")
    suspend fun getAllSetLogs(): List<SetLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDays(days: List<TemplateDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetLogs(logs: List<SetLogEntity>)

    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN exercise_day_links l ON l.exerciseId = e.id
        WHERE l.dayNumber = :dayNumber
        ORDER BY l.position ASC
    """)
    suspend fun getExercisesForDay(dayNumber: Int): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity?

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercise_day_links WHERE dayNumber = :dayNumber ORDER BY position ASC")
    suspend fun getLinksForDay(dayNumber: Int): List<ExerciseDayLinkEntity>

    @Query("SELECT * FROM exercise_day_links WHERE exerciseId = :exerciseId")
    suspend fun getLinksForExercise(exerciseId: Long): List<ExerciseDayLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: ExerciseDayLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<ExerciseDayLinkEntity>)

    @Query("DELETE FROM exercise_day_links WHERE exerciseId = :exerciseId AND dayNumber = :dayNumber")
    suspend fun deleteLink(exerciseId: Long, dayNumber: Int): Int

    @Query("DELETE FROM exercise_day_links WHERE exerciseId = :exerciseId")
    suspend fun deleteLinksForExercise(exerciseId: Long): Int

    @Query("DELETE FROM exercise_day_links")
    suspend fun deleteAllLinks()

    @Query("UPDATE exercise_day_links SET position = :position WHERE exerciseId = :exerciseId AND dayNumber = :dayNumber")
    suspend fun updateLinkPosition(exerciseId: Long, dayNumber: Int, position: Int): Int

    @Query("SELECT * FROM exercises WHERE id NOT IN (SELECT exerciseId FROM exercise_day_links) ORDER BY id ASC")
    suspend fun getUnassignedExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE id NOT IN (SELECT exerciseId FROM exercise_day_links) ORDER BY id ASC")
    fun observeUnassignedExercises(): Flow<List<ExerciseEntity>>

    @Query("UPDATE template_days SET workoutName = :workoutName WHERE dayNumber = :dayNumber")
    suspend fun updateWorkoutName(dayNumber: Int, workoutName: String): Int

    // Keep past sessions' recorded name in sync with a template rename, so Insights history
    // (grouped by workoutName) doesn't fragment into an old-name and new-name entry.
    @Query("UPDATE workout_sessions SET workoutName = :workoutName WHERE dayNumber = :dayNumber")
    suspend fun renameSessionsForDay(dayNumber: Int, workoutName: String): Int

    @Query("UPDATE template_days SET completedForDateEpochDay = :completedForDateEpochDay WHERE dayNumber = :dayNumber")
    suspend fun updateWorkoutDone(dayNumber: Int, completedForDateEpochDay: Long?): Int

    // Re-points logged session history between two days in one atomic statement (a plain
    // two-step update would transiently violate the dayNumber foreign key). The template
    // days' own dayNumber/plannedDateEpochDay rows are never touched - only which day's
    // history each session belongs to.
    @Query(
        """
        UPDATE workout_sessions
        SET dayNumber = CASE WHEN dayNumber = :dayNumberA THEN :dayNumberB ELSE :dayNumberA END
        WHERE dayNumber IN (:dayNumberA, :dayNumberB)
        """
    )
    suspend fun swapSessionDays(dayNumberA: Int, dayNumberB: Int): Int

    // Same idea for exercise assignments, but an exercise linked to BOTH days is excluded
    // from the swap (it stays on both either way) since touching both its rows in one
    // statement could collide on the (exerciseId, dayNumber) primary key.
    @Query(
        """
        UPDATE exercise_day_links
        SET dayNumber = CASE WHEN dayNumber = :dayNumberA THEN :dayNumberB ELSE :dayNumberA END
        WHERE dayNumber IN (:dayNumberA, :dayNumberB)
          AND exerciseId NOT IN (
              SELECT exerciseId FROM exercise_day_links WHERE dayNumber = :dayNumberA
              INTERSECT
              SELECT exerciseId FROM exercise_day_links WHERE dayNumber = :dayNumberB
          )
        """
    )
    suspend fun swapLinkDays(dayNumberA: Int, dayNumberB: Int): Int

    // Swaps two days' workout identity (name, assigned exercises, logged history) as one
    // atomic unit - used by the day-reorder UI. dayNumber itself (the primary key both
    // workout_sessions and exercise_day_links cascade-delete off of) is never reassigned;
    // only the content attached to each fixed dayNumber slot moves.
    @Transaction
    suspend fun swapDays(dayNumberA: Int, dayNumberB: Int, workoutNameForA: String, workoutNameForB: String) {
        updateWorkoutName(dayNumberA, workoutNameForA)
        updateWorkoutName(dayNumberB, workoutNameForB)
        swapSessionDays(dayNumberA, dayNumberB)
        swapLinkDays(dayNumberA, dayNumberB)
    }

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET finishedAt = :finishedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, finishedAt: Long): Int

    @Insert
    suspend fun insertSetLog(log: SetLogEntity)

    @Query("UPDATE set_logs SET actualReps = :actualReps, actualWeight = :actualWeight WHERE id = :logId")
    suspend fun updateSetLogEntry(logId: Long, actualReps: Int, actualWeight: String): Int

    @Query("DELETE FROM set_logs WHERE id = :logId")
    suspend fun deleteSetLogEntry(logId: Long): Int

    @Query("DELETE FROM set_logs WHERE sessionId = :sessionId AND exerciseId = :exerciseId")
    suspend fun deleteExerciseLogsForSession(sessionId: Long, exerciseId: Long): Int

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId AND NOT EXISTS (SELECT 1 FROM set_logs WHERE sessionId = :sessionId)")
    suspend fun deleteSessionIfNoLogs(sessionId: Long): Int

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long): Int

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Query("UPDATE template_days SET completedForDateEpochDay = null WHERE dayNumber = :dayNumber AND completedForDateEpochDay = :dateEpochDay")
    suspend fun clearWorkoutDoneForDay(dayNumber: Int, dateEpochDay: Long): Int

    @Query("UPDATE template_days SET completedForDateEpochDay = null WHERE completedForDateEpochDay = :dateEpochDay")
    suspend fun clearWorkoutDoneForDate(dateEpochDay: Long): Int

    @Query("DELETE FROM set_logs")
    suspend fun deleteAllSetLogs()

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    @Query("DELETE FROM template_days")
    suspend fun deleteAllDays()

    @Transaction
    suspend fun replaceAllData(
        days: List<TemplateDayEntity>,
        exercises: List<ExerciseEntity>,
        sessions: List<WorkoutSessionEntity>,
        logs: List<SetLogEntity>,
        links: List<ExerciseDayLinkEntity>
    ) {
        deleteAllSetLogs()
        deleteAllSessions()
        deleteAllLinks()
        deleteAllExercises()
        deleteAllDays()

        if (days.isNotEmpty()) {
            upsertDays(days)
        }
        if (exercises.isNotEmpty()) {
            upsertExercises(exercises)
        }
        if (links.isNotEmpty()) {
            insertLinks(links)
        }
        if (sessions.isNotEmpty()) {
            upsertSessions(sessions)
        }
        if (logs.isNotEmpty()) {
            upsertSetLogs(logs)
        }
    }
}
