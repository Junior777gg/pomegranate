package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.screen.ChatScreen
import org.unstabledev.pomegranate.screen.ContactsScreen
import org.unstabledev.pomegranate.screen.FirebaseAddressSelectScreen
import org.unstabledev.pomegranate.screen.LoginScreen
import org.unstabledev.pomegranate.screen.HomeScreen
import org.unstabledev.pomegranate.screen.ProfileScreen
import org.unstabledev.pomegranate.screen.SettingsScreen
import org.unstabledev.pomegranate.screen.WelcomeScreen


@Composable
fun Navigation(navController: NavHostController, chatDao: ChatDao, messagesDao: MessagesDao) {
    var startDestination: String
    val fistFilePath = remember { "pomegranate${File.sep}auth.txt"}
    if (File(fistFilePath).exists()) {
        startDestination = if (File(fistFilePath).readText() != "") {
            Routes.HOME_SCREEN
        } else {
            Routes.WELCOME_SCREEN
        }
    } else {
        if (File("pomegranate").exists()) {
            File(fistFilePath).createFile()
        } else {
            File("pomegranate").createDirectory()
            File(fistFilePath).createFile()
        }
        startDestination = Routes.WELCOME_SCREEN
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            WelcomeScreen(navWayObj)
        }
        composable(Routes.LOGIN_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            LoginScreen(navWayObj)
        }
        composable(Routes.HOME_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            HomeScreen(navWayObj, chatDao, messagesDao)
        }
        composable(Routes.CONTACTS_SCREEN){
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            ContactsScreen(navWayObj)
        }
        composable(Routes.SETTINGS_SCREEN){
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            SettingsScreen(navWayObj)
        }
        composable(Routes.SETTINGS_SELECT_FIREBASE_SCREEN){
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            FirebaseAddressSelectScreen(navWayObj)
        }
        composable(Routes.CHAT_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.navigate(Routes.HOME_SCREEN) }
                )
            }
            ChatScreen(navWayObj, messagesDao)
        }
        composable(Routes.PROFILE_SCREEN_ROUTE){
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.navigate(Routes.HOME_SCREEN) }
                )
            }
            ProfileScreen(navWayObj)
        }
    }
}