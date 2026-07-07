package com.hustlers.tobedecided.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val title: String,
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null
) {
    object Home : NavItem("home", "Home", iconVector = Icons.Default.Home)
    object Apps : NavItem("apps", "Apps", iconVector = Icons.Rounded.Apps)
    object Profile : NavItem("profile", "Profile", iconVector = Icons.Default.Person)
}