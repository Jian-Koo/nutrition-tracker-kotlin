package com.example.foodcalorie

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun isInputValid(mealName: String, mealType: String, portion: Double, calories: Double, fat: Double, protein: Double, carb: Double): Boolean {
    return mealName.trim().isNotEmpty() &&
            mealType.trim().isNotEmpty() &&
            portion > 0.0 &&
            calories >= 0.0 &&
            fat >= 0.0 &&
            protein >= 0.0 &&
            carb >= 0.0
}

fun navigateToMealList(context: Context, onLoadingChange: (Boolean) -> Unit) {
    onLoadingChange(false)
    context.startActivity(Intent(context, MealListingActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    })
}

fun showValidationError(context: Context) {
    showToast(context, "Please fill all required fields correctly.")
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun MealTextField(
    mealName: String,
    onMealNameChange: (String) -> Unit,
    mealType: String,
    onMealTypeChange: (String) -> Unit,
    mealPortion: String,
    onMealPortionChange: (String) -> Unit,
    mealCalories: String,
    onMealCaloriesChange: (String) -> Unit,
    mealFat: String,
    onMealFatChange: (String) -> Unit,
    mealProtein: String,
    onMealProteinChange: (String) -> Unit,
    mealCarb: String,
    onMealCarbChange: (String) -> Unit,
    enabled: Boolean
) {
    TextField(
        value = mealName,
        onValueChange = onMealNameChange,
        label = { Text("Meal Name *") },
        enabled = enabled
    )

    TextField(
        value = mealType,
        onValueChange = onMealTypeChange,
        label = { Text("Meal Type *") },
        enabled = enabled
    )

    TextField(
        value = mealPortion,
        onValueChange = onMealPortionChange,
        label = { Text("Portion (g) *") },
        enabled = enabled
    )

    TextField(
        value = mealCalories,
        onValueChange = onMealCaloriesChange,
        label = { Text("Calories (kcal) ") },
        enabled = enabled
    )

    TextField(
        value = mealFat,
        onValueChange = onMealFatChange,
        label = { Text("Fat (g) ") },
        enabled = enabled
    )

    TextField(
        value = mealProtein,
        onValueChange = onMealProteinChange,
        label = { Text("Protein (g)") },
        enabled = enabled
    )

    TextField(
        value = mealCarb,
        onValueChange = onMealCarbChange,
        label = { Text("Carb (g) ") },
        enabled = enabled
    )
}


@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun GalleryButton(galleryLauncher: ManagedActivityResultLauncher<String, Uri?>, isLoading: Boolean) {
    Button(
        onClick = {
            galleryLauncher.launch("image/*")
        },
        enabled = !isLoading // Disable button when loading
    ) {
        Text(text = "Choose from Gallery")
    }
}

@Composable
fun CameraButton(context: Context, cameraUri: MutableState<Uri?>, cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>, isLoading: Boolean) {
    Button(
        onClick = {
            // Create temporary file and URI for the camera
            val photoFile = createImageFile(context)
            cameraUri.value = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            cameraUri.value?.let { uri ->
                cameraLauncher.launch(uri)
            }
        },
        enabled = !isLoading // Disable button when loading
    ) {
        Text(text = "Take Meal Photo")
    }
}

private fun createImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timestamp}_", "", storageDir).apply {
    }
}

@Composable
fun MealPhotoDisplay(mealPhoto: Uri?) {
    var imageUri by remember { mutableStateOf<Uri?>(mealPhoto) }
    var isImageLoading by remember(mealPhoto) { mutableStateOf(true) }

    LaunchedEffect(mealPhoto) {
        if (mealPhoto != null) {
            // Check if the URI is local (not Firebase path)
            if (mealPhoto.scheme == "content" || mealPhoto.scheme == "file") {
                // If it's a local URI, display it directly
                imageUri = mealPhoto
                isImageLoading = false
            } else {
                // Otherwise, fetch from Firebase
                isImageLoading = true
                FirebaseStorage.getInstance().reference.child(mealPhoto.toString())
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        imageUri = uri
                        isImageLoading = false
                    }
                    .addOnFailureListener {
                        isImageLoading = false
                    }
            }
        } else {
            isImageLoading = false
        }
    }

    Box(
        modifier = Modifier
            .height(300.dp)
            .padding(16.dp),
    ) {
        if (isImageLoading) {
            CircularProgressIndicator(color = Color.DarkGray)
        } else {
            imageUri?.let {
                Image(
                    painter = rememberImagePainter(data = it),
                    contentDescription = "Meal Photo",
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Placeholder for missing image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No Image Available")
                }
            }
        }
    }
}

fun uploadImageToFirebase(uri: Uri?, context: Context, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
    // firebase SOURCE : https://youtu.be/btfGTDeZg8w?si=4X6Vf0-z1UoAAOiY
    if (uri == null || uri.lastPathSegment == null) {
        showToast(context, "Invalid image URI")
        onFailure(Exception("Invalid image URI"))
        return
    }

    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val relativePath = "images/${uri.lastPathSegment}"
    val imageRef = storageRef.child(relativePath)

    val uploadTask = imageRef.putFile(uri)
    uploadTask.addOnSuccessListener {
        // Get the download URL of the uploaded image
        imageRef.downloadUrl.addOnSuccessListener {
            showToast(context, "Image upload successful!")
            onSuccess(relativePath)
        }.addOnFailureListener { exception ->
            showToast(context, "Image upload successful!")
            onFailure(exception)
        }
    }.addOnFailureListener { exception ->
        showToast(context, "Image upload failed")
        onFailure(exception)
    }
}

@Composable
fun BackToMenuButton(context : Context){
    // Back Button with circular background
    IconButton(
        onClick = {
            val intent = Intent(context, MainActivity::class.java)
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

@Composable
fun ActivityTitle(context: Context, title: String) {
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
        BackToMenuButton(context = context)

        Text(
            text = title,
            fontSize = baseFontSize,
            lineHeight = lineHeight,
            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp, end = 16.dp)
        )
    }
}
