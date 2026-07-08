package com.hustlers.tobedecided.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityMainBinding
import com.hustlers.tobedecided.fragment.AppScreen
import com.hustlers.tobedecided.fragment.HomeScreen
import com.hustlers.tobedecided.fragment.ProfileScreen
import com.hustlers.tobedecided.ui.NavItem
import com.hustlers.tobedecided.ui.components.GlassmorphicBottomBar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentRoute by mutableStateOf("home")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeScreen())
        }

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
                        is NavItem.Home -> {
                            loadFragment(HomeScreen())
                        }
                        is NavItem.Apps -> {
                            loadFragment(AppScreen())
                        }
                        is NavItem.Profile -> {
                            loadFragment(ProfileScreen())
                        }
                    }
                }
            )
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}