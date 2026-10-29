package com.example.foodcalorie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealViewModel : ViewModel() {

    // MutableStateFlow to hold the nutrition information
    private val _nutritionItem = MutableStateFlow<FoodItem?>(null)
    val nutritionItem: StateFlow<FoodItem?> get() = _nutritionItem

    // MutableStateFlow to hold error messages
    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // MutableStateFlow to hold loading state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val apiService = RetrofitInstance.api

    fun fetchNutritionData(apiKey: String, query: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val searchResult = withContext(Dispatchers.IO) {
                    apiService.getNutritionInfo(apiKey, query) // Call the updated API method
                }

                // Get the first item, if it is null, return an empty list
                val firstItem = searchResult.hits.firstOrNull()
                _nutritionItem.value = firstItem
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}