package com.hustlers.tobedecided.data

import android.app.Activity

sealed class Route {
    data class Go(val target: Class<out Activity>) : Route()
    data object NoProfile : Route()
    data object NeedsInternet : Route()
    data class Error(val message: String) : Route()
}