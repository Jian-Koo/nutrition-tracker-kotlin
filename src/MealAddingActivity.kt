package com.example.foodcalorie

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MealAddingActivity : ComponentActivity() {
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
                    Column (modifier = Modifier.fillMaxSize()) {
                        ActivityTitle(context, "Add a Meal")
                        MealAddingScreen(mealDao = db.MealDao(), context = context)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealAddingScreen(mealDao: MealDao, context: Context) {
    var mealName by rememberSaveable { mutableStateOf("") }
    var mealPortion by rememberSaveable { mutableStateOf("") }
    var mealType by rememberSaveable { mutableStateOf("") }
    var mealCalories by rememberSaveable { mutableStateOf("") }
    var mealFat by rememberSaveable { mutableStateOf("") }
    var mealProtein by rememberSaveable { mutableStateOf("") }
    var mealCarb by rememberSaveable { mutableStateOf("") }
    var mealPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val mealDate by rememberSaveable { mutableStateOf(currentDate.format(dateFormatter)) }
    var isLoading by rememberSaveable { mutableStateOf(false) } // Loading state
    val cameraUri = rememberSaveable { mutableStateOf<Uri?>(null) } // Create a file URI for the camera to save the photo
    val cameraLauncher = rememberLauncherForActivityResult(  // Camera launcher
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri.value?.let { uri ->
                mealPhoto = uri // Update meal photo URI on success
            }
            showToast(context, "Image Captured successfully")
        } else {
            showToast(context, "Image Captured failed")
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(  // Gallery launcher
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            mealPhoto = it // Set the photo URI from gallery selection
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Main content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                MealPhotoDisplay(mealPhoto = mealPhoto)
            }

            item {
                MealTextField(
                    mealName = mealName,
                    onMealNameChange = { mealName = it },
                    mealType = mealType,
                    onMealTypeChange = { mealType = it },
                    mealPortion = mealPortion,
                    onMealPortionChange = { mealPortion = it },
                    mealCalories = mealCalories,
                    onMealCaloriesChange = { mealCalories = it },
                    mealFat = mealFat,
                    onMealFatChange = { mealFat = it },
                    mealProtein = mealProtein,
                    onMealProteinChange = { mealProtein = it },
                    mealCarb = mealCarb,
                    onMealCarbChange = { mealCarb = it },
                    enabled = !isLoading
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CameraButton(
                    context = context,
                    cameraUri = cameraUri,
                    cameraLauncher = cameraLauncher,
                    isLoading = isLoading
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                GalleryButton(
                    galleryLauncher = galleryLauncher,
                    isLoading = isLoading
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Add Meal Button
                AddMealButton(
                    mealName = mealName,
                    mealPortion = mealPortion,
                    mealType = mealType,
                    mealCalories = mealCalories,
                    mealFat = mealFat,
                    mealProtein = mealProtein,
                    mealCarb = mealCarb,
                    mealDate = mealDate,
                    mealPhoto = mealPhoto,
                    mealDao = mealDao,
                    context = context,
                    onLoadingChange = { isLoading = it },
                    enabled = !isLoading
                )
            }
        }
        if (isLoading) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun AddMealButton(
    mealName: String,
    mealPortion: String,
    mealType: String,
    mealCalories: String,
    mealFat: String,
    mealProtein: String,
    mealCarb: String,
    mealDate: String,
    mealPhoto: Uri?,
    mealDao: MealDao,
    context: Context,
    onLoadingChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

    Button(
        onClick = {
        val portion = mealPortion.toDoubleOrNull() ?: 0.0
        val calories = mealCalories.toDoubleOrNull() ?: 0.0
        val fat = mealFat.toDoubleOrNull() ?: 0.0
        val protein = mealProtein.toDoubleOrNull() ?: 0.0
        val carb = mealCarb.toDoubleOrNull() ?: 0.0

        if (isInputValid(mealName, mealType, portion, calories, fat, protein, carb)) {
            onLoadingChange(true)

            coroutineScope.launch { // Launch a coroutine
                if (mealPhoto == null) {
                    runBlocking {
                        // Insert meal without a photo URL
                        mealDao.insertMeal(
                            Meal(
                                name = mealName,
                                portion = portion,
                                type = mealType,
                                calories = calories,
                                fat = fat,
                                protein = protein,
                                carb = carb,
                                date = mealDate,
                                photoUrl = null // No photo URL provided
                            )
                        )
                    }
                    showToast(context, "Meal added successfully!")
                    navigateToMealList(context, onLoadingChange)
                } else {
                    coroutineScope.launch (Dispatchers.IO) {
                        try {
                            // Upload the image and add the meal with the URL
                            uploadImageToFirebase(mealPhoto, context, { photoUrl ->
                                runBlocking {
                                    mealDao.insertMeal(
                                        Meal(
                                            name = mealName,
                                            portion = portion,
                                            type = mealType,
                                            calories = calories,
                                            fat = fat,
                                            protein = protein,
                                            carb = carb,
                                            date = mealDate,
                                            photoUrl = photoUrl // Use the uploaded photo URL
                                        )
                                    )
                                }
                                showToast(context, "Meal added successfully!")
                                navigateToMealList(context, onLoadingChange)
                            }, { exception ->
                                Toast.makeText(context, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                                onLoadingChange(false)
                            })
                        } catch (e: Exception)
                        {
                            Log.e("MealAdding", "Error updating meal: ${e.message}")
                            onLoadingChange(false)
                        }
                    }


                }
            }
        } else {
            showValidationError(context)
        }
    }, enabled = enabled) {
        Text("Add My Meal")
    }
}
