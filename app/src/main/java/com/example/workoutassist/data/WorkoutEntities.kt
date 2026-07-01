package com.example.workoutassist.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "template_days")
data class TemplateDayEntity(
    @PrimaryKey val dayNumber: Int,
    val workoutName: String,
    val plannedDateEpochDay: Long,
    val completedForDateEpochDay: Long? = null
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = TemplateDayEntity::class,
            parentColumns = ["dayNumber"],
            childColumns = ["dayNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayNumber")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val remarks: String = "",
    val position: Int,
    val isDone: Boolean = false
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TemplateDayEntity::class,
            parentColumns = ["dayNumber"],
            childColumns = ["dayNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayNumber")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayNumber: Int,
    val workoutName: String,
    val startedAt: Long,
    val finishedAt: Long?
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val plannedReps: Int,
    val actualReps: Int,
    val plannedWeight: String,
    val actualWeight: String,
    val loggedAt: Long
)

data class DayWithExercises(
    @Embedded val day: TemplateDayEntity,
    @Relation(
        parentColumn = "dayNumber",
        entityColumn = "dayNumber"
    )
    val exercises: List<ExerciseEntity>
)
