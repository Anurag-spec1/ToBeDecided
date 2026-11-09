package com.hustlers.tobedecided.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.Fragment
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.databinding.ActivityMainBinding
import com.hustlers.tobedecided.fragment.MemberHomeScreen
import com.hustlers.tobedecided.fragment.PlaceholderScreen
import com.hustlers.tobedecided.fragment.SettingsScreen
import com.hustlers.tobedecided.ui.NavItem
import com.hustlers.tobedecided.ui.components.GlassmorphicBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navItems: List<NavItem>
    private var currentRoute by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isAdmin = AppPrefs(this).role == AppPrefs.ROLE_ADMIN

        navItems = if (isAdmin) {
            listOf(NavItem.LiveMap, NavItem.Alerts, NavItem.Family, NavItem.Settings)
        } else {
            listOf(NavItem.Home, NavItem.Sos, NavItem.Settings)
        }

        val restoredRoute = savedInstanceState?.getString(KEY_ROUTE)
        val startItem = navItems.firstOrNull { it.route == restoredRoute } ?: navItems.first()
        currentRoute = startItem.route
        showTab(startItem)

        binding.composeBottomNav.setContent {
            GlassmorphicBottomBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    if (item.route != currentRoute) {
                        currentRoute = item.route
                        showTab(item)
                    }
                }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ROUTE, currentRoute)
    }

    private fun showTab(item: NavItem) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction().setReorderingAllowed(true)

        manager.fragments
            .filter { it.id == R.id.fragmentContainer && it.tag != item.route }
            .forEach { transaction.hide(it) }

        val existing = manager.findFragmentByTag(item.route)
        if (existing == null) {
            transaction.add(R.id.fragmentContainer, createFragment(item), item.route)
        } else {
            transaction.show(existing)
        }

        transaction.commit()
    }

    private fun createFragment(item: NavItem): Fragment = when (item) {
        is NavItem.LiveMap -> PlaceholderScreen.newInstance(
            "Family map",
            "Everyone's live location will show here."
        )
        is NavItem.Alerts -> PlaceholderScreen.newInstance(
            "Alerts",
            "SOS, arrivals and departures will show here."
        )
        is NavItem.Family -> PlaceholderScreen.newInstance(
            "Family",
            "Members, battery and last seen will show here."
        )
        is NavItem.Home -> MemberHomeScreen()
        is NavItem.Sos -> PlaceholderScreen.newInstance(
            "SOS",
            "The SOS button arrives with the location service."
        )
        is NavItem.Settings -> SettingsScreen()
    }

    companion object {
        private const val KEY_ROUTE = "current_route"
    }
}