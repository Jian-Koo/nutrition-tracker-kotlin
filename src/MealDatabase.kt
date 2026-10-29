package com.example.foodcalorie

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Define Contact entity for the room db
@Entity(tableName = "meals")
data class Meal(
    // For fulfilling the requirement of a db, primary key is needed,
    // but when listing, no id will be shown
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val portion: Double,
    val type: String,
    val calories : Double,
    val fat : Double,
    val protein : Double,
    val carb: Double,
    val date : String,
    val photoUrl: String? = null
)

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY id DESC")
    fun getAllMeals(): Flow<List<Meal>>
    @Query("SELECT SUM(calories) FROM meals WHERE date = :todayDate GROUP BY date")
    fun getTotalCaloriesForToday(todayDate: String): Flow<Double?>
    @Query("SELECT * FROM meals WHERE id = :mealId")
    fun getMealById(mealId: Int): Flow<Meal>
    @Insert
    suspend fun insertMeal(meal: Meal)
    @Delete
    suspend fun deleteMeal(meal: Meal)
    @Update
    suspend fun updateMeal(meal: Meal)
}

@Database(entities = [Meal::class], version = 1)
abstract class MealAppDatabase : RoomDatabase() {
    abstract fun MealDao(): MealDao
}

object MealDatabaseProvider {
    private var INSTANCE: MealAppDatabase? = null

    fun getDatabase(context: Context): MealAppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                MealAppDatabase::class.java,
                "meal-database"
            ).allowMainThreadQueries().build()
            INSTANCE = instance
            instance
        }
    }
}