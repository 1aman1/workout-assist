package com.example.workoutassist.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "template_days")
data class TemplateDayEntity(
    @PrimaryKey val dayNumber: Int,
    val workoutName: String,
    val plannedDateEpochDay: Long,
    val completedForDateEpochDay: Long? = null
)

// Day membership/order lives in ExerciseDayLinkEntity now - an exercise can be on
// several days at once, or none (unassigned/pooled).
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sets: Int,
    val reps: Int,
    val intervalSeconds: Int,
    val plannedWeight: String,
    val plannedRepsBySetJson: String = "",
    val plannedWeightBySetJson: String = "",
    val remarks: String = "",
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
    val finishedAt: Long?,
    // False for a substitute activity logged instead of the due day (an alternate
    // template day, or a freeform one-off) - it counts toward the streak but doesn't
    // move the day-in-cycle pointer, so the originally due day stays owed for next time.
    val advancesCycle: Boolean = true,
    // True when workoutName is a freeform one-off activity, not a template day's name.
    val isCustomActivity: Boolean = false
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

// Many-to-many: one exercise can be marked onto multiple days (e.g. Treadmill on every
// day). `position` orders this exercise within this one day's list only - independent of
// any other day it's also linked to. An exercise with zero rows here is unassigned/pooled.
@Entity(
    tableName = "exercise_day_links",
    primaryKeys = ["exerciseId", "dayNumber"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TemplateDayEntity::class,
            parentColumns = ["dayNumber"],
            childColumns = ["dayNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayNumber")]
)
data class ExerciseDayLinkEntity(
    val exerciseId: Long,
    val dayNumber: Int,
    val position: Int
)
