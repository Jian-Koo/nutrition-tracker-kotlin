# Nutrition Tracker — Kotlin Android Application

An Android nutrition and calorie tracking application developed using **Kotlin and Jetpack Compose**, featuring meal management, daily calorie goal tracking, nutritional information retrieval, and meal photo management.

The application integrates **Room Database** for local data persistence, **Firebase Storage** for meal photographs, and the **CalorieNinjas API** for retrieving nutritional information.

The project explores Android application development, local database management, REST API integration, cloud storage, asynchronous programming, and user interface design.

## Project Overview

Nutrition Tracker is designed to help users record their daily food consumption, monitor calorie intake, and manage nutritional information through an Android application.

Users can manually create and manage meal entries or search for nutritional information through an external API.

Each meal can contain its name, category, portion size, calories, macronutrient information, date, and an optional photograph.

### Key Features

- **Meal management:** Create, view, update, and delete meal records.
- **Calorie tracking:** Monitor daily calorie intake against a configurable goal.
- **Nutritional information:** Record calories, fat, protein, and carbohydrates.
- **Food search:** Retrieve nutritional information using the CalorieNinjas API.
- **Meal photographs:** Capture images using the camera or select existing images from the gallery.
- **Firebase integration:** Upload meal photographs to Firebase Storage and retrieve them for display.
- **Local persistence:** Store meal records and calorie goals using Room Database.
- **Reactive updates:** Observe database changes through Kotlin Flow.
- **Asynchronous operations:** Use Kotlin Coroutines for database and network operations.
- **User interface:** Built with Jetpack Compose and Material 3.

## Technical Implementation

### 1. Meal Management

The application supports the creation and management of meal records through dedicated activity screens.

Each meal contains the following information:

| Field | Description |
|---|---|
| Meal ID | Automatically generated unique identifier |
| Name | Name of the food or meal |
| Portion | Serving size |
| Type | Meal category |
| Calories | Energy content in kilocalories |
| Fat | Total fat in grams |
| Protein | Protein content in grams |
| Carbohydrates | Carbohydrate content in grams |
| Date | Date associated with the meal |
| Photo URL | Optional Firebase Storage reference |

#### Adding Meals

Users can manually enter nutritional information and optionally attach a photograph.

The application validates the required fields before saving the meal to the local database.

If a photograph is attached, the application uploads it to Firebase Storage and stores the corresponding reference with the meal record.

#### Viewing Meals

Saved meals are displayed in a scrollable list.

Each entry displays meal information and its associated photograph, when available.

Selecting a meal opens the editing interface.

#### Updating and Deleting Meals

Users can modify existing meal information, replace photographs, or delete individual meal records.

These operations are performed through Room Database and reflected in the application's meal list.

### 2. Local Data Persistence — Room Database

The application uses **Room**, Android's persistence library built on SQLite, to store information locally.

Two separate databases are implemented:

**Meal Database — `MealDatabase.kt`**

Stores the user's recorded meals and their nutritional information.

The `Meal` entity represents an individual food entry.

The corresponding Data Access Object (`MealDao`) provides operations for:

- Retrieving all meals
- Retrieving a specific meal by ID
- Inserting new meals
- Updating existing meals
- Deleting meals
- Calculating total daily calorie intake

**Calorie Goal Database — `CalorieDatabase.kt`**

Stores user-defined daily calorie targets.

The `Calorie` entity represents a recorded calorie goal, while `CalorieDao` provides operations to insert goals and retrieve the latest saved value.

#### Reactive Data Access

The databases use Kotlin `Flow` to expose query results.

Jetpack Compose collects these values and updates the interface as the underlying data changes.

This approach supports reactive updates without requiring the user to manually refresh the application.

### 3. Daily Calorie Tracking

The main screen displays:

- Current date
- Total calories consumed for the day
- User-defined daily calorie goal

Daily calorie intake is calculated from meal entries matching the current date.

The application uses the following SQL query through Room:

```sql
SELECT SUM(calories)
FROM meals
WHERE date = :todayDate
GROUP BY date
```

Dates are represented using the `yyyy-MM-dd` format.

The application displays the daily intake alongside the configured goal:

```text
Today's Intake (kcal): 1450.00 / 2000.00
```

If no calorie goal has been saved, the application uses a default value of 2,000 kcal.

Users can set a new goal through the dedicated Goal Setting screen.

### 4. Nutritional Information — CalorieNinjas API

The application integrates the **CalorieNinjas Nutrition API** to retrieve nutritional information for searched food items.

The integration is implemented using Retrofit and Gson.

