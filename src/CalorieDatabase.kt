package com.example.foodcalorie

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Define Contact entity for the room db
@Entity(tableName = "calories")
data class Calorie(
    @PrimaryKey(autoGenerate = true) val calorieId: Int = 0,
    val calories : Double
)

@Dao
interface CalorieDao {
    @Query("SELECT calories FROM calories ORDER BY calorieId DESC LIMIT 1")
    fun getLatestCalorie(): Flow<Double?>
    @Insert
    suspend fun insertCalorie(calorie: Calorie)
    // @Update
    // suspend fun updateCalorie(calorie: Calorie)
    // Currently unused
}

@Database(entities = [Calorie::class], version = 1)
abstract class CalorieAppDatabase : RoomDatabase() {
    abstract fun CalorieDao(): CalorieDao
}

object CalorieDatabaseProvider {
    private var INSTANCE: CalorieAppDatabase? = null

    fun getDatabase(context: Context): CalorieAppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                CalorieAppDatabase::class.java,
                "calorie-database"
            ).allowMainThreadQueries().build()
            INSTANCE = instance
            instance
        }
    }
}
