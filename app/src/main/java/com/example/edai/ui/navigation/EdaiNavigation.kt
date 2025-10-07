package com.example.edai.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.edai.ui.screens.HomeScreen
import com.example.edai.ui.screens.PlaceInfoScreen
import com.example.edai.ui.screens.PopularPlacesScreen
import com.example.edai.ui.screens.PlaceDetailScreen
import com.example.edai.ui.screens.PlaceQuizScreen
import com.example.edai.ui.screens.TriviaScreen
import com.example.edai.ui.screens.SettingsScreen
import com.example.edai.ui.viewmodel.LocationViewModel
import com.example.edai.ui.viewmodel.PlaceInfoViewModel
import com.example.edai.ui.viewmodel.PopularPlacesViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlaceInfo : Screen("place_info")
    object Trivia : Screen("trivia")
    object PopularPlaces : Screen("popular_places")
    object Settings : Screen("settings")
    object PlaceDetail : Screen("place_detail/{placeId}") {
        fun createRoute(placeId: Int) = "place_detail/$placeId"
    }
    object PlaceQuiz : Screen("place_quiz/{placeId}") {
        fun createRoute(placeId: Int) = "place_quiz/$placeId"
    }
}

@Composable
fun EdaiNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val placeInfoViewModel: PlaceInfoViewModel = viewModel { PlaceInfoViewModel(context.applicationContext as Application) }
    val popularPlacesViewModel: PopularPlacesViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                locationViewModel = locationViewModel,
                onNavigateToPlace = { navController.navigate(Screen.PlaceInfo.route) },
                onNavigateToPopularPlaces = { navController.navigate(Screen.PopularPlaces.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.PlaceInfo.route) {
            PlaceInfoScreen(
                locationViewModel = locationViewModel,
                placeInfoViewModel = placeInfoViewModel,
                onNavigateToTrivia = { navController.navigate(Screen.Trivia.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Trivia.route) {
            TriviaScreen(
                placeInfoViewModel = placeInfoViewModel,
                locationInfo = locationViewModel.uiState.collectAsStateWithLifecycle().value.currentLocation,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PopularPlaces.route) {
            PopularPlacesScreen(
                viewModel = popularPlacesViewModel,
                onNavigateToPlaceDetail = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: return@composable
            PlaceDetailScreen(
                placeId = placeId,
                viewModel = popularPlacesViewModel,
                onNavigateToQuiz = { navController.navigate(Screen.PlaceQuiz.createRoute(placeId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PlaceQuiz.route,
            arguments = listOf(navArgument("placeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: return@composable
            
            // Reset quiz state when entering quiz screen to ensure clean start
            LaunchedEffect(placeId) {
                popularPlacesViewModel.resetQuiz()
            }
            
            PlaceQuizScreen(
                placeId = placeId,
                viewModel = popularPlacesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
