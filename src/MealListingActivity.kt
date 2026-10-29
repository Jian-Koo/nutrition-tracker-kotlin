package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import com.google.firebase.storage.FirebaseStorage

class MealListingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodCalorieTheme {
                val db = MealDatabaseProvider.getDatabase(this)
                val context = LocalContext.current
                Surface( // give an outline of the background colour and size
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ActivityTitle(context, "Meal List")
                        MealListingScreen(mealDao = db.MealDao(), context = context)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealListingScreen(mealDao: MealDao, context: Context) {
    val meals by mealDao.getAllMeals().collectAsState(initial = emptyList())

    // Display meals using LazyColumn
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(meals) { meal ->
            MealItem(meal = meal, context = context)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add "End" at the bottom of the list
        item {
            Spacer(modifier = Modifier.height(8.dp)) // Space before "End"
            Text(
                text = "END",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MealItem(meal: Meal, context: Context) {
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var isImageLoading by remember { mutableStateOf(true) } // Track loading state

    // Retrieve the image URL from Firebase Storage if available
    LaunchedEffect(meal.photoUrl) {
        meal.photoUrl?.let { path ->
            FirebaseStorage.getInstance().reference.child(path)
                .downloadUrl
                .addOnSuccessListener { uri ->
                    photoUrl = uri.toString()
                    isImageLoading = false // Loading complete
                }
                .addOnFailureListener {
                    isImageLoading = false // Error occurred, loading complete
                }
        } ?: run {
            isImageLoading = false // No image available
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable { openMealUpdateActivity(context, meal.id) }
    ) {
        // Show image or placeholder based on loading state
        if (isImageLoading) {
            // Display a loading indicator or placeholder image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF5e2ed1),)
            }
        } else {
            // Display the image if available, otherwise show a placeholder
            if (photoUrl != null) {
                Image(
                    painter = rememberImagePainter(data = photoUrl),
                    contentDescription = "Meal Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(end = 8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder if no image is available
                Text(
                    text = "No Image Available",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(end = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Meal details on the right
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Name: ${meal.name}")
            Text(text = "Type: ${meal.type}")
            Text(text = "Portion: ${meal.portion}g")
            Text(text = "Calories: ${meal.calories}kcal")
            Text(text = "Fat: ${meal.fat}g")
            Text(text = "Protein: ${meal.protein}g")
            Text(text = "Carb: ${meal.carb}g")
            Text(text = "Date: ${meal.date}")
        }
    }
}

private fun openMealUpdateActivity(context: Context, mealId: Int) {
    val intent = Intent(context, MealUpdatingActivity::class.java).apply {
        putExtra("MEAL_ID", mealId)
    }
    context.startActivity(intent)
}
