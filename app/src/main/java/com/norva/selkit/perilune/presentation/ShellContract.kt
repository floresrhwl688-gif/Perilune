package com.norva.selkit.perilune.presentation

import com.norva.selkit.perilune.data.Award
import com.norva.selkit.perilune.data.RunSummary

sealed class Route {
    data object Splash : Route()
    data object Tutorial : Route()
    data object Menu : Route()
    data object Sites : Route()
    data class Flight(val site: Int) : Route()
    data class Debrief(val summary: RunSummary, val awards: List<Award>) : Route()
    data object Awards : Route()
    data object Refit : Route()
    data object Records : Route()
    data object Setup : Route()
}

interface ShellContract {

    interface Presenter {
        val route: Route
        fun go(route: Route)
        fun swap(route: Route)
        fun back(): Boolean
        fun home()
        fun openingRoute(): Route
    }
}