#### API Request

The application communicates with the following endpoint:

```http
GET https://api.calorieninjas.com/v1/nutrition
```

Requests include:

- `X-Api-Key` header for API authentication
- `query` parameter containing the food search term

#### Response Processing

The API returns nutritional information in JSON format.

The application maps the response to Kotlin data classes defined in `RetrofitUtil.kt`.

The response model includes nutritional attributes such as:

- Food name
- Serving size
- Calories
- Total and saturated fat
- Protein
- Carbohydrates
- Sodium and potassium
- Cholesterol
- Fibre and sugar

The application primarily uses the food name, serving size, calories, fat, protein, and carbohydrates.

#### Search Workflow

1. The user enters a food item.
2. The application sends an authenticated request through Retrofit.
3. The response is converted into Kotlin objects using Gson.
4. The first returned food item is displayed with its nutritional information.
5. The user can add the displayed item to their meal records.
6. The selected information is stored in the local Room database.

`MealViewModel.kt` manages the network request and exposes loading, result, and error states through `StateFlow`.

Kotlin Coroutines perform the network operation asynchronously.

### 5. Meal Photographs — Firebase Storage

Firebase Storage is integrated to support meal photographs.

Users can:

- Capture photographs using the device camera.
- Select existing photographs from the gallery.
- Attach photographs to meal records.
- View stored photographs in the meal list.
- Replace existing photographs when updating meals.

#### Image Upload

The application uploads images to Firebase Storage under the following path:

```text
images/
```

Uploaded photographs are referenced using their storage paths.

These paths are saved with the corresponding meal records in Room Database.

#### Image Retrieval

When displaying a meal, the application uses its stored Firebase reference to retrieve the photograph's download URL.

The image is subsequently displayed through the Coil image-loading library.

Loading indicators and placeholder content are used while photographs are being retrieved or when no image is available.

#### Integration Design

The implementation separates meal information from image storage:

- **Room Database:** Stores meal details and image references.
- **Firebase Storage:** Stores the actual photograph files.
- **Coil:** Loads photographs into the application's UI.

This demonstrates the integration of local persistence and cloud-based media storage.

### 6. Application Architecture and State Management

The application uses multiple Android activities for its main functional screens.

Jetpack Compose provides the user interface, while Room, ViewModels, and Kotlin Coroutines support data management and asynchronous operations.

The main components are:

| Component | Responsibility |
|---|---|
| Activities | Screen navigation and user interactions |
| Jetpack Compose | Declarative interface components |
| ViewModel | API request state and result management |
| Room Database | Persistent local storage |
| Retrofit | REST API communication |
| Kotlin Coroutines | Asynchronous operations |
| Firebase Storage | Cloud-based image storage |
| Coil | Asynchronous image loading |

#### Data Flow

**Manual Meal Entry**

```text
User Input
    ↓
Input Validation
    ↓
Optional Photo Upload → Firebase Storage
    ↓
Room Database
    ↓
Meal List / Daily Calorie Tracking
```

**API-Based Meal Entry**

```text
Food Search
    ↓
Retrofit API Request
    ↓
CalorieNinjas API
    ↓
JSON Response
    ↓
Gson Deserialisation
    ↓
ViewModel State
    ↓
Nutrition Display
    ↓
Room Database
```

This architecture combines local data management, network communication, and cloud storage within a single Android application.

### 7. User Interface — Jetpack Compose

The application interface is implemented using **Jetpack Compose and Material 3**.

The main screens include:

| Screen | Description |
|---|---|
| Main Menu | Daily calorie overview and navigation |
| Add Meal | Manual meal entry and optional photo attachment |
| Meal List | Display recorded meals and photographs |
| Update Meal | Edit or delete existing meal records |
| Search Meal | Retrieve food information through the CalorieNinjas API |
| Goal Setting | Configure the daily calorie target |

The interface uses Compose state management, scrollable layouts, input fields, buttons, loading indicators, and navigation between activities.

The implementation also includes layout adjustments based on device orientation.

## Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Android SDK | Android application development |
| Jetpack Compose | Declarative user interface |
| Material 3 | UI components and styling |
| Android ViewModel | State management |
| Room Database | Local persistent storage |
| SQLite | Underlying relational database |
| Kotlin Flow / StateFlow | Reactive data observation |
| Kotlin Coroutines | Asynchronous execution |
| Retrofit | REST API communication |
| Gson | JSON deserialisation |
| CalorieNinjas API | Nutritional information retrieval |
| Firebase Storage | Meal photograph storage |
| Coil | Image loading and display |

