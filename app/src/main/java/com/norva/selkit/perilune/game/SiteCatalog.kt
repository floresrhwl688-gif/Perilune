package com.norva.selkit.perilune.game

enum class Objective(val tag: String, val brief: String, val bonus: String) {
    Touchdown("LANDING", "Set the module down on the deck.", "Land with fuel to spare."),
    Collect("SALVAGE", "Take every fuel pod, then land.", "Land with fuel to spare."),
    Precision("PRECISION", "Put the pads inside the inner ring.", "Land within a third of the deck radius."),
    Blackout("BLACKOUT", "No approach scope. Fly the instruments.", "Land with fuel to spare."),
    Window("WINDOW", "Be down before the comms window closes.", "Land with fuel to spare."),
    Ferry("FERRY", "Land, lift off, then land on the second deck.", "Land with fuel to spare.")
}

data class Site(
    val index: Int,
    val chapter: Int,
    val name: String,
    val brief: String,
    val objective: Objective,
    val altitude: Float,
    val fuel: Float,
    val padRadius: Float,
    val padX: Float,
    val padZ: Float,
    val pad2X: Float,
    val pad2Z: Float,
    val driftX: Float,
    val driftZ: Float,
    val gust: Float,
    val shear: Float,
    val spires: Int,
    val boulders: Int,
    val masts: Int,
    val pods: Int,
    val windowSeconds: Float,
    val softTouch: Float,
    val parFuel: Float
) {
    val ferry: Boolean get() = objective == Objective.Ferry
    val blackout: Boolean get() = objective == Objective.Blackout
    val timed: Boolean get() = objective == Objective.Window
    val quota: Int get() = if (objective == Objective.Collect) pods else 0
}

object SiteCatalog {

    const val SITE_COUNT = 40
    const val STAR_COUNT = SITE_COUNT * 3

    val chapters = listOf("Mare Serenity", "The Highlands", "Far Side", "Polar Shadow")

    val chapterBrief = listOf(
        "Flat basalt plain, no weather, wide decks.",
        "Broken ridges and rock spires under a hard sun.",
        "No Earth overhead, thin decks and constant drift.",
        "Crater rims in permanent night, the coldest air on the moon."
    )

