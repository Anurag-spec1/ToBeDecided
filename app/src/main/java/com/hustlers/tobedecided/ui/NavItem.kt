package com.hustlers.tobedecided.ui

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val title: String,
    val iconVector: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null
) {
    data object LiveMap : NavItem("map", "Map", Icons.Filled.LocationOn)
    data object Alerts : NavItem("alerts", "Alerts", Icons.Filled.Notifications)
    data object Family : NavItem("family", "Family", Icons.Filled.Person)
    data object Home : NavItem("home", "Home", Icons.Filled.Home)
    data object Sos : NavItem("sos", "SOS", Icons.Filled.Warning)
    data object Settings : NavItem("settings", "Settings", Icons.Filled.Settings)
}