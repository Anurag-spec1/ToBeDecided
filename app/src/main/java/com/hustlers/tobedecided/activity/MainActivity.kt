package com.hustlers.tobedecided.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityMainBinding
import com.hustlers.tobedecided.ui.NavItem
import com.hustlers.tobedecided.ui.components.GlassmorphicBottomBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentRoute by mutableStateOf("home")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navItems = listOf(
            NavItem.Apps,
            NavItem.Home,
            NavItem.Profile
        )

        binding.composeBottomNav.setContent {
            GlassmorphicBottomBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    currentRoute = item.route
                    when (item) {
                        is NavItem.Home -> { /* Navigate to home */ }
                        is NavItem.Apps -> { /* Navigate to team */ }
                        is NavItem.Profile -> { /* Navigate to profile */ }
                    }
                }
            )
        }
    }
}