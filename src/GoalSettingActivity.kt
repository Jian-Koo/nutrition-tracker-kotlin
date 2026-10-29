package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoalSettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodCalorieTheme {
                val db = CalorieDatabaseProvider.getDatabase(this)
                val context = LocalContext.current
                Surface( // give an outline of the background colour and size
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ActivityTitle(context, "Goal Setting")
                        CalorieSettingScreen(calorieDao = db.CalorieDao(), context = context)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalorieSettingScreen(calorieDao: CalorieDao, context: Context) {
    var calorieGoal by rememberSaveable { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                TextField(
                    value = calorieGoal,
                    onValueChange = { calorieGoal = it },
                    label = { Text("Calorie Goal (kcal) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        val calories = calorieGoal.toDoubleOrNull() ?:0.0
                        if (calories > 0.0) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    calorieDao.insertCalorie(Calorie(calories = calories))
                                    Log.d("CalorieInsert", "Inserted calorie goal: $calories")
                                } catch (e: Exception) {
                                    Log.e("CalorieInsert", "Error inserting calorie goal", e)
                                }
                            }
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}

