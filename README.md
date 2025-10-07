# Edai - Educational Travel App 🌍📚

A Kotlin Android application that combines location detection with educational content, providing users with fascinating information and quizzes about places around the world.

## 🎯 Features

### Core Functionality
- **📍 Location Detection**: Hybrid GPS + Network location detection using FusedLocationProviderClient
- **🔍 Manual Location Search**: Search for places using OpenStreetMap Nominatim API
- **🏛️ Place Information**: Rich details about locations using Wikipedia API
- **🧠 Interactive Quizzes**: Knowledge tests using Open Trivia Database API
- **🎨 Modern UI**: Clean Material 3 design with Jetpack Compose

### Key Capabilities
- **Auto-detect current location** with proper permission handling
- **Search locations manually** with autocomplete suggestions
- **View comprehensive place information** with descriptions and images
- **Take knowledge quizzes** in different categories (Geography, History, General Knowledge)
- **Score tracking** with detailed results and retry options
- **Offline fallback** with mock data when APIs are unavailable

## 🛠️ Technology Stack

### Architecture
- **MVVM Pattern** with Repository pattern
- **Jetpack Compose** for modern UI
- **Kotlin Coroutines & Flow** for asynchronous operations
- **StateFlow & LiveData** for reactive state management

### Key Libraries
- **Location Services**: Google Play Services Location
- **Networking**: Retrofit + Gson + OkHttp
- **Image Loading**: Coil for Compose
- **Permissions**: Accompanist Permissions
- **Navigation**: Compose Navigation
- **UI**: Material 3 + Compose

### Free APIs Used
- **OpenStreetMap Nominatim API** - Geocoding (forward/reverse)
- **Wikipedia REST API** - Place information and descriptions
- **Open Trivia Database API** - Quiz questions and trivia
- **OpenStreetMap Overpass API** - Place type detection

## 🏗️ Project Structure

```
app/src/main/java/com/example/edai/
├── data/
│   ├── api/                 # API interfaces
│   │   ├── NominatimApi.kt
│   │   ├── WikipediaApi.kt
│   │   └── TriviaApi.kt
│   ├── model/               # Data models
│   │   ├── LocationModels.kt
│   │   ├── WikipediaModels.kt
│   │   └── TriviaModels.kt
│   ├── network/             # Network configuration
│   │   └── NetworkModule.kt
│   └── repository/          # Data repositories
│       ├── LocationRepository.kt
│       ├── PlaceRepository.kt
│       └── TriviaRepository.kt
├── location/                # Location services
│   └── LocationService.kt
├── ui/
│   ├── navigation/          # Navigation setup
│   │   └── EdaiNavigation.kt
│   ├── screens/             # UI screens
│   │   ├── HomeScreen.kt
│   │   ├── PlaceInfoScreen.kt
│   │   └── TriviaScreen.kt
│   ├── theme/               # UI theming
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/           # ViewModels
│       ├── LocationViewModel.kt
│       ├── PlaceViewModel.kt
│       └── TriviaViewModel.kt
├── utils/                   # Utility classes
│   ├── Constants.kt
│   └── NetworkUtils.kt
└── MainActivity.kt
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24+ (Android 7.0)
- Kotlin 2.0+

### Installation
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd EdaiApp
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Click "Open an Existing Project"
   - Navigate to the project directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### Permissions Required
- `ACCESS_FINE_LOCATION` - For GPS location detection
- `ACCESS_COARSE_LOCATION` - For network-based location
- `INTERNET` - For API calls
- `ACCESS_NETWORK_STATE` - For network status checking

## 📱 How to Use

### 1. Home Screen
- **Auto-detect Location**: Tap "Detect Current Location" to use GPS
- **Manual Search**: Use "Enter Location Manually" to search for places
- **Search Results**: Select from search suggestions

### 2. Place Information
- View comprehensive details about the selected location
- See Wikipedia descriptions and images
- Access coordinates and location metadata
- Navigate to quiz section

### 3. Knowledge Quiz
- **Choose Category**: Geography, History, or General Knowledge
- **Answer Questions**: 5 multiple-choice questions per quiz
- **View Results**: See score, correct answers, and performance
- **Retry Options**: Retake same category or try different topics

## 🔧 Configuration

### API Rate Limits
- **Nominatim API**: 1 request per second (automatic throttling implemented)
- **Wikipedia API**: No strict limits, but respectful usage
- **Open Trivia DB**: No limits on free tier

### Network Configuration
```kotlin
// In NetworkModule.kt
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("User-Agent", "EdaiApp/1.0 (Educational Travel App)")
        val request = requestBuilder.build()
        chain.proceed(request)
    }
    .build()
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- Repository layer tests
- ViewModel state management tests
- API response parsing tests

## 🔒 Privacy & Security

- **Location Data**: Only used for place information, never stored permanently
- **API Calls**: All data fetched from public, free APIs
- **No Personal Data**: App doesn't collect or store personal information
- **Network Security**: HTTPS enforced for all API calls

## 📊 Features in Detail

### Location Detection
- Uses Google's Fused Location Provider for best accuracy
- Fallback to network-based location if GPS unavailable
- Mock data fallback for testing and offline scenarios
- Proper error handling and user feedback

### Place Information
- Wikipedia integration for rich, educational content
- Image loading with caching via Coil
- Coordinate display and metadata
- Graceful fallback to mock data when APIs fail

### Interactive Quizzes
- Multiple categories with varying difficulty
- Score calculation and performance tracking
- Question shuffling for replayability
- Detailed result display with correct answers

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Maintain consistent formatting

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OpenStreetMap** for free geocoding services
- **Wikipedia** for educational content API
- **Open Trivia Database** for quiz questions
- **Google** for location services and Material Design
- **Jetpack Compose** team for modern UI toolkit

## 📞 Support

For support, questions, or feature requests:
- Open an issue on GitHub
- Contact the development team
- Check the documentation

---

**Made with ❤️ for educational exploration and discovery** 🌟