package com.hhkv.rustitok.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.*
import com.hhkv.rustitok.screen.HomeScreen
import com.hhkv.rustitok.screen.HistoryScreen
import com.hhkv.rustitok.screen.SettingScreen
import com.hhkv.rustitok.utils.LocalNavController


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(navController = navController, startDestination = "home") {
                composable(NavigationRoutes.HOME) { HomeScreen() }
                composable(NavigationRoutes.HISTORY) { HistoryScreen() }
                composable(NavigationRoutes.SETTINGS) {  SettingScreen() }
            }
    }
}