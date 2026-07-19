package org.unstabledev.pomegranate

import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.screen.ChatScreen
import org.unstabledev.pomegranate.screen.ContactsScreen
import org.unstabledev.pomegranate.screen.DesktopHomeScreen
import org.unstabledev.pomegranate.screen.FirebaseAddressSelectScreen
import org.unstabledev.pomegranate.screen.LoginScreen
import org.unstabledev.pomegranate.screen.HomeScreen
import org.unstabledev.pomegranate.screen.ProfileScreen
import org.unstabledev.pomegranate.screen.SettingsScreen
import org.unstabledev.pomegranate.screen.WelcomeScreen

@Composable
fun applyScreenPadding(base: Modifier = Modifier): Modifier {
    val mod = base.padding(bottom = if(isMobile) 12.dp else 0.dp, top = if(isLandscape()) 30.dp else 0.dp)
    return mod.displayCutoutPadding()
}

@Composable
fun Navigation(navController: NavHostController, chatDao: ChatDao, messagesDao: MessagesDao) {
    Repository.messagesDao = messagesDao
    var startDestination: String
    val fistFilePath = remember { "pomegranate${File.sep}auth.txt" }
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
                    back = { }
                )
            }
            if (isMobile) HomeScreen(navWayObj, chatDao)
            else DesktopHomeScreen(navWayObj, chatDao)
        }
        composable(Routes.CONTACTS_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            ContactsScreen(navWayObj, chatDao)
        }
        composable(Routes.SETTINGS_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            SettingsScreen(navWayObj, chatDao)
        }
        composable(Routes.SETTINGS_SELECT_FIREBASE_SCREEN) {
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
            ChatScreen(navWayObj)
        }
        composable(Routes.PROFILE_SCREEN_ROUTE) {
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