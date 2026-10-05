package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.SavedLocationEntity
import com.example.data.model.GeocodingResult
import com.example.ui.components.AermosWeatherIcon
import com.example.ui.components.AermosWordmark
import com.example.ui.theme.AermosAtmosphereTeal
import com.example.ui.theme.AermosDarkSurface
import com.example.ui.theme.AermosDarkSurfaceVariant
import com.example.ui.theme.AermosSkyBlue
import com.example.ui.util.AermosHaptics
import com.example.ui.viewmodel.LocationTarget
import com.example.ui.viewmodel.WeatherViewModel

@Composable
fun LocationsScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val activeLocation by viewModel.activeLocation.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            AermosHaptics.heavyClick(context)
            viewModel.fetchCurrentGpsLocation()
            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AermosWordmark(fontSize = 17, showSubtitle = true)
            Text(
                text = "OBSERVATION STATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = AermosSkyBlue,
                letterSpacing = 1.sp
            )
        }

        // Global City Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search city, region, or country...") },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = AermosSkyBlue
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AermosSkyBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = AermosDarkSurfaceVariant.copy(alpha = 0.6f),
                unfocusedContainerColor = AermosDarkSurfaceVariant.copy(alpha = 0.4f)
            ),
            singleLine = true
        )

        // GPS Location Detection Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AermosSkyBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "GPS",
                            tint = AermosSkyBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CURRENT DEVICE LOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = AermosSkyBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (hasLocationPermission) "Detect exact local atmospheric telemetry" else "Location permission allows live micro-climate tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        if (hasLocationPermission) {
                            viewModel.fetchCurrentGpsLocation()
                            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AermosSkyBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (hasLocationPermission) "Locate" else "Enable",
                        color = Color(0xFF041E34),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Search Results Dropdown / List
        if (searchResults.isNotEmpty()) {
            Text(
                text = "SEARCH MATCHES (${searchResults.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { result ->
                    SearchResultItem(
                        result = result,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.selectSearchResult(result)
                        }
                    )
                }
            }
        } else {
            // Saved Locations List
            Text(
                text = "SAVED STATIONS & FAVORITES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            if (savedLocations.isEmpty()) {
                // Preset atmospheric hubs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetCityItem(
                        name = "Tokyo",
                        country = "Japan",
                        lat = 35.6762,
                        lon = 139.6503,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.loadWeatherForTarget(
                                LocationTarget("Tokyo", "Kanto", "Japan", 35.6762, 139.6503)
                            )
                            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                        }
                    )
                    PresetCityItem(
                        name = "London",
                        country = "United Kingdom",
                        lat = 51.5074,
                        lon = -0.1278,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.loadWeatherForTarget(
                                LocationTarget("London", "England", "United Kingdom", 51.5074, -0.1278)
                            )
                            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                        }
                    )
                    PresetCityItem(
                        name = "Zurich",
                        country = "Switzerland",
                        lat = 47.3769,
                        lon = 8.5417,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.loadWeatherForTarget(
                                LocationTarget("Zurich", "Canton Zurich", "Switzerland", 47.3769, 8.5417)
                            )
                            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                        }
                    )
                    PresetCityItem(
                        name = "San Francisco",
                        country = "United States",
                        lat = 37.7749,
                        lon = -122.4194,
                        onClick = {
                            AermosHaptics.click(context)
                            viewModel.loadWeatherForTarget(
                                LocationTarget("San Francisco", "California", "United States", 37.7749, -122.4194)
                            )
                            viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedLocations, key = { it.id }) { loc ->
                        SavedLocationItem(
                            location = loc,
                            isActive = loc.name == activeLocation.name,
                            onSelect = {
                                AermosHaptics.click(context)
                                viewModel.loadWeatherForTarget(
                                    LocationTarget(
                                        name = loc.name,
                                        region = loc.region,
                                        country = loc.country,
                                        latitude = loc.latitude,
                                        longitude = loc.longitude,
                                        isCurrentGps = loc.isCurrentLocation
                                    )
                                )
                                viewModel.selectTab(com.example.ui.viewmodel.AermosTab.HOME)
                            },
                            onDelete = {
                                AermosHaptics.heavyClick(context)
                                viewModel.deleteSavedLocation(loc.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(result: GeocodingResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AermosDarkSurface.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = result.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val sub = listOfNotNull(result.admin1, result.country).joinToString(", ")
            Text(
                text = sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.AddLocationAlt,
            contentDescription = "Select",
            tint = AermosSkyBlue
        )
    }
}

@Composable
private fun SavedLocationItem(
    location: SavedLocationEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isActive) AermosSkyBlue.copy(alpha = 0.6f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isActive) AermosDarkSurfaceVariant else AermosDarkSurface.copy(alpha = 0.7f))
            .clickable(onClick = onSelect)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (location.isCurrentLocation) {
                    Text(
                        text = "• CURRENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = AermosSkyBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val desc = listOfNotNull(location.region.takeIf { it.isNotEmpty() }, location.country.takeIf { it.isNotEmpty() })
                .joinToString(", ")
            if (desc.isNotEmpty()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Rounded.DeleteOutline,
                contentDescription = "Remove station",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PresetCityItem(
    name: String,
    country: String,
    lat: Double,
    lon: Double,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AermosDarkSurface.copy(alpha = 0.7f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = country,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = "Switch",
            tint = AermosSkyBlue
        )
    }
}