    val sites: List<Site> = listOf(
        row(0, "Dust Flat", "A wide deck on open regolith.", 70f, 100f, 7.0f, 6f, 14f, boulders = 2, softTouch = 2.9f, parFuel = 30f),
        row(1, "First Beacon", "Follow the beacon line down.", 80f, 100f, 6.7f, -12f, 18f, boulders = 4, softTouch = 2.8f, parFuel = 30f),
        row(2, "Quiet Basin", "Survey pods are scattered over the basin floor.", 88f, 100f, 6.4f, 16f, -12f, boulders = 5, pods = 2, objective = Objective.Collect, softTouch = 2.7f, parFuel = 30f),
        row(3, "Rille Crossing", "The deck sits across a collapsed lava channel.", 94f, 98f, 6.1f, -20f, -16f, spires = 1, boulders = 6, softTouch = 2.7f, parFuel = 31f),
        row(4, "Shepherd's Rock", "Control wants the marking, not the apron.", 100f, 98f, 5.9f, 22f, 20f, spires = 1, boulders = 6, pods = 1, objective = Objective.Precision, softTouch = 2.6f, parFuel = 31f),
        row(5, "Long Shadow", "Late light, the deck hides in a shadow.", 106f, 96f, 5.7f, -24f, 12f, spires = 2, boulders = 7, masts = 1, driftX = 0.06f, softTouch = 2.6f, parFuel = 32f),
        row(6, "Ash Terrace", "A relay pass overhead closes in half a minute.", 112f, 96f, 5.5f, 18f, -22f, spires = 2, boulders = 7, pods = 1, driftZ = 0.08f, objective = Objective.Window, windowSeconds = 34f, softTouch = 2.5f, parFuel = 32f),
        row(7, "Twin Pads", "Two decks, one tank. Down, up and down again.", 104f, 100f, 5.6f, -22f, 16f, pad2X = 24f, pad2Z = -18f, spires = 1, boulders = 6, objective = Objective.Ferry, softTouch = 2.5f, parFuel = 26f),
        row(8, "Cargo Run", "Three pods went out with the last drop.", 116f, 96f, 5.3f, 20f, 24f, spires = 3, boulders = 8, masts = 1, pods = 3, gust = 0.08f, objective = Objective.Collect, softTouch = 2.4f, parFuel = 33f),
        row(9, "Serenity Gate", "Two spires frame the only way in.", 124f, 94f, 5.0f, -8f, 26f, spires = 4, boulders = 8, masts = 1, pods = 1, driftX = 0.1f, gust = 0.1f, softTouch = 2.3f, parFuel = 34f),
        row(10, "Broken Rim", "The rim wall is a field of loose rock.", 118f, 94f, 4.9f, 24f, 18f, spires = 4, boulders = 9, driftX = 0.14f, gust = 0.12f, shear = 0.1f, softTouch = 2.3f, parFuel = 33f),
        row(11, "Chalk Ridge", "Pale ridges throw the eye off the marking.", 126f, 92f, 4.8f, -26f, -20f, spires = 5, boulders = 9, masts = 1, pods = 1, driftZ = 0.16f, shear = 0.12f, objective = Objective.Precision, softTouch = 2.2f, parFuel = 33f),
        row(12, "Spire Alley", "A run of pinnacles either side of the deck.", 132f, 92f, 4.6f, 14f, 28f, spires = 7, boulders = 8, pods = 1, driftX = 0.16f, gust = 0.14f, shear = 0.14f, softTouch = 2.2f, parFuel = 34f),
        row(13, "Sunstruck Ledge", "Glare and a thirty second window.", 138f, 90f, 4.6f, -30f, 16f, spires = 6, boulders = 10, masts = 2, driftZ = 0.16f, gust = 0.16f, shear = 0.16f, objective = Objective.Window, windowSeconds = 30f, softTouch = 2.1f, parFuel = 34f),
        row(14, "Crater Steps", "Three terraces down to the deck.", 142f, 90f, 4.4f, 28f, -26f, spires = 6, boulders = 11, masts = 2, pods = 1, driftX = 0.18f, gust = 0.16f, shear = 0.2f, softTouch = 2.1f, parFuel = 35f),
        row(15, "Narrow Shelf", "The deck overhangs a drop, and control is watching.", 148f, 88f, 4.3f, -18f, -30f, spires = 7, boulders = 10, masts = 2, driftZ = 0.2f, gust = 0.18f, shear = 0.22f, objective = Objective.Precision, softTouch = 2.0f, parFuel = 35f),
        row(16, "Ridgeline Post", "Resupply for the post on the crest.", 152f, 88f, 4.2f, 30f, 24f, spires = 8, boulders = 11, masts = 3, pods = 2, driftX = 0.16f, gust = 0.2f, shear = 0.24f, objective = Objective.Collect, softTouch = 2.0f, parFuel = 35f),
        row(17, "Blind Approach", "The scope is out. Altitude, speed and nerve.", 156f, 88f, 4.4f, -24f, 28f, spires = 5, boulders = 9, masts = 1, driftZ = 0.14f, shear = 0.2f, objective = Objective.Blackout, softTouch = 2.0f, parFuel = 34f),
        row(18, "Supply Hop", "Drop at the near deck, then move to the far one.", 150f, 92f, 4.6f, 26f, -22f, pad2X = -28f, pad2Z = 26f, spires = 6, boulders = 10, masts = 2, driftX = 0.18f, gust = 0.16f, shear = 0.2f, objective = Objective.Ferry, softTouch = 2.0f, parFuel = 26f),
        row(19, "Highland Crown", "The high point of the range.", 164f, 86f, 4.0f, -32f, -18f, spires = 8, boulders = 12, masts = 3, pods = 2, driftX = 0.22f, driftZ = 0.18f, gust = 0.22f, shear = 0.3f, softTouch = 1.9f, parFuel = 36f),
        row(20, "Nightside Drift", "Cold air off the terminator pushes you along.", 150f, 88f, 4.0f, 26f, 30f, spires = 7, boulders = 10, masts = 2, pods = 2, driftX = 0.28f, driftZ = 0.14f, gust = 0.2f, shear = 0.3f, softTouch = 1.9f, parFuel = 35f),
        row(21, "Cold Relay", "The relay crew left their pods behind.", 158f, 86f, 3.9f, -28f, 26f, spires = 8, boulders = 11, masts = 3, pods = 2, driftX = 0.2f, driftZ = 0.28f, gust = 0.22f, shear = 0.32f, objective = Objective.Collect, softTouch = 1.9f, parFuel = 35f),
        row(22, "Ghost Rille", "A channel that swallows the beacon signal.", 164f, 86f, 4.0f, 32f, -28f, spires = 8, boulders = 12, masts = 2, driftX = 0.26f, gust = 0.22f, shear = 0.34f, objective = Objective.Blackout, softTouch = 1.8f, parFuel = 35f),
        row(23, "Static Field", "Charged dust drags at the hull.", 170f, 84f, 3.7f, -34f, -24f, spires = 9, boulders = 12, masts = 3, pods = 2, driftX = 0.26f, driftZ = 0.3f, gust = 0.26f, shear = 0.36f, softTouch = 1.8f, parFuel = 36f),
        row(24, "Deep Dark", "No horizon, no scope, only the deck lights.", 176f, 84f, 3.9f, 30f, 32f, spires = 8, boulders = 12, masts = 3, driftZ = 0.28f, gust = 0.24f, shear = 0.38f, objective = Objective.Blackout, softTouch = 1.7f, parFuel = 36f),
        row(25, "Iron Basin", "Heavy ore below throws the readouts off.", 182f, 82f, 3.6f, -36f, 30f, spires = 10, boulders = 13, masts = 4, pods = 2, driftX = 0.28f, driftZ = 0.34f, gust = 0.3f, shear = 0.4f, objective = Objective.Precision, softTouch = 1.7f, parFuel = 37f),
        row(26, "The Slot", "A gap in the rock barely wider than the deck.", 188f, 80f, 3.5f, 34f, -32f, spires = 11, boulders = 12, masts = 4, pods = 3, driftX = 0.36f, driftZ = 0.28f, gust = 0.32f, shear = 0.42f, softTouch = 1.7f, parFuel = 37f),
        row(27, "Signal Loss", "The window is twenty six seconds and closing.", 184f, 82f, 3.7f, -30f, -34f, spires = 9, boulders = 12, masts = 3, driftX = 0.3f, gust = 0.3f, shear = 0.4f, objective = Objective.Window, windowSeconds = 26f, softTouch = 1.7f, parFuel = 36f),
        row(28, "Long Ferry", "Two decks on opposite rims of the basin.", 178f, 88f, 3.9f, 32f, 28f, pad2X = -34f, pad2Z = -30f, spires = 9, boulders = 11, masts = 3, driftX = 0.3f, driftZ = 0.26f, gust = 0.28f, shear = 0.38f, objective = Objective.Ferry, softTouch = 1.7f, parFuel = 24f),
        row(29, "Far Side Crown", "The last deck before the pole.", 196f, 78f, 3.3f, -38f, -34f, spires = 11, boulders = 14, masts = 4, pods = 3, driftX = 0.38f, driftZ = 0.36f, gust = 0.34f, shear = 0.46f, softTouch = 1.6f, parFuel = 38f),
        row(30, "Frost Shelf", "Ice under the dust, and nothing to see by.", 175f, 84f, 3.6f, 30f, -30f, spires = 9, boulders = 12, masts = 3, pods = 2, driftX = 0.35f, driftZ = 0.2f, gust = 0.3f, shear = 0.45f, softTouch = 1.7f, parFuel = 36f),
        row(31, "Rim of Light", "A sliver of sun on the rim, the deck in shadow.", 182f, 82f, 3.5f, -32f, 28f, spires = 10, boulders = 12, masts = 4, pods = 2, driftZ = 0.36f, gust = 0.32f, shear = 0.48f, objective = Objective.Precision, softTouch = 1.6f, parFuel = 37f),
        row(32, "Permanent Night", "Sunlight has never touched this floor.", 188f, 82f, 3.7f, 34f, 30f, spires = 10, boulders = 13, masts = 4, driftX = 0.36f, gust = 0.34f, shear = 0.5f, objective = Objective.Blackout, softTouch = 1.6f, parFuel = 36f),
        row(33, "Ice Sump", "Pods left by the drill crew, and they are cold.", 194f, 80f, 3.4f, -34f, -30f, spires = 11, boulders = 13, masts = 4, pods = 3, driftX = 0.32f, driftZ = 0.38f, gust = 0.34f, shear = 0.52f, objective = Objective.Collect, softTouch = 1.6f, parFuel = 37f),
        row(34, "Crater Wall", "The wall throws the wind straight down on you.", 198f, 78f, 3.3f, 36f, -34f, spires = 12, boulders = 14, masts = 4, pods = 3, driftX = 0.4f, driftZ = 0.34f, gust = 0.36f, shear = 0.55f, softTouch = 1.5f, parFuel = 38f),
        row(35, "Shackle Ridge", "Twenty four seconds of link, then nothing.", 196f, 80f, 3.5f, -36f, 32f, spires = 11, boulders = 13, masts = 5, driftX = 0.38f, gust = 0.36f, shear = 0.54f, objective = Objective.Window, windowSeconds = 24f, softTouch = 1.5f, parFuel = 37f),
        row(36, "The Cold Trap", "The narrowest marking on the survey.", 202f, 78f, 3.2f, 34f, 34f, spires = 12, boulders = 14, masts = 5, pods = 3, driftX = 0.42f, driftZ = 0.38f, gust = 0.38f, shear = 0.58f, objective = Objective.Precision, softTouch = 1.5f, parFuel = 39f),
        row(37, "Blackout Run", "No scope, full drift, deep shadow.", 206f, 78f, 3.4f, -38f, -36f, spires = 12, boulders = 14, masts = 5, driftX = 0.44f, driftZ = 0.36f, gust = 0.38f, shear = 0.6f, objective = Objective.Blackout, softTouch = 1.5f, parFuel = 38f),
        row(38, "Polar Ferry", "Rim to floor, and the tank has to cover both.", 200f, 86f, 3.6f, 36f, -32f, pad2X = -36f, pad2Z = 34f, spires = 11, boulders = 13, masts = 4, driftX = 0.4f, driftZ = 0.36f, gust = 0.36f, shear = 0.56f, objective = Objective.Ferry, softTouch = 1.5f, parFuel = 22f),
        row(39, "Perilune", "The lowest point of the orbit, and the last deck.", 215f, 74f, 3.0f, -40f, 36f, spires = 13, boulders = 14, masts = 5, pods = 4, driftX = 0.5f, driftZ = 0.44f, gust = 0.4f, shear = 0.65f, softTouch = 1.4f, parFuel = 40f)
    )

