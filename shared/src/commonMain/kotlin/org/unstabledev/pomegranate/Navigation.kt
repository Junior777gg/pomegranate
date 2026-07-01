package org.unstabledev.pomegranate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.unstabledev.pomegranate.database.ChatDao


@Composable
fun Navigation(navController: NavHostController, chatDao: ChatDao) {
    var startDestination = remember { Routes.FIRST_SCREEN_ROUTE}
    val fistFilePath = remember { "pomegranate${File.sep}firstFile.txt"}
    if (File(fistFilePath).exists()) {
        if (File(fistFilePath).readText() != "") {
            startDestination = Routes.HOME_SCREEN_ROUTE
        }else{
            startDestination = Routes.FIRST_SCREEN_ROUTE
        }
    } else {
        if (File("pomegranate").exists()) {
            File(fistFilePath).createFile()
        } else {
            File("pomegranate").createDirectory()
            File(fistFilePath).createFile()
        }
        startDestination = Routes.FIRST_SCREEN_ROUTE
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.FIRST_SCREEN_ROUTE) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            FirstScreen(navWayObj)
        }
        composable(Routes.HOME_SCREEN_ROUTE) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            MainScreen(navWayObj, chatDao)
        }
        composable(Routes.CONTACTS_SCREEN_ROUTE){
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.popBackStack() }
                )
            }
            ContactsScreen(navWayObj)
        }
        composable(Routes.CHAT_SCREEN_ROUTE) {
            val navWayObj = remember {
                NavigationWays(
                    goTo = { route: String -> navController.navigate(route) },
                    back = { navController.navigate(Routes.HOME_SCREEN_ROUTE) }
                )
            }
            ChatScreen(navWayObj)
        }
    }
}