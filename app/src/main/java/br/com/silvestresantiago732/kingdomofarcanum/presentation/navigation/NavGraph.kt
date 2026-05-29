package br.com.silvestresantiago732.kingdomofarcanum.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.silvestresantiago732.kingdomofarcanum.presentation.character_detail.CharacterDetailScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.character_image.CharacterImageScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.character_list.CharacterListScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.character_sheet.CharacterSheetScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.login.LoginScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.register.RegisterScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.settings.SettingsScreen
import br.com.silvestresantiago732.kingdomofarcanum.presentation.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object CharacterImage : Screen("character_image/{characterId}") {
        fun createRoute(characterId: String) = "character_image/$characterId"
    }
    object CharacterSheet : Screen("character_sheet/{characterId}") {
        fun createRoute(characterId: String) = "character_sheet/$characterId"
    }
    object CharacterDetail : Screen("character_detail/{characterId}") {
        fun createRoute(characterId: String) = "character_detail/$characterId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            CharacterListScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.CharacterSheet.createRoute(id))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.CharacterDetail.createRoute("new"))
                }
            )
        }
        composable(Screen.CharacterSheet.route) {
            CharacterSheetScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.CharacterDetail.createRoute(id))
                },
                onNavigateToImage = { id ->
                    navController.navigate(Screen.CharacterImage.createRoute(id))
                }
            )
        }
        composable(Screen.CharacterImage.route) {
            CharacterImageScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CharacterDetail.route) {
            CharacterDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
