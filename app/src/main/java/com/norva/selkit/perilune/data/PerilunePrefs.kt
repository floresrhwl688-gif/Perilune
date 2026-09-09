package com.norva.selkit.perilune.data

import android.content.Context
import android.content.SharedPreferences
import com.norva.selkit.perilune.game.SiteCatalog

class PerilunePrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("com.norva.selkit.perilune_game_v1", Context.MODE_PRIVATE)

    var sound: Boolean
        get() = prefs.getBoolean("sound", true)
        set(value) = prefs.edit().putBoolean("sound", value).apply()

    var vibration: Boolean
        get() = prefs.getBoolean("vibration", true)
        set(value) = prefs.edit().putBoolean("vibration", value).apply()

    var backdrop: Int
        get() = prefs.getInt("backdrop", 0)
        set(value) = prefs.edit().putInt("backdrop", value).apply()

    var tutorialSeen: Boolean
        get() = prefs.getBoolean("tutorial_seen", false)
        set(value) = prefs.edit().putBoolean("tutorial_seen", value).apply()

    fun stars(site: Int): Int = prefs.getInt("site_${site}_stars", 0)

    fun bestTime(site: Int): Float = prefs.getFloat("site_${site}_time", 0f)

    fun bestTouchdown(site: Int): Float = prefs.getFloat("site_${site}_touch", 0f)

    fun unlocked(site: Int): Boolean = site == 0 || prefs.getBoolean("site_${site}_open", false)

    fun openSite(site: Int) {
        prefs.edit().putBoolean("site_${site}_open", true).apply()
    }

    fun totalStars(): Int {
        var sum = 0
        for (i in 0 until SiteCatalog.SITE_COUNT) sum += stars(i)
        return sum
    }

    fun sitesCleared(): Int {
        var sum = 0
        for (i in 0 until SiteCatalog.SITE_COUNT) if (stars(i) > 0) sum++
        return sum
    }

    fun chapterStars(chapter: Int): Int {
        var sum = 0
        for (i in chapter * 10 until chapter * 10 + 10) sum += stars(i)
        return sum
    }

    fun chapterCleared(chapter: Int): Boolean {
        for (i in chapter * 10 until chapter * 10 + 10) if (stars(i) == 0) return false
        return true
    }

    fun refit(): Refit = Refit(
        tank = prefs.getInt("up_tank", 0),
        engine = prefs.getInt("up_engine", 0),
        rcs = prefs.getInt("up_rcs", 0),
        gear = prefs.getInt("up_gear", 0)
    )

    fun spentStars(): Int = prefs.getInt("stars_spent", 0)

    fun availableStars(): Int = (totalStars() - spentStars()).coerceAtLeast(0)

    fun buyUpgrade(upgrade: Upgrade): Boolean {
        val level = refit().level(upgrade)
        if (level >= Upgrade.MAX_LEVEL) return false
        val price = Upgrade.cost(level)
        if (availableStars() < price) return false
        prefs.edit()
            .putInt("up_${upgrade.id}", level + 1)
            .putInt("stars_spent", spentStars() + price)
            .apply()
        return true
    }

    fun refundRefit() {
        prefs.edit()
            .putInt("up_tank", 0)
            .putInt("up_engine", 0)
            .putInt("up_rcs", 0)
            .putInt("up_gear", 0)
            .putInt("stars_spent", 0)
            .apply()
    }

    fun recordRun(site: Int, stars: Int, time: Float, touchdown: Float, fuelLeft: Float, pods: Int) {
        val editor = prefs.edit()
        if (stars > stars(site)) editor.putInt("site_${site}_stars", stars)
        val prevTime = bestTime(site)
        if (prevTime <= 0f || time < prevTime) editor.putFloat("site_${site}_time", time)
        val prevTouch = bestTouchdown(site)
        if (prevTouch <= 0f || touchdown < prevTouch) editor.putFloat("site_${site}_touch", touchdown)
        editor.putInt("stat_landings", statLandings() + 1)
        editor.putInt("stat_pods", statPods() + pods)
        editor.putFloat("stat_flight_time", statFlightTime() + time)
        val softest = statSoftest()
        if (softest <= 0f || touchdown < softest) editor.putFloat("stat_softest", touchdown)
        if (fuelLeft > statFuelSpared()) editor.putFloat("stat_fuel_spared", fuelLeft)
        editor.apply()
    }

    fun recordCrash() {
        prefs.edit().putInt("stat_crashes", statCrashes() + 1).apply()
    }

    fun statLandings(): Int = prefs.getInt("stat_landings", 0)
    fun statCrashes(): Int = prefs.getInt("stat_crashes", 0)
    fun statPods(): Int = prefs.getInt("stat_pods", 0)
    fun statFlightTime(): Float = prefs.getFloat("stat_flight_time", 0f)
    fun statSoftest(): Float = prefs.getFloat("stat_softest", 0f)
    fun statFuelSpared(): Float = prefs.getFloat("stat_fuel_spared", 0f)

    fun awardUnlocked(id: String): Boolean = prefs.getBoolean("aw_$id", false)

    fun unlockAward(id: String): Boolean {
        if (awardUnlocked(id)) return false
        prefs.edit().putBoolean("aw_$id", true).apply()
        return true
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}
