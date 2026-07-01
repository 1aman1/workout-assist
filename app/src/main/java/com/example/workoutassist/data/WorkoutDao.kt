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
    @Transaction
    @Query("SELECT * FROM template_days ORDER BY dayNumber ASC")
    fun observeDaysWithExercises(): Flow<List<DayWithExercises>>

    @Query("SELECT * FROM workout_sessions ORDER BY id DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COUNT(*) FROM template_days")
    suspend fun countDays(): Int

    @Query("SELECT * FROM template_days ORDER BY dayNumber ASC")
    suspend fun getAllDays(): List<TemplateDayEntity>

    @Query("SELECT * FROM exercises ORDER BY dayNumber ASC, position ASC")
    suspend fun getAllExercises(): List<ExerciseEntity>

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

    @Insert
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises WHERE dayNumber = :dayNumber ORDER BY position ASC")
    suspend fun getExercisesForDay(dayNumber: Int): List<ExerciseEntity>

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET position = :position WHERE id = :exerciseId")
    suspend fun updateExercisePosition(exerciseId: Long, position: Int): Int

    @Query("UPDATE template_days SET workoutName = :workoutName WHERE dayNumber = :dayNumber")
    suspend fun updateWorkoutName(dayNumber: Int, workoutName: String): Int

    @Query("UPDATE template_days SET plannedDateEpochDay = :plannedDateEpochDay WHERE dayNumber = :dayNumber")
    suspend fun updatePlannedDate(dayNumber: Int, plannedDateEpochDay: Long): Int

    @Query("UPDATE template_days SET completedForDateEpochDay = :completedForDateEpochDay WHERE dayNumber = :dayNumber")
    suspend fun updateWorkoutDone(dayNumber: Int, completedForDateEpochDay: Long?): Int

    @Query("UPDATE exercises SET isDone = :isDone WHERE id = :exerciseId")
    suspend fun updateExerciseDone(exerciseId: Long, isDone: Boolean): Int

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET finishedAt = :finishedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, finishedAt: Long): Int

    @Insert
    suspend fun insertSetLog(log: SetLogEntity)

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
        logs: List<SetLogEntity>
    ) {
        deleteAllSetLogs()
        deleteAllSessions()
        deleteAllExercises()
        deleteAllDays()

        if (days.isNotEmpty()) {
            upsertDays(days)
        }
        if (exercises.isNotEmpty()) {
            upsertExercises(exercises)
        }
        if (sessions.isNotEmpty()) {
            upsertSessions(sessions)
        }
        if (logs.isNotEmpty()) {
            upsertSetLogs(logs)
        }
    }
}
