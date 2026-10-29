
package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MealSearchingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodCalorieTheme {
                val context = LocalContext.current
                val viewModel : MealViewModel by viewModels()
                val db = MealDatabaseProvider.getDatabase(this)


                Surface( // give an outline of the background colour and size
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ActivityTitle(context, "Food Searching")
                        MealSearchingScreen(viewModel = viewModel,
                            mealDao = db.MealDao(),
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealSearchingScreen(viewModel: MealViewModel, mealDao: MealDao, context: Context) {
    var searchKey by rememberSaveable { mutableStateOf("") }
    // Observing state
    val nutritionItem by viewModel.nutritionItem.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val currentDate = LocalDate.now() // get today's date
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // in yyyy-mm-dd format
    val mealDate by remember { mutableStateOf(currentDate.format(dateFormatter)) } // and apply it to the mealDate
    var selectedNutritionItem by remember { mutableStateOf<FoodItem?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Add spacing between items
    ) {
        item {
            SearchField(searchKey, onValueChange = { searchKey = it })
        }

        item {
            SearchLoadingIndicator(
                loading = loading,
                nutritionItem = nutritionItem,
                onNutritionItemSelected = { selectedNutritionItem = it }
            )
        }

        errorMessage?.let { message ->
            if (message.isNotEmpty()) {
                item {
                    ErrorMessage(message)
                }
            }
        }

        item {
            SearchButton(
                onClick = {
                    selectedNutritionItem = null
                    // Call the fetchNutrition from the MealViewModel
                    viewModel.fetchNutritionData("YOUR_CALORIENINJAS_API_KEY", searchKey)
                }
            )
        }

        item {
            AddMealButton(
                selectedNutritionItem = selectedNutritionItem,
                mealDao = mealDao,
                mealDate = mealDate,
                context = context,
                coroutineScope = coroutineScope
            )
        }
    }
}

@Composable
private fun SearchField(searchKey: String, onValueChange: (String) -> Unit) {
    // Container Box with light gray background
    Box(
        modifier = Modifier
            .background(Color.LightGray, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = searchKey,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (searchKey.isEmpty()) {
                        Text(text = "Enter food item *")
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun SearchLoadingIndicator(
    loading: Boolean,
    nutritionItem: FoodItem?,
    onNutritionItemSelected: (FoodItem?) -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.LightGray, shape = RoundedCornerShape(20.dp))
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            if (nutritionItem != null) {
                NutritionItemCard(nutritionItem) // Display the nutritionItem
                onNutritionItemSelected(nutritionItem)
            } else {
                // Handle the case where there is no nutrition item
                Text(text = "No result found")
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Search Food")
    }
}

@Composable
private fun AddMealButton(
    selectedNutritionItem: FoodItem?,
    mealDao: MealDao,
    mealDate: String,
    context: Context,
    coroutineScope: CoroutineScope
) {
    Button(onClick = {
        // execute only if it is not null , ?.let
        selectedNutritionItem?.let { item ->
            val meal = Meal(
                name = item.name,
                portion = item.servingSize,
                type = "Unknown type",
                calories = item.calories,
                fat = item.fatTotal,
                protein = item.protein,
                carb = item.carb,
                date = mealDate,
                photoUrl = null
            )

            coroutineScope.launch {
                mealDao.insertMeal(meal)
                // After adding, navigate back to the main activity
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(intent)
            }
        }
    }) {
        Text("Add to My Meal")
    }
}

@Composable
private fun NutritionItemCard(item: FoodItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Food: ${item.name}")
        Text(text = "Portion : ${item.servingSize}g")
        Text(text = "Calories : ${item.calories}kcal")
        Text(text = "Fat: ${item.fatTotal}g")
        Text(text = "Protein: ${item.protein}g")
        Text(text = "Carbs: ${item.carb}g")
    }
}
