package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.AermosDarkBackground
import com.example.ui.theme.AermosDarkSurface
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.theme.AermosTheme
import com.example.ui.util.AermosHaptics
import com.example.ui.viewmodel.AermosTab
import com.example.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AermosTheme {
                AermosApp()
            }
        }
    }
}

@Composable
fun AermosApp(
    viewModel: WeatherViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()

    // Handle back button on sub-screens to return to Home
    BackHandler(enabled = currentTab != AermosTab.HOME) {
        AermosHaptics.click(context)
        viewModel.selectTab(AermosTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AermosDarkBackground,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = AermosDarkSurface.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                AermosTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                AermosHaptics.click(context)
                                viewModel.selectTab(tab)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AermosTab.HOME -> if (selected) Icons.Rounded.Cloud else Icons.Rounded.CloudQueue
                                    AermosTab.RADAR -> Icons.Rounded.Radar
                                    AermosTab.AIR -> Icons.Rounded.Air
                                    AermosTab.LOCATIONS -> if (selected) Icons.Rounded.LocationCity else Icons.Rounded.Apartment
                                    AermosTab.SETTINGS -> if (selected) Icons.Rounded.Tune else Icons.Rounded.Settings
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF041E34),
                            selectedTextColor = AermosSkyBlue,
                            indicatorColor = AermosSkyBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    AermosTab.HOME -> HomeScreen(viewModel = viewModel)
                    AermosTab.RADAR -> RadarMapScreen(viewModel = viewModel)
                    AermosTab.AIR -> AirQualityScreen(viewModel = viewModel)
                    AermosTab.LOCATIONS -> LocationsScreen(viewModel = viewModel)
                    AermosTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
