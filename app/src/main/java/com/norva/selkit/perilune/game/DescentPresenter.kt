package com.norva.selkit.perilune.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.norva.selkit.perilune.data.AwardBook
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.data.RunSummary

class DescentPresenter(
    override val site: Site,
    private val prefs: PerilunePrefs
) : DescentContract.Presenter {

    private val world = Descent(site, prefs.refit())
    private var view: DescentContract.View? = null
    private var paused = false
    private var settled = false
    private var wasThrusting = false
    private var warned = false
    private var perched = false
    private var podsSeen = 0

    override var pulse by mutableIntStateOf(0)
        private set

    override fun world(): Descent = world

    override fun attach(view: DescentContract.View) {
        this.view = view
    }

    override fun detach() {
        view = null
    }

    override fun stick(x: Float, z: Float) {
        if (!paused) world.control(x, z)
    }

    override fun throttle(on: Boolean) {
        if (paused) {
            world.throttle(false)
            return
        }
        world.throttle(on)
    }

    override fun tick(dt: Float) {
        if (paused || settled) return
        world.step(dt)
        pulse++
        if (world.thrusting != wasThrusting) {
            wasThrusting = world.thrusting
            view?.onCue(if (world.thrusting) Cue.Ignite else Cue.Cut)
        }
        if (!warned && world.fuelPercent() < 18f && world.phase == Phase.Flying) {
            warned = true
            view?.onCue(Cue.LowFuel)
        }
        if (world.podsTaken > podsSeen) {
            podsSeen = world.podsTaken
            view?.onCue(Cue.Pickup)
        }
        if (!perched && world.phase == Phase.Perched) {
            perched = true
            view?.onCue(Cue.Perch)
        }
        if (world.phase == Phase.Landed || world.phase == Phase.Crashed) settle()
    }

    override fun paused(): Boolean = paused

    override fun setPaused(value: Boolean) {
        paused = value
        if (value) world.throttle(false)
    }

    override fun retry() {
        world.reset()
        settled = false
        warned = false
        perched = false
        wasThrusting = false
        podsSeen = 0
        paused = false
        pulse++
    }

    private fun settle() {
        if (settled) return
        settled = true
        world.throttle(false)
        val landed = world.phase == Phase.Landed
        val summary = RunSummary(
            site = site.index,
            objective = site.objective,
            landed = landed,
            reason = world.verdict,
            stars = world.stars,
            time = world.elapsed,
            touchdown = world.touchdownSpeed,
            lateral = world.lateralSpeed,
            offset = world.padOffset,
            fuelLeft = world.fuel,
            fuelPercent = world.fuelPercent(),
            pods = world.podsTaken,
            windowLeft = world.windowLeft()
        )
        if (landed) {
            prefs.recordRun(site.index, summary.stars, summary.time, summary.touchdown, summary.fuelPercent, summary.pods)
            if (site.index + 1 < SiteCatalog.SITE_COUNT) prefs.openSite(site.index + 1)
        } else {
            prefs.recordCrash()
        }
        val awards = AwardBook.review(prefs, summary)
        view?.onCue(if (landed) Cue.Touchdown else Cue.Wreck)
        view?.onSettled(summary, awards)
    }
}
