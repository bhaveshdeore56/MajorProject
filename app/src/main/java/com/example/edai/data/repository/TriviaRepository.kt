package com.example.edai.data.repository

import com.example.edai.data.api.TriviaApi
import com.example.edai.data.model.LocationInfo
import com.example.edai.data.model.QuizQuestion
import com.example.edai.data.model.TriviaQuestion
import com.example.edai.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder

class TriviaRepository(
    private val triviaApi: TriviaApi = NetworkModule.triviaApi
) {
    
    suspend fun getLocationSpecificQuestions(
        locationInfo: LocationInfo,
        amount: Int = 5
    ): Result<List<QuizQuestion>> {
        return withContext(Dispatchers.IO) {
            try {
                // First try to get place-specific questions
                val placeSpecificQuestions = generatePlaceSpecificQuestions(locationInfo)
                
                if (placeSpecificQuestions.isNotEmpty()) {
                    Result.success(placeSpecificQuestions.take(amount))
                } else {
                    // Fallback to generic questions related to the region
                    val fallbackQuestions = generateRegionalQuestions(locationInfo)
                    Result.success(fallbackQuestions.take(amount))
                }
            } catch (e: Exception) {
                // Final fallback to completely generic questions
                Result.success(getGenericQuestionsForLocation(locationInfo).take(amount))
            }
        }
    }
    
    private fun generatePlaceSpecificQuestions(locationInfo: LocationInfo): List<QuizQuestion> {
        val placeName = locationInfo.placeName
        val country = locationInfo.country?.lowercase() ?: ""
        val displayName = locationInfo.displayName?.lowercase() ?: ""
        
        return when {
            // Pune-specific questions
            placeName.contains("Pune", ignoreCase = true) -> getPuneSpecificQuestions()
            
            // Mumbai-specific questions  
            placeName.contains("Mumbai", ignoreCase = true) || 
            placeName.contains("Bombay", ignoreCase = true) -> getMumbaiSpecificQuestions()
            
            // Delhi-specific questions
            placeName.contains("Delhi", ignoreCase = true) -> getDelhiSpecificQuestions()
            
            // Bangalore-specific questions
            placeName.contains("Bangalore", ignoreCase = true) ||
            placeName.contains("Bengaluru", ignoreCase = true) -> getBangaloreSpecificQuestions()
            
            // Maharashtra-specific questions
            displayName.contains("maharashtra") -> getMaharashtraSpecificQuestions()
            
            // India-specific questions
            country.contains("india") -> getIndiaSpecificQuestions()
            
            // International cities
            placeName.contains("London", ignoreCase = true) -> getLondonSpecificQuestions()
            placeName.contains("Paris", ignoreCase = true) -> getParisSpecificQuestions()
            placeName.contains("New York", ignoreCase = true) -> getNewYorkSpecificQuestions()
            placeName.contains("Tokyo", ignoreCase = true) -> getTokyoSpecificQuestions()
            
            else -> emptyList()
        }
    }
    
    private fun getPuneSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What is Pune historically known as?",
                options = listOf("Poona", "Punaka", "Punyapura", "Puneri"),
                correctAnswer = "Poona"
            ),
            QuizQuestion(
                question = "Pune was the capital of which historical empire?",
                options = listOf("Mughal Empire", "Maratha Empire", "British Empire", "Vijayanagara Empire"),
                correctAnswer = "Maratha Empire"
            ),
            QuizQuestion(
                question = "Who founded the city of Pune?",
                options = listOf("Chhatrapati Shivaji", "Chhatrapati Shahu", "Balaji Bajirao", "Shahaji Bhosale"),
                correctAnswer = "Shahaji Bhosale"
            ),
            QuizQuestion(
                question = "In which year was Pune established as a city?",
                options = listOf("1600", "1647", "1674", "1700"),
                correctAnswer = "1647"
            ),
            QuizQuestion(
                question = "What does 'Pune' mean in Sanskrit?",
                options = listOf("City of Virtue", "Land of Prosperity", "Sacred Place", "River Junction"),
                correctAnswer = "City of Virtue"
            ),
            QuizQuestion(
                question = "Which river flows through Pune?",
                options = listOf("Godavari", "Krishna", "Mutha", "Tapi"),
                correctAnswer = "Mutha"
            ),
            QuizQuestion(
                question = "Pune is known as the 'Oxford of the East' because of its:",
                options = listOf("Historical monuments", "Educational institutions", "IT companies", "Cultural heritage"),
                correctAnswer = "Educational institutions"
            )
        )
    }
    
    private fun getMumbaiSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "Mumbai was previously known as:",
                options = listOf("Bombay", "Bambai", "Bom Bahia", "Mumbadevi"),
                correctAnswer = "Bombay"
            ),
            QuizQuestion(
                question = "When did Bombay officially become Mumbai?",
                options = listOf("1995", "1996", "1997", "1998"),
                correctAnswer = "1995"
            ),
            QuizQuestion(
                question = "Mumbai is named after which goddess?",
                options = listOf("Mahalakshmi", "Mumbadevi", "Durga", "Saraswati"),
                correctAnswer = "Mumbadevi"
            ),
            QuizQuestion(
                question = "How many islands was Mumbai originally built on?",
                options = listOf("5", "6", "7", "8"),
                correctAnswer = "7"
            ),
            QuizQuestion(
                question = "The Gateway of India was built to commemorate the visit of which British monarch?",
                options = listOf("Queen Victoria", "King George V", "King Edward VII", "Queen Elizabeth I"),
                correctAnswer = "King George V"
            )
        )
    }
    
    private fun getDelhiSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "How many cities have been built in the area of present-day Delhi?",
                options = listOf("5", "7", "9", "11"),
                correctAnswer = "7"
            ),
            QuizQuestion(
                question = "When was New Delhi inaugurated as India's capital?",
                options = listOf("1911", "1931", "1947", "1950"),
                correctAnswer = "1931"
            ),
            QuizQuestion(
                question = "Delhi was named after which king?",
                options = listOf("Raja Dhilu", "Emperor Humayun", "Prithviraj Chauhan", "Bahadur Shah"),
                correctAnswer = "Raja Dhilu"
            ),
            QuizQuestion(
                question = "Which river flows through Delhi?",
                options = listOf("Ganga", "Yamuna", "Saraswati", "Gomti"),
                correctAnswer = "Yamuna"
            ),
            QuizQuestion(
                question = "The Red Fort was built by which Mughal emperor?",
                options = listOf("Akbar", "Shah Jahan", "Humayun", "Aurangzeb"),
                correctAnswer = "Shah Jahan"
            )
        )
    }
    
    private fun getBangaloreSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "Bangalore was founded by which ruler?",
                options = listOf("Tipu Sultan", "Kempe Gowda", "Hyder Ali", "Krishnadevaraya"),
                correctAnswer = "Kempe Gowda"
            ),
            QuizQuestion(
                question = "In which year was Bangalore founded?",
                options = listOf("1537", "1547", "1557", "1567"),
                correctAnswer = "1537"
            ),
            QuizQuestion(
                question = "What does 'Bengaluru' mean?",
                options = listOf("City of Gardens", "Town of Boiled Beans", "Land of Warriors", "Place of Kings"),
                correctAnswer = "Town of Boiled Beans"
            ),
            QuizQuestion(
                question = "Bangalore is known as the:",
                options = listOf("Garden City", "Silicon Valley of India", "IT Capital", "All of the above"),
                correctAnswer = "All of the above"
            ),
            QuizQuestion(
                question = "When did Bangalore officially become Bengaluru?",
                options = listOf("2006", "2007", "2008", "2009"),
                correctAnswer = "2006"
            )
        )
    }
    
    private fun getMaharashtraSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "Maharashtra was formed on which date?",
                options = listOf("May 1, 1960", "May 1, 1961", "April 1, 1960", "June 1, 1960"),
                correctAnswer = "May 1, 1960"
            ),
            QuizQuestion(
                question = "What does 'Maharashtra' mean?",
                options = listOf("Great Nation", "Land of Marathas", "Great State", "Land of Warriors"),
                correctAnswer = "Great Nation"
            ),
            QuizQuestion(
                question = "Who is considered the founder of the Maratha Empire?",
                options = listOf("Shivaji Maharaj", "Sambhaji", "Rajaram", "Shahu"),
                correctAnswer = "Shivaji Maharaj"
            ),
            QuizQuestion(
                question = "Which is the state animal of Maharashtra?",
                options = listOf("Tiger", "Leopard", "Giant Squirrel", "Sambhar"),
                correctAnswer = "Giant Squirrel"
            ),
            QuizQuestion(
                question = "Maharashtra Day is celebrated on:",
                options = listOf("May 1", "March 30", "April 14", "June 1"),
                correctAnswer = "May 1"
            )
        )
    }
    
    private fun getIndiaSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "When did India gain independence?",
                options = listOf("August 15, 1947", "August 15, 1946", "July 15, 1947", "September 15, 1947"),
                correctAnswer = "August 15, 1947"
            ),
            QuizQuestion(
                question = "What does 'India' derive its name from?",
                options = listOf("River Indus", "Sanskrit word 'Hindu'", "Greek word 'Indoi'", "All of the above"),
                correctAnswer = "River Indus"
            ),
            QuizQuestion(
                question = "How many states are there in India currently?",
                options = listOf("28", "29", "30", "31"),
                correctAnswer = "28"
            ),
            QuizQuestion(
                question = "Which was India's first capital city?",
                options = listOf("Delhi", "Mumbai", "Kolkata", "Chennai"),
                correctAnswer = "Kolkata"
            ),
            QuizQuestion(
                question = "The name 'Bharat' comes from which ancient king?",
                options = listOf("King Bharata", "King Bharat", "Emperor Bharatvarsha", "King Dushyanta"),
                correctAnswer = "King Bharata"
            )
        )
    }
    
    private fun getLondonSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "London was founded by which ancient civilization?",
                options = listOf("Romans", "Saxons", "Vikings", "Celts"),
                correctAnswer = "Romans"
            ),
            QuizQuestion(
                question = "What was London originally called?",
                options = listOf("Londinium", "Lundenwic", "Londonium", "Lundinium"),
                correctAnswer = "Londinium"
            ),
            QuizQuestion(
                question = "In which year was London founded?",
                options = listOf("43 AD", "47 AD", "50 AD", "55 AD"),
                correctAnswer = "47 AD"
            ),
            QuizQuestion(
                question = "The River Thames flows through London. What does 'Thames' mean?",
                options = listOf("Dark River", "Flowing Water", "Sacred River", "Great Stream"),
                correctAnswer = "Dark River"
            ),
            QuizQuestion(
                question = "When did the Great Fire of London occur?",
                options = listOf("1665", "1666", "1667", "1668"),
                correctAnswer = "1666"
            )
        )
    }
    
    private fun getParisSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "Paris gets its name from which ancient tribe?",
                options = listOf("Parisii", "Parisi", "Gallic", "Belgae"),
                correctAnswer = "Parisii"
            ),
            QuizQuestion(
                question = "When was the Eiffel Tower completed?",
                options = listOf("1887", "1888", "1889", "1890"),
                correctAnswer = "1889"
            ),
            QuizQuestion(
                question = "What was Paris originally called?",
                options = listOf("Lutetia", "Parisia", "Lutecia", "Parisiorum"),
                correctAnswer = "Lutetia"
            ),
            QuizQuestion(
                question = "Paris is built around which river?",
                options = listOf("Loire", "Seine", "Rhone", "Garonne"),
                correctAnswer = "Seine"
            ),
            QuizQuestion(
                question = "How many arrondissements (districts) does Paris have?",
                options = listOf("18", "19", "20", "21"),
                correctAnswer = "20"
            )
        )
    }
    
    private fun getNewYorkSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "New York was originally called:",
                options = listOf("New Amsterdam", "New Holland", "New England", "New Britain"),
                correctAnswer = "New Amsterdam"
            ),
            QuizQuestion(
                question = "Who founded New Amsterdam (now New York)?",
                options = listOf("British", "Dutch", "French", "Spanish"),
                correctAnswer = "Dutch"
            ),
            QuizQuestion(
                question = "In which year was New Amsterdam renamed to New York?",
                options = listOf("1664", "1665", "1666", "1667"),
                correctAnswer = "1664"
            ),
            QuizQuestion(
                question = "New York was named after which Duke?",
                options = listOf("Duke of York", "Duke of Cambridge", "Duke of Sussex", "Duke of Kent"),
                correctAnswer = "Duke of York"
            ),
            QuizQuestion(
                question = "Manhattan was supposedly bought from Native Americans for:",
                options = listOf("$24 worth of goods", "$60 worth of goods", "$100 worth of goods", "$240 worth of goods"),
                correctAnswer = "$24 worth of goods"
            )
        )
    }
    
    private fun getTokyoSpecificQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "Tokyo was previously known as:",
                options = listOf("Edo", "Kyoto", "Osaka", "Yokohama"),
                correctAnswer = "Edo"
            ),
            QuizQuestion(
                question = "When was Edo renamed to Tokyo?",
                options = listOf("1867", "1868", "1869", "1870"),
                correctAnswer = "1868"
            ),
            QuizQuestion(
                question = "What does 'Tokyo' mean?",
                options = listOf("Eastern Capital", "Great City", "Imperial City", "New Capital"),
                correctAnswer = "Eastern Capital"
            ),
            QuizQuestion(
                question = "Tokyo became Japan's capital during which period?",
                options = listOf("Edo Period", "Meiji Restoration", "Taisho Period", "Showa Period"),
                correctAnswer = "Meiji Restoration"
            ),
            QuizQuestion(
                question = "The Tokyo Imperial Palace was built on the site of:",
                options = listOf("Edo Castle", "Kyoto Palace", "Osaka Castle", "Himeji Castle"),
                correctAnswer = "Edo Castle"
            )
        )
    }
    
    private fun generateRegionalQuestions(locationInfo: LocationInfo): List<QuizQuestion> {
        val country = locationInfo.country?.lowercase() ?: ""
        val displayName = locationInfo.displayName?.lowercase() ?: ""
        
        return when {
            country.contains("india") -> getIndiaSpecificQuestions()
            displayName.contains("maharashtra") -> getMaharashtraSpecificQuestions()
            country.contains("united states") || country.contains("usa") -> getUSAQuestions()
            country.contains("united kingdom") || country.contains("uk") -> getUKQuestions()
            country.contains("france") -> getFranceQuestions()
            country.contains("japan") -> getJapanQuestions()
            else -> getGenericQuestionsForLocation(locationInfo)
        }
    }
    
    private fun getUSAQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "When was the United States founded?",
                options = listOf("1775", "1776", "1777", "1778"),
                correctAnswer = "1776"
            ),
            QuizQuestion(
                question = "How many original colonies were there?",
                options = listOf("12", "13", "14", "15"),
                correctAnswer = "13"
            ),
            QuizQuestion(
                question = "What does 'America' derive its name from?",
                options = listOf("Amerigo Vespucci", "Christopher Columbus", "John America", "Native American word"),
                correctAnswer = "Amerigo Vespucci"
            )
        )
    }
    
    private fun getUKQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What does 'Britain' originally mean?",
                options = listOf("Land of the Painted", "Great Island", "Foggy Land", "Celtic Territory"),
                correctAnswer = "Land of the Painted"
            ),
            QuizQuestion(
                question = "When was the United Kingdom formed?",
                options = listOf("1707", "1801", "1603", "1707"),
                correctAnswer = "1707"
            )
        )
    }
    
    private fun getFranceQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "France gets its name from which ancient tribe?",
                options = listOf("Franks", "Gauls", "Celts", "Romans"),
                correctAnswer = "Franks"
            ),
            QuizQuestion(
                question = "When did the French Revolution begin?",
                options = listOf("1789", "1790", "1791", "1792"),
                correctAnswer = "1789"
            )
        )
    }
    
    private fun getJapanQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What does 'Japan' mean in Japanese?",
                options = listOf("Nippon/Nihon", "Yamato", "Wa", "Akitsushima"),
                correctAnswer = "Nippon/Nihon"
            ),
            QuizQuestion(
                question = "What does 'Nihon' mean?",
                options = listOf("Land of Rising Sun", "Island Nation", "Great Harmony", "Divine Country"),
                correctAnswer = "Land of Rising Sun"
            )
        )
    }
    
    private fun getGenericQuestionsForLocation(locationInfo: LocationInfo): List<QuizQuestion> {
        // Fallback generic questions that are still somewhat relevant
        return listOf(
            QuizQuestion(
                question = "What is the most important factor in naming historical places?",
                options = listOf("Geographical features", "Historical rulers", "Local legends", "All of the above"),
                correctAnswer = "All of the above"
            ),
            QuizQuestion(
                question = "Most ancient cities were built near:",
                options = listOf("Mountains", "Rivers", "Deserts", "Forests"),
                correctAnswer = "Rivers"
            ),
            QuizQuestion(
                question = "The suffix '-pur' in Indian city names usually means:",
                options = listOf("City", "Village", "Fort", "Market"),
                correctAnswer = "City"
            ),
            QuizQuestion(
                question = "The suffix '-bad' in city names often indicates:",
                options = listOf("Garden", "Settlement", "Fortress", "Market"),
                correctAnswer = "Settlement"
            ),
            QuizQuestion(
                question = "Ancient settlements were typically established based on:",
                options = listOf("Water availability", "Trade routes", "Defense advantages", "All of the above"),
                correctAnswer = "All of the above"
            )
        )
    }
    
    // Legacy method for backward compatibility
    suspend fun getTriviaQuestions(amount: Int = 5, category: TriviaCategory? = null): Result<List<QuizQuestion>> {
        return Result.success(getGenericTriviaQuestions(category))
    }
    
    private fun getGenericTriviaQuestions(category: TriviaCategory?): List<QuizQuestion> {
        return when (category) {
            TriviaCategory.GEOGRAPHY -> getMockGeographyQuestions()
            TriviaCategory.HISTORY -> getMockHistoryQuestions()
            else -> getMockGeneralQuestions()
        }
    }
    
    private fun getMockGeographyQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What is the capital of Australia?",
                options = listOf("Sydney", "Melbourne", "Canberra", "Perth"),
                correctAnswer = "Canberra"
            ),
            QuizQuestion(
                question = "Which is the longest river in the world?",
                options = listOf("Amazon", "Nile", "Mississippi", "Yangtze"),
                correctAnswer = "Nile"
            ),
            QuizQuestion(
                question = "Mount Everest is located in which mountain range?",
                options = listOf("Andes", "Alps", "Himalayas", "Rockies"),
                correctAnswer = "Himalayas"
            ),
            QuizQuestion(
                question = "Which desert is the largest in the world?",
                options = listOf("Sahara", "Gobi", "Antarctic", "Arabian"),
                correctAnswer = "Antarctic"
            ),
            QuizQuestion(
                question = "What is the smallest country in the world?",
                options = listOf("Monaco", "Vatican City", "San Marino", "Liechtenstein"),
                correctAnswer = "Vatican City"
            )
        )
    }
    
    private fun getMockHistoryQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "In which year did World War II end?",
                options = listOf("1944", "1945", "1946", "1947"),
                correctAnswer = "1945"
            ),
            QuizQuestion(
                question = "Who was the first person to walk on the moon?",
                options = listOf("Buzz Aldrin", "Neil Armstrong", "John Glenn", "Alan Shepard"),
                correctAnswer = "Neil Armstrong"
            ),
            QuizQuestion(
                question = "The ancient city of Troy was located in which modern-day country?",
                options = listOf("Greece", "Italy", "Turkey", "Egypt"),
                correctAnswer = "Turkey"
            ),
            QuizQuestion(
                question = "Which empire was ruled by Julius Caesar?",
                options = listOf("Greek Empire", "Roman Empire", "Persian Empire", "Byzantine Empire"),
                correctAnswer = "Roman Empire"
            ),
            QuizQuestion(
                question = "The Berlin Wall fell in which year?",
                options = listOf("1987", "1988", "1989", "1990"),
                correctAnswer = "1989"
            )
        )
    }
    
    private fun getMockGeneralQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                question = "What is the largest planet in our solar system?",
                options = listOf("Saturn", "Jupiter", "Neptune", "Earth"),
                correctAnswer = "Jupiter"
            ),
            QuizQuestion(
                question = "Which element has the chemical symbol 'O'?",
                options = listOf("Gold", "Silver", "Oxygen", "Iron"),
                correctAnswer = "Oxygen"
            ),
            QuizQuestion(
                question = "What is the fastest land animal?",
                options = listOf("Lion", "Cheetah", "Leopard", "Tiger"),
                correctAnswer = "Cheetah"
            ),
            QuizQuestion(
                question = "How many continents are there?",
                options = listOf("5", "6", "7", "8"),
                correctAnswer = "7"
            ),
            QuizQuestion(
                question = "What is the largest ocean on Earth?",
                options = listOf("Atlantic", "Indian", "Arctic", "Pacific"),
                correctAnswer = "Pacific"
            )
        )
    }
}

enum class TriviaCategory(val id: Int, val displayName: String) {
    GENERAL_KNOWLEDGE(9, "General Knowledge"),
    GEOGRAPHY(22, "Geography"),
    HISTORY(23, "History")
}