    private fun row(
        index: Int,
        name: String,
        brief: String,
        altitude: Float,
        fuel: Float,
        padRadius: Float,
        padX: Float,
        padZ: Float,
        pad2X: Float = 0f,
        pad2Z: Float = 0f,
        spires: Int = 0,
        boulders: Int = 4,
        masts: Int = 0,
        pods: Int = 0,
        driftX: Float = 0f,
        driftZ: Float = 0f,
        gust: Float = 0f,
        shear: Float = 0f,
        objective: Objective = Objective.Touchdown,
        windowSeconds: Float = 0f,
        softTouch: Float,
        parFuel: Float
    ) = Site(
        index = index, chapter = index / 10, name = name, brief = brief, objective = objective,
        altitude = altitude, fuel = fuel, padRadius = padRadius, padX = padX, padZ = padZ,
        pad2X = pad2X, pad2Z = pad2Z, driftX = driftX, driftZ = driftZ, gust = gust, shear = shear,
        spires = spires, boulders = boulders, masts = masts, pods = pods,
        windowSeconds = windowSeconds, softTouch = softTouch, parFuel = parFuel
    )

    fun site(index: Int): Site = sites[index.coerceIn(0, sites.lastIndex)]

    fun difficulty(site: Site): String = when {
        site.index < 5 -> "Routine"
        site.index < 10 -> "Steady"
        site.index < 15 -> "Testing"
        site.index < 20 -> "Hard"
        site.index < 26 -> "Severe"
        site.index < 33 -> "Extreme"
        else -> "Critical"
    }
}
