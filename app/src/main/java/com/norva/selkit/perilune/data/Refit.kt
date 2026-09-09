package com.norva.selkit.perilune.data

enum class Upgrade(
    val id: String,
    val title: String,
    val detail: String,
    val effect: String,
    val icon: String
) {
    Tank("tank", "Descent tank", "A larger tank for the same hull.", "Fuel capacity +12% per level", "aw_fuel"),
    Engine("engine", "Engine trim", "More thrust from the descent stage.", "Thrust +5% per level", "aw_dry"),
    Rcs("rcs", "Attitude jets", "Faster and wider tilt authority.", "Tilt rate +18%, lean +2 degrees per level", "aw_centre"),
    Gear("gear", "Landing gear", "Crushable struts that forgive a hard arrival.", "Contact limits +0.4 m/s per level", "aw_first");

    companion object {
        const val MAX_LEVEL = 3
        val costs = listOf(8, 20, 38)
        fun cost(level: Int): Int = costs.getOrElse(level) { Int.MAX_VALUE }
    }
}

data class Refit(
    val tank: Int = 0,
    val engine: Int = 0,
    val rcs: Int = 0,
    val gear: Int = 0
) {
    val fuelScale: Float get() = 1f + 0.12f * tank
    val thrustScale: Float get() = 1f + 0.05f * engine
    val tiltRateScale: Float get() = 1f + 0.18f * rcs
    val leanBonus: Float get() = 0.035f * rcs
    val contactBonus: Float get() = 0.4f * gear
    val levels: Int get() = tank + engine + rcs + gear

    fun level(upgrade: Upgrade): Int = when (upgrade) {
        Upgrade.Tank -> tank
        Upgrade.Engine -> engine
        Upgrade.Rcs -> rcs
        Upgrade.Gear -> gear
    }
}
