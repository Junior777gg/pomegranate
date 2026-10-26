package org.unstabledev.pomegranate.screen.nav

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
import androidx.navigation.toRoute
import org.unstabledev.pomegranate.platform.KMPFile
import org.unstabledev.pomegranate.platform.PlatformEvents
import org.unstabledev.pomegranate.Repository
import org.unstabledev.pomegranate.Repository.currentCall
import org.unstabledev.pomegranate.Repository.pomegranatePath
import org.unstabledev.pomegranate.common.AppSettings
import org.unstabledev.pomegranate.common.DraftManager
import org.unstabledev.pomegranate.database.ChatDao
import org.unstabledev.pomegranate.database.MessagesDao
import org.unstabledev.pomegranate.database.PersonDao
import org.unstabledev.pomegranate.platform.isLandscape
import org.unstabledev.pomegranate.platform.isMobile
import org.unstabledev.pomegranate.platform.kmpReadText
import org.unstabledev.pomegranate.screen.BetterCallSoulScreen
import org.unstabledev.pomegranate.screen.ChatScreen
import org.unstabledev.pomegranate.screen.ContactsScreen
import org.unstabledev.pomegranate.screen.DesktopHomeScreen
import org.unstabledev.pomegranate.screen.FirebaseAddressSelectScreen
import org.unstabledev.pomegranate.screen.SwitchAccountScreen
import org.unstabledev.pomegranate.screen.HomeScreen
import org.unstabledev.pomegranate.screen.ProfileScreen
import org.unstabledev.pomegranate.screen.settings.SettingsScreen
import org.unstabledev.pomegranate.screen.WelcomeScreen
import org.unstabledev.pomegranate.screen.settings.NetworkSettingsScreen
import org.unstabledev.pomegranate.screen.settings.PowerSaveSettingsScreen
import org.unstabledev.pomegranate.screen.settings.StorageSettingsScreen
import org.unstabledev.pomegranate.screen.settings.StyleSettingsScreen
import org.unstabledev.pomegranate.platform.separator
import org.unstabledev.pomegranate.screen.ImagePreviewScreen
import org.unstabledev.pomegranate.screen.settings.DeveloperSettingsScreen

@Composable
fun applyScreenPadding(base: Modifier = Modifier): Modifier {
    val mod = base.padding(bottom = if(isMobile) 12.dp else 0.dp, top = if(isLandscape()) 30.dp else 0.dp)
    return mod.displayCutoutPadding()
}

@Composable
fun Navigation(navController: NavHostController, chatDao: ChatDao, messagesDao: MessagesDao, personsDao: PersonDao) {
    PlatformEvents.Instance = PlatformEvents()
    Repository.messagesDao = messagesDao
    Repository.chatDao = chatDao
    Repository.personsDao = personsDao
    if (currentCall.value != null) {
        navController.navigate(Routes.Call())
    }
    var startDestination: Routes
    val fistFilePath = remember { Repository.fistFilePath }
    if (KMPFile(fistFilePath).exists()) {
        KMPFile("$pomegranatePath${separator}temp").createNewFile()
        startDestination = if (KMPFile(fistFilePath).kmpReadText() != "") Routes.Home()
        else Routes.Onboarding()
    } else {
        if (KMPFile(pomegranatePath).exists()) {
            KMPFile("$pomegranatePath${separator}temp").createNewFile()
            KMPFile(fistFilePath).createNewFile()
        } else {
            KMPFile(pomegranatePath).mkdir()
            KMPFile("$pomegranatePath${separator}temp").createNewFile()
            KMPFile(fistFilePath).createNewFile()
        }
        startDestination = Routes.Onboarding()
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Routes.Onboarding>{
            WelcomeScreen(navController)
        }
        composable<Routes.SwitchAccount> {
            SwitchAccountScreen(navController)
        }
        composable<Routes.Home> {
            if (isMobile) HomeScreen(navController)
            else DesktopHomeScreen(navController)
        }
        composable<Routes.Contacts>{
            ContactsScreen(navController)
        }
        composable<Routes.Settings>{
            SettingsScreen(navController)
        }
        composable<Routes.SettingsStyle>{
            StyleSettingsScreen(navController)
        }
        composable<Routes.SettingsNetwork>{
            NetworkSettingsScreen(navController)
        }
        composable<Routes.SettingsPowerSave>{
            PowerSaveSettingsScreen(navController)
        }
        composable<Routes.SettingsStorage>{
            StorageSettingsScreen(navController)
        }
        composable<Routes.SettingsDeveloper>{
            DeveloperSettingsScreen(navController)
        }
        composable<Routes.SettingsSelectFirebase>{
            FirebaseAddressSelectScreen(navController)
        }
        composable<Routes.Chat>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
        ) {
            val route = it.toRoute<Routes.Chat>()
            ChatScreen(navController, route.chatId, onChatDelete = {
                DraftManager.drafts.remove(route.chatId)
                DraftManager.save()
                AppSettings.removePinnedChat(route.chatId)
            })
        }
        composable<Routes.Profile>{
            val route = it.toRoute<Routes.Profile>()
            ProfileScreen(navController, route.chatId, route.email)
        }
        composable<Routes.Call>{
            BetterCallSoulScreen(navController)
        }
        composable<Routes.ImagePreview>{
            val route = it.toRoute<Routes.ImagePreview>()
            ImagePreviewScreen(route.messageId, navController)
        }
    }
}