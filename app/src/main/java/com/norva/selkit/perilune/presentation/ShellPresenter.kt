package com.norva.selkit.perilune.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.norva.selkit.perilune.data.PerilunePrefs

class ShellPresenter(private val prefs: PerilunePrefs) : ShellContract.Presenter {

    private val stack = ArrayList<Route>().apply { add(Route.Splash) }

    override var route: Route by mutableStateOf(Route.Splash)
        private set

    override fun go(route: Route) {
        stack.add(route)
        this.route = route
    }

    override fun swap(route: Route) {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
        stack.add(route)
        this.route = route
    }

    override fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        route = stack[stack.lastIndex]
        return true
    }

    override fun home() {
        stack.clear()
        stack.add(Route.Menu)
        route = Route.Menu
    }

    override fun openingRoute(): Route = if (prefs.tutorialSeen) Route.Menu else Route.Tutorial
}
