# API Documentation

## Overview
This document describes the APIs used in the Edai app and their integration patterns.

## 1. OpenStreetMap Nominatim API

### Base URL
```
https://nominatim.openstreetmap.org/
```

### Endpoints

#### Reverse Geocoding
**GET** `/reverse`
- **Purpose**: Convert coordinates to place names
- **Parameters**:
  - `lat`: Latitude (required)
  - `lon`: Longitude (required)
  - `format`: Response format (json)
  - `addressdetails`: Include address breakdown (1)

**Example Request**:
```
GET /reverse?lat=18.5204&lon=73.8567&format=json&addressdetails=1
```

**Example Response**:
```json
{
  "place_id": 259127396,
  "licence": "Data © OpenStreetMap contributors, ODbL 1.0...",
  "lat": "18.5204303",
  "lon": "73.8567437",
  "display_name": "Pune, Maharashtra, India",
  "address": {
    "city": "Pune",
    "state": "Maharashtra",
    "country": "India",
    "country_code": "in"
  }
}
```

#### Forward Geocoding
**GET** `/search`
- **Purpose**: Search for places by name
- **Parameters**:
  - `q`: Search query (required)
  - `format`: Response format (json)
  - `limit`: Maximum results (5)
  - `addressdetails`: Include address breakdown (1)

**Example Request**:
```
GET /search?q=Pune&format=json&limit=5&addressdetails=1
```

### Rate Limiting
- 1 request per second
- Respectful usage policy
- User-Agent header required

### Implementation
```kotlin
interface NominatimApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1
    ): Response<NominatimResult>
    
    @GET("search")
    suspend fun forwardGeocode(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("addressdetails") addressDetails: Int = 1
    ): Response<List<NominatimResult>>
}
```

## 2. Wikipedia REST API

### Base URL
```
https://en.wikipedia.org/api/rest_v1/
```

### Endpoints

#### Page Summary
**GET** `/page/summary/{title}`
- **Purpose**: Get summary information about a Wikipedia page
- **Parameters**:
  - `title`: Page title (URL encoded)

**Example Request**:
```
GET /page/summary/Pune
```

**Example Response**:
```json
{
  "type": "standard",
  "title": "Pune",
  "displaytitle": "Pune",
  "extract": "Pune is a sprawling city in the western Indian state of Maharashtra...",
  "thumbnail": {
    "source": "https://upload.wikimedia.org/wikipedia/commons/thumb/...",
    "width": 320,
    "height": 240
  },
  "content_urls": {
    "desktop": {
      "page": "https://en.wikipedia.org/wiki/Pune"
    }
  }
}
```

### Implementation
```kotlin
interface WikipediaApi {
    @GET("page/summary/{title}")
    suspend fun getPageSummary(
        @Path("title") title: String
    ): Response<WikipediaResponse>
}
```

## 3. Open Trivia Database API

### Base URL
```
https://opentdb.com/
```

### Endpoints

#### Get Questions
**GET** `/api.php`
- **Purpose**: Retrieve trivia questions
- **Parameters**:
  - `amount`: Number of questions (1-50)
  - `category`: Category ID (optional)
  - `type`: Question type (multiple/boolean)
  - `difficulty`: easy/medium/hard (optional)

**Example Request**:
```
GET /api.php?amount=5&category=22&type=multiple
```

**Example Response**:
```json
{
  "response_code": 0,
  "results": [
    {
      "category": "Geography",
      "type": "multiple",
      "difficulty": "medium",
      "question": "What is the capital of Australia?",
      "correct_answer": "Canberra",
      "incorrect_answers": ["Sydney", "Melbourne", "Perth"]
    }
  ]
}
```

### Categories
- 9: General Knowledge
- 22: Geography
- 23: History

### Implementation
```kotlin
interface TriviaApi {
    @GET("api.php")
    suspend fun getTriviaQuestions(
        @Query("amount") amount: Int = 5,
        @Query("category") category: Int? = null,
        @Query("type") type: String = "multiple",
        @Query("difficulty") difficulty: String? = null
    ): Response<TriviaResponse>
}
```

## 4. Error Handling

### Network Errors
```kotlin
try {
    val response = api.getData()
    if (response.isSuccessful) {
        // Handle success
    } else {
        // Handle API error
    }
} catch (e: Exception) {
    // Handle network/parsing errors
    when (e) {
        is IOException -> // Network error
        is HttpException -> // HTTP error
        else -> // Unknown error
    }
}
```

### Fallback Strategy
1. Try primary API endpoint
2. On failure, try alternative approach if available
3. Fall back to cached/mock data
4. Display appropriate error message to user

### Retry Logic
```kotlin
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> = try {
    Result.success(apiCall())
} catch (e: Exception) {
    Result.failure(e)
}
```

## 5. Best Practices

### API Usage
- Always include User-Agent header
- Respect rate limits
- Handle errors gracefully
- Implement proper timeouts
- Use HTTPS only

### Data Processing
- Validate API responses
- Handle null/missing fields
- Sanitize HTML content
- Cache appropriate data

### Performance
- Use coroutines for async operations
- Implement proper loading states
- Optimize image loading
- Minimize API calls

## 6. Testing

### Mock Responses
For testing, use mock data that matches API response structure:

```kotlin
val mockNominatimResult = NominatimResult(
    lat = "18.5204",
    lon = "73.8567",
    displayName = "Pune, Maharashtra, India",
    // ... other fields
)
```

### Integration Tests
Test API integration with actual endpoints in controlled environment.

### Unit Tests
Mock API interfaces for testing repository and viewmodel logic.
