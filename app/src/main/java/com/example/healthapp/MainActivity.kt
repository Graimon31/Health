package com.example.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.healthapp.di.AppModule
import com.example.healthapp.ui.analytics.AnalyticsScreen
import com.example.healthapp.ui.analytics.AnalyticsViewModel
import com.example.healthapp.ui.history.HistoryScreen
import com.example.healthapp.ui.history.HistoryViewModel
import com.example.healthapp.ui.scan.ScanScreen
import com.example.healthapp.ui.scan.ScanViewModel
import com.example.healthapp.ui.settings.SettingsScreen
import com.example.healthapp.ui.settings.SettingsViewModel
import com.example.healthapp.ui.theme.HealthTheme

/**
 * Hosts the Compose navigation graph with four bottom tabs.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthTheme {
                HealthApp()
            }
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

@Composable
private fun HealthApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val destinations = listOf(
        NavItem("scan", "Датчик", android.R.drawable.ic_menu_search),
        NavItem("history", "История", android.R.drawable.ic_menu_recent_history),
        NavItem("analytics", "Аналитика", android.R.drawable.ic_menu_compass),
        NavItem("settings", "Настройки", android.R.drawable.ic_menu_manage)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                destinations.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(id = item.iconRes), contentDescription = item.label) },
                        label = { Text(text = item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "scan",
            modifier = Modifier.padding(padding)
        ) {
            composable("scan") {
                val viewModel: ScanViewModel = viewModel(factory = AppModule.scanViewModelFactory())
                ScanScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
            composable("history") {
                val viewModel: HistoryViewModel = viewModel(factory = AppModule.historyViewModelFactory())
                HistoryScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
            composable("analytics") {
                val viewModel: AnalyticsViewModel = viewModel(factory = AppModule.analyticsViewModelFactory())
                AnalyticsScreen(viewModel = viewModel)
            }
            composable("settings") {
                val viewModel: SettingsViewModel = viewModel(factory = AppModule.settingsViewModelFactory())
                SettingsScreen(viewModel = viewModel, snackbarHostState = snackbarHostState)
            }
        }
    }
}
