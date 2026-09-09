package com.norva.selkit.perilune.game

import com.norva.selkit.perilune.data.Award
import com.norva.selkit.perilune.data.RunSummary

enum class Cue { Ignite, Cut, Pickup, LowFuel, Perch, Touchdown, Wreck }

interface DescentContract {

    interface View {
        fun onCue(cue: Cue)
        fun onSettled(summary: RunSummary, awards: List<Award>)
    }

    interface Presenter {
        val site: Site
        val pulse: Int
        fun world(): Descent
        fun attach(view: View)
        fun detach()
        fun stick(x: Float, z: Float)
        fun throttle(on: Boolean)
        fun tick(dt: Float)
        fun paused(): Boolean
        fun setPaused(value: Boolean)
        fun retry()
    }
}
