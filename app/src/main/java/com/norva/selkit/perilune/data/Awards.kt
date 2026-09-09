package com.norva.selkit.perilune.data

import com.norva.selkit.perilune.game.Objective
import com.norva.selkit.perilune.game.SiteCatalog

data class Award(val id: String, val title: String, val detail: String, val icon: String)

object AwardBook {

    val all = listOf(
        Award("first", "First Contact", "Set the pads down on any deck.", "aw_first"),
        Award("feather", "Feather Touch", "Touch down at under one metre per second.", "aw_feather"),
        Award("centre", "Dead Centre", "Land within a metre of the deck marking.", "aw_centre"),
        Award("dry", "Dry Tank", "Land with less than five percent of the fuel left.", "aw_dry"),
        Award("perfect", "Full Marks", "Take three stars on any site.", "aw_perfect"),
        Award("swift", "Quick Descent", "Land a site in under twenty seconds.", "aw_swift"),
        Award("fuel", "Tanker", "Collect forty fuel pods.", "aw_fuel"),
        Award("scavenger", "Scavenger", "Clear a salvage site with every pod aboard.", "aw_scavenger"),
        Award("precise", "Sharpshooter", "Take three stars on a precision site.", "aw_precise"),
        Award("blind", "Blind Landing", "Clear a blackout site without the scope.", "aw_blind"),
        Award("window", "On Schedule", "Clear a window site with five seconds to spare.", "aw_window"),
        Award("ferry", "Ferryman", "Complete a ferry run across two decks.", "aw_ferry"),
        Award("refit", "Back in the Shop", "Fit any upgrade to the module.", "aw_refit"),
        Award("survey", "Survey Complete", "Land on twenty different sites.", "aw_survey"),
        Award("mare", "Mare Cleared", "Clear every site in Mare Serenity.", "aw_mare"),
        Award("highland", "Highlands Cleared", "Clear every site in The Highlands.", "aw_highland"),
        Award("farside", "Far Side Cleared", "Clear every site on the Far Side.", "aw_farside"),
        Award("polar", "Polar Cleared", "Clear every site in the Polar Shadow.", "aw_polar"),
        Award("master", "Perilune", "Take three stars on every site in the survey.", "aw_master")
    )

    fun review(prefs: PerilunePrefs, run: RunSummary?): List<Award> {
        val won = ArrayList<Award>()
        val claim = { id: String ->
            if (prefs.unlockAward(id)) all.firstOrNull { it.id == id }?.let { won.add(it) } else Unit
        }
        if (prefs.statLandings() > 0) claim("first")
        if (run != null && run.landed) {
            if (run.touchdown < 1.0f) claim("feather")
            if (run.offset < 1.0f) claim("centre")
            if (run.fuelPercent < 5f) claim("dry")
            if (run.stars >= 3) claim("perfect")
            if (run.time < 20f) claim("swift")
            when (run.objective) {
                Objective.Collect -> claim("scavenger")
                Objective.Precision -> if (run.stars >= 3) claim("precise")
                Objective.Blackout -> claim("blind")
                Objective.Window -> if (run.windowLeft >= 5f) claim("window")
                Objective.Ferry -> claim("ferry")
                Objective.Touchdown -> Unit
            }
        }
        if (prefs.refit().levels > 0) claim("refit")
        if (prefs.statPods() >= 40) claim("fuel")
        if (prefs.sitesCleared() >= 20) claim("survey")
        if (prefs.chapterCleared(0)) claim("mare")
        if (prefs.chapterCleared(1)) claim("highland")
        if (prefs.chapterCleared(2)) claim("farside")
        if (prefs.chapterCleared(3)) claim("polar")
        if (prefs.totalStars() >= SiteCatalog.STAR_COUNT) claim("master")
        return won
    }
}

data class RunSummary(
    val site: Int,
    val objective: Objective,
    val landed: Boolean,
    val reason: String,
    val stars: Int,
    val time: Float,
    val touchdown: Float,
    val lateral: Float,
    val offset: Float,
    val fuelLeft: Float,
    val fuelPercent: Float,
    val pods: Int,
    val windowLeft: Float
)
