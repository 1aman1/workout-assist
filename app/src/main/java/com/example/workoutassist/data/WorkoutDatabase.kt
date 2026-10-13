package com.example.workoutassist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TemplateDayEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        SetLogEntity::class,
        ExerciseDayLinkEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getInstance(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_assist.db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN remarks TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN plannedRepsBySetJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE exercises ADD COLUMN plannedWeightBySetJson TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN advancesCycle INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN isCustomActivity INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Additive only: introduces the exercise<->day join table used for multi-day
        // exercise assignment, backfilled from each exercise's current single day/position.
        // exercises.dayNumber/position are dropped next migration, once the join table is
        // the sole source of truth.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exercise_day_links (
                        exerciseId INTEGER NOT NULL,
                        dayNumber INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        PRIMARY KEY(exerciseId, dayNumber),
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE,
                        FOREIGN KEY(dayNumber) REFERENCES template_days(dayNumber) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_exercise_day_links_dayNumber ON exercise_day_links(dayNumber)"
                )
                db.execSQL(
                    "INSERT INTO exercise_day_links (exerciseId, dayNumber, position) SELECT id, dayNumber, position FROM exercises"
                )
            }
        }

        // Table rebuild (SQLite can't DROP COLUMN on old Android versions): recreates
        // exercises without dayNumber/position now that exercise_day_links is the sole
        // source of truth for day membership and per-day order.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE exercises_new (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        sets INTEGER NOT NULL,
                        reps INTEGER NOT NULL,
                        intervalSeconds INTEGER NOT NULL,
                        plannedWeight TEXT NOT NULL,
                        plannedRepsBySetJson TEXT NOT NULL DEFAULT '',
                        plannedWeightBySetJson TEXT NOT NULL DEFAULT '',
                        remarks TEXT NOT NULL DEFAULT '',
                        isDone INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO exercises_new (id, name, sets, reps, intervalSeconds, plannedWeight, plannedRepsBySetJson, plannedWeightBySetJson, remarks, isDone)
                    SELECT id, name, sets, reps, intervalSeconds, plannedWeight, plannedRepsBySetJson, plannedWeightBySetJson, remarks, isDone FROM exercises
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE exercises")
                db.execSQL("ALTER TABLE exercises_new RENAME TO exercises")
            }
        }
    }
}
