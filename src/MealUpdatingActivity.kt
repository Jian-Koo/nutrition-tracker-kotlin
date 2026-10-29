package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodcalorie.ui.theme.FoodCalorieTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MealUpdatingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodCalorieTheme {
                val db = MealDatabaseProvider.getDatabase(this)
                val context = LocalContext.current
                val mealId = intent.getIntExtra("MEAL_ID", -1)
                Surface( // give an outline of the background colour and size
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MealUpdatingTitle(context = context)
                        MealUpdatingScreen(mealDao = db.MealDao(), context = context, mealId = mealId)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealUpdatingScreen(mealDao: MealDao, context: Context, mealId: Int) {
    // Collect the meal data from the database using the provided mealId
    val meal by mealDao.getMealById(mealId).collectAsState(initial = null)

    // Initialize fields with empty values
    var mealName by rememberSaveable { mutableStateOf("") }
    var mealPortion by rememberSaveable { mutableStateOf("") }
    var mealType by rememberSaveable { mutableStateOf("") }
    var mealCalories by rememberSaveable { mutableStateOf("") }
    var mealFat by rememberSaveable { mutableStateOf("") }
    var mealProtein by rememberSaveable { mutableStateOf("") }
    var mealCarb by rememberSaveable { mutableStateOf("") }
    var mealPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) } // Loading state
    val cameraUri = rememberSaveable { mutableStateOf<Uri?>(null) } // Create a file URI for the camera to save the photo
    val cameraLauncher = rememberLauncherForActivityResult(  // Camera launcher
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri.value?.let { uri ->
                mealPhoto = uri // Update meal photo URI on success
                showToast(context, "Image Captured successfully")
            }
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

    // Update the text fields when the meal data changes, only if fields are empty
    LaunchedEffect(meal) {
        meal?.let {
            if (mealName.isEmpty()) mealName = it.name
            if (mealPortion.isEmpty()) mealPortion = it.portion.toString()
            if (mealType.isEmpty()) mealType = it.type
            if (mealCalories.isEmpty()) mealCalories = it.calories.toString()
            if (mealFat.isEmpty()) mealFat = it.fat.toString()
            if (mealProtein.isEmpty()) mealProtein = it.protein.toString()
            if (mealCarb.isEmpty()) mealCarb = it.carb.toString()
            if (mealPhoto == null) {
                mealPhoto = it.photoUrl?.let { photoUrl -> Uri.parse(photoUrl) }
            }
        }
    }

    // Only display the content if the meal is not null
    if (meal != null) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    // Display existing photo or place holder if not available
                    MealPhotoDisplay(mealPhoto = mealPhoto)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Text fields to edit meal details
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
                    // Button to update meal
                    MealUpdatingButton(
                        context = context,
                        mealDao = mealDao,
                        meal = meal,
                        mealName = mealName,
                        mealType = mealType,
                        mealPortion = mealPortion,
                        mealCalories = mealCalories,
                        mealFat = mealFat,
                        mealProtein = mealProtein,
                        mealCarb = mealCarb,
                        mealPhoto = mealPhoto,
                        onLoadingChange = { isLoading = it },
                        enabled = !isLoading
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Button to delete meal
                    MealDeletingButton(
                        mealId = mealId,
                        mealDao = mealDao,
                        context = context,
                        enabled = !isLoading
                    )
                }
            }
            if (isLoading) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
private fun MealUpdatingButton(
    meal: Meal?,
    mealName: String,
    mealType: String,
    mealPortion: String,
    mealCalories: String,
    mealFat: String,
    mealProtein: String,
    mealCarb: String,
    mealPhoto: Uri?,
    mealDao: MealDao,
    context: Context,
    onLoadingChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            val portion = mealPortion.toDoubleOrNull() ?: 0.0
            val calories = mealCalories.toDoubleOrNull() ?: 0.0
            val fat = mealFat.toDoubleOrNull() ?: 0.0
            val protein = mealProtein.toDoubleOrNull() ?: 0.0
            val carb = mealCarb.toDoubleOrNull() ?: 0.0

            if (isInputValid(mealName, mealType, portion, calories, fat, protein, carb)) {
                onLoadingChange(true)

                coroutineScope.launch (Dispatchers.IO){
                    try {
                        meal?.let { nonNullMeal ->
                            val previousPhotoUrl = nonNullMeal.photoUrl
                            val isNewPhoto = mealPhoto != null
                            val isPhotoChanged = isNewPhoto && (previousPhotoUrl == null || mealPhoto != Uri.parse(previousPhotoUrl))

                            // Check if mealPhoto is changed and valid
                            if (isPhotoChanged) {

                                uploadImageToFirebase(
                                    uri = mealPhoto,
                                    context = context,
                                    onSuccess = { photoUrl ->
                                        // Update meal with new photo URL after upload, now new info is stored in new 'val' called updatedMeal
                                        val updatedMeal = nonNullMeal.copy(
                                            name = mealName,
                                            type = mealType,
                                            portion = portion,
                                            calories = calories,
                                            fat = fat,
                                            protein = protein,
                                            carb = carb,
                                            photoUrl = photoUrl // Use the new photo URL
                                        )
                                        runBlocking {
                                            mealDao.updateMeal(updatedMeal)
                                        }
                                        navigateToMealList(context, onLoadingChange)
                                    },
                                    onFailure = { exception ->
                                        showToast(context, "Failed to upload image: ${exception.message}")
                                        onLoadingChange(false)
                                    }
                                )
                            } else {
                                // Update meal without modifying photo URL
                                val updatedMeal = nonNullMeal.copy(
                                    name = mealName,
                                    type = mealType,
                                    portion = portion,
                                    calories = calories,
                                    fat = fat,
                                    protein = protein,
                                    carb = carb
                                )
                                runBlocking {
                                    mealDao.updateMeal(updatedMeal)
                                }
                                navigateToMealList(context, onLoadingChange)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MealUpdating", "Error updating meal: ${e.message}")
                        onLoadingChange(false)
                    }
                }
            } else {
                showValidationError(context)
            }
        },
        enabled = enabled
    ) {
        Text("Save Changes")
    }
}

@Composable
private fun MealDeletingButton(mealId: Int, mealDao: MealDao, context: Context, enabled: Boolean) {
    Button(
        onClick = {
            // Launch a coroutine to delete the meal from the database
            CoroutineScope(Dispatchers.IO).launch {
                // Retrieve the meal from the database first
                val meal = mealDao.getMealById(mealId).firstOrNull()

                if(meal != null) {
                    mealDao.deleteMeal(meal)

                    withContext(Dispatchers.Main) {
                        // Show a confirmation message
                        showToast(context, "Meal deleted successfully")
                        // Navigate back to the listing
                        val intent = Intent(context, MealListingActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            }
        }, enabled = enabled
    ) {
        Text(text = "Delete Meal")
    }
}

@Composable
private fun MealUpdatingTitle(context: Context) {
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
    ) {
        BackToListButton(context = context)
        Text(
            text = "Meal Update",
            fontSize = baseFontSize,
            lineHeight = lineHeight,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp, end = 16.dp)
        )
    }
}

@Composable
private fun BackToListButton(context : Context){
    // Back Button with circular background
    IconButton(
        onClick = {
            val intent = Intent(context, MealListingActivity::class.java)
            context.startActivity(intent)
        },
        modifier = Modifier
            .padding(16.dp)
            .size(60.dp)
            .background(color = Color.Gray, shape = CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowLeft, // Use the arrow left icon
            contentDescription = "Back",
            tint = Color.White // Icon color
        )
    }
}