## Repository Structure

This repository contains a curated selection of the original application's Kotlin source code.

```text
nutrition-tracker-kotlin/
├── README.md
│
└── src/
    ├── CalorieDatabase.kt
    ├── GoalSettingActivity.kt
    ├── MainActivity.kt
    ├── MealAddingActivity.kt
    ├── MealDatabase.kt
    ├── MealFunctions.kt
    ├── MealListingActivity.kt
    ├── MealSearchingActivity.kt
    ├── MealUpdatingActivity.kt
    ├── MealViewModel.kt
    ├── RetrofitUtil.kt
    │
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Source File Descriptions

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Main dashboard, daily calorie tracking, and navigation |
| `MealAddingActivity.kt` | Manual meal entry and photograph attachment |
| `MealListingActivity.kt` | Displaying saved meal records |
| `MealUpdatingActivity.kt` | Updating and deleting existing meals |
| `MealSearchingActivity.kt` | Food search and API-based meal entry |
| `GoalSettingActivity.kt` | Daily calorie goal configuration |
| `MealDatabase.kt` | Meal entity, DAO, and Room database |
| `CalorieDatabase.kt` | Calorie goal entity, DAO, and Room database |
| `MealViewModel.kt` | API search state, network requests, and error handling |
| `RetrofitUtil.kt` | REST API configuration and nutritional response models |
| `MealFunctions.kt` | Shared UI components, validation, photograph handling, and Firebase utilities |
| `theme/` | Application colour scheme, typography, and theme configuration |

## Repository Scope

This repository is intended as a **source-code showcase** of the application's implementation.

The original application was developed using Android Studio. For portfolio purposes, the repository has been simplified to focus on the source code, database implementation, API integration, Firebase functionality, and user interface components.

Gradle configuration, generated resources, Android Studio files, and project-specific configuration files have been omitted.

As a result, the repository is not directly buildable as a standalone Android Studio project without restoring the necessary configuration and resources.

The original Kotlin implementation has otherwise been preserved, apart from the replacement of sensitive credentials.

## External Services and Configuration

### CalorieNinjas API

The food search feature requires a valid CalorieNinjas API key.

The original hard-coded key has been replaced with a placeholder in the publicly shared source code.

A valid key must be configured for API-based nutritional searches to function.

### Firebase Storage

The application originally uses a Firebase project for photograph uploads and retrieval.

The Firebase configuration file (`google-services.json`) is intentionally excluded from this repository.

Firebase integration requires an appropriately configured Firebase project and Storage security rules.

### Local Database

Room stores meal records and calorie goals locally on the device.

The local database implementation does not require an external database server.

## Design Considerations and Limitations

### API Dependency

Nutritional searches depend on the availability of the CalorieNinjas API and a valid API key.

Network errors or unavailable results may prevent nutritional information from being retrieved.

### Search Results

The implementation uses the first food item returned by the API rather than providing a comprehensive selection of multiple matching results.

### Data Storage

Meal records and calorie goals are stored locally using Room Database.

The application does not implement cross-device synchronisation of its local database records.

### Photograph Storage

Meal photographs are stored separately in Firebase Storage.

Their availability depends on the Firebase project's configuration, access rules, and storage availability.

### Authentication and Security

The application does not implement user authentication for Firebase Storage access.

Access control therefore depends on the configured Firebase Storage security rules.

The original implementation was developed as an educational project rather than a production-ready health or nutrition platform.

### Application Build

This repository contains selected Kotlin source files rather than the complete Android Studio project.

It is intended for technical review and portfolio presentation, not direct deployment.

## Development and Contributions

This application was originally developed as a group project for undergraduate Android development coursework.

I was the **primary developer**, responsible for the application's core design, implementation, and integration, including:

- Meal creation, editing, listing, and deletion
- Daily calorie tracking and goal management
- Room database design and implementation
- Nutritional information retrieval through the CalorieNinjas API
- Retrofit integration and JSON response handling
- Firebase Storage integration for meal photographs
- Camera and gallery functionality
- Jetpack Compose user interface development
- Application navigation and state management
- Integration of local storage, external APIs, and cloud services

The project provided practical experience in developing an Android application that combines persistent local data, third-party services, cloud storage, and interactive user interfaces.

## Copyright

Copyright © 2026 Jian Heng Koo. All rights reserved for my original contributions.

The source code and documentation authored by me are presented for portfolio and educational review.

No licence is granted to copy, modify, redistribute, or commercially use my original materials without permission.

Any contributions or materials created by other authors remain subject to their respective rights.
