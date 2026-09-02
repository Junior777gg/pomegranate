package org.unstabledev.pomegranate

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.unstabledev.pomegranate.Repository.currentCall
import org.unstabledev.pomegranate.Repository.pomegranatePath
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.screen.BetterCallSoulScreen
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
    PlatformKeyEvents.Instance = PlatformKeyEvents()
    Repository.messagesDao = messagesDao
    Repository.chatDao = chatDao
    if (currentCall.value != null) {
        navController.navigate(Routes.CALL_SCREEN)
    }
    var startDestination: String
    val fistFilePath = remember { Repository.fistFilePath }
    if (KMPFile(fistFilePath).exists()) {
        KMPFile("$pomegranatePath${separator}temp").createNewFile()
        startDestination = if (KMPFile(fistFilePath).kmpReadText() != "") Routes.HOME_SCREEN
        else Routes.WELCOME_SCREEN
    } else {
        if (KMPFile(Repository.pomegranatePath).exists()) {
            KMPFile("$pomegranatePath${separator}temp").createNewFile()
            KMPFile(fistFilePath).createNewFile()
        } else {
            KMPFile(Repository.pomegranatePath).mkdir()
            KMPFile("$pomegranatePath${separator}temp").createNewFile()
            KMPFile(fistFilePath).createNewFile()
        }
        startDestination = Routes.WELCOME_SCREEN
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = {  }
                )
            }
            WelcomeScreen(navWayObj)
        }
        composable(Routes.LOGIN_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.navigate(Routes.WELCOME_SCREEN) }
                )
            }
            LoginScreen(navWayObj)
        }
        composable(Routes.HOME_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String ->
                        navController.navigate(route)
                        PlatformKeyEvents.Instance?.onBackCallback = {}
                    },
                    back = {}
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
        composable(
            Routes.CHAT_SCREEN,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
        ) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.navigate(Routes.HOME_SCREEN) }
                )
            }
            ChatScreen(navWayObj, chatDao)
        }
        composable(Routes.PROFILE_SCREEN_ROUTE) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            ProfileScreen(navWayObj)
        }
        composable(Routes.CALL_SCREEN) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            BetterCallSoulScreen(navWayObj)
        }
    }
}