package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import com.google.firebase.FirebaseApp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodCalorieTheme {
                val calorieDb = CalorieDatabaseProvider.getDatabase(this)
                val mealDb = MealDatabaseProvider.getDatabase(this)
                val context = LocalContext.current

                val calorieGoal by calorieDb.CalorieDao().getLatestCalorie().collectAsState(initial = 2000.0)

                Surface( // give an outline of the background colour and size
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    FoodCalorieMenu(calorieGoal = calorieGoal?: 2000.0,
                                    mealDao = mealDb.MealDao(),
                                    context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodCalorieMenu(calorieGoal: Double, mealDao: MealDao, context: Context) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val currentDate = LocalDate.now().format(dateFormatter)
    var todayIntake by remember { mutableDoubleStateOf(0.0) }

    // SOURCE : https://developer.android.com/develop/ui/compose/side-effects
    LaunchedEffect(Unit) {
    // Collecting Flow for total calories for today
       mealDao.getTotalCaloriesForToday(currentDate).collect { intake ->
            todayIntake = intake ?: 0.0 // Handle null values if necessary
       }
    }
    Column {
        MenuTitle()
        LazyColumn (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            item{
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Today's Date : $currentDate",
                    color = Color.White
                )
                Text(
                    text = String.format("Today's Intake (kcal) : %.2f / %.2f", todayIntake, calorieGoal),
                    color = Color.White
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                MenuButtons(context)
            }
        }
    }
}

@Composable
private fun MenuTitle() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // Adjust font size based on screen width and height
    val baseFontSize = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        (screenHeight / 10).sp // slightly smaller font size in landscape mode
    } else {
        (screenWidth / 10).sp // adjust this division for better fit
    }
    val lineHeight = baseFontSize * 1.2f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Food Calorie Management App",
            fontSize = baseFontSize,
            lineHeight = lineHeight
        )
    }
}

@Composable
private fun MenuButtons(context: Context)
{
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier
                .height(30.dp)
                .weight(0.5f))
            Button(
                modifier = Modifier.width(140.dp),
                onClick = { openMealAddingActivity(context) }
            ) {
                Text(text = "Add Meals")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.width(140.dp),
                onClick = { openMealListingActivity(context) }
            ) {
                Text(text = "Meals List")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.width(140.dp),
                onClick = { openMealSearchingActivity(context) }
            ) {
                Text(text = "Search Meals")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.width(140.dp),
                onClick = { openGoalSettingActivity(context) }
            ) {
                Text(text = "Goal Setting")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Spacer(modifier = Modifier
                .height(30.dp)
                .weight(1f))
        }
    }
}

private fun openMealAddingActivity(context: Context) {
    val intent = Intent(context, MealAddingActivity::class.java)
    context.startActivity(intent)
}

private fun openMealListingActivity(context: Context) {
    val intent = Intent(context, MealListingActivity::class.java)
    context.startActivity(intent)
}

private fun openMealSearchingActivity(context: Context) {
    val intent = Intent(context, MealSearchingActivity::class.java)
    context.startActivity(intent)
}

private fun openGoalSettingActivity(context: Context) {
    val intent = Intent(context, GoalSettingActivity::class.java)
    context.startActivity(intent)
}

