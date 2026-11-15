package com.example.trekerpodhodov.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "work_sets")
data class WorkSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val weight: Float,
    val reps: Int,
    val date: Long = System.currentTimeMillis()
)

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertExercise(exercise: Exercise)

    @Query("SELECT * FROM exercises ORDER BY id DESC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert
    suspend fun insertSet(workSet: WorkSet)

    @Query("SELECT * FROM work_sets WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getSetsForExercise(exerciseId: Long): Flow<List<WorkSet>>
}

@Database(entities = [Exercise::class, WorkSet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}