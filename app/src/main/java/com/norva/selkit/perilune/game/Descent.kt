package com.norva.selkit.perilune.game

import com.norva.selkit.perilune.data.Refit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class Phase { Flying, Perched, Landed, Crashed }

enum class RockKind { Spire, Boulder, Mast }

class Rock(val x: Float, val z: Float, val radius: Float, val height: Float, val kind: RockKind, val spin: Float)

class Pod(val x: Float, val y: Float, val z: Float) {
    var taken = false
}

class Descent(val site: Site, val refit: Refit = Refit()) {

    var x = 0f
    var y = 0f
    var z = 0f
    var vx = 0f
    var vy = 0f
    var vz = 0f
    var tiltX = 0f
    var tiltZ = 0f
    var fuel = 0f
    var elapsed = 0f
    var phase = Phase.Flying
    var thrusting = false
    var podsTaken = 0
    var stage = 0
    var verdict = ""
    var touchdownSpeed = 0f
    var lateralSpeed = 0f
    var padOffset = 0f
    var stars = 0
    var shake = 0f
    var plume = 0f

    val rocks = ArrayList<Rock>()
    val pods = ArrayList<Pod>()

    val fuelCapacity = site.fuel * refit.fuelScale
    private val thrustPower = THRUST * refit.thrustScale
    private val leanLimit = MAX_TILT + refit.leanBonus
    private val leanRate = TILT_RATE * refit.tiltRateScale
    private val touchLimit = MAX_TOUCH + refit.contactBonus
    private val slideLimit = MAX_LATERAL + refit.contactBonus * 0.75f

    private var stickX = 0f
    private var stickZ = 0f
    private var throttle = false
    private var gustPhase = 0f

    init { reset() }

    fun reset() {
        x = 0f
        z = 0f
        y = site.altitude
        vx = site.driftX * 6f
        vz = site.driftZ * 6f
        vy = -3.2f - site.index * 0.04f
        tiltX = 0f
        tiltZ = 0f
        fuel = fuelCapacity
        elapsed = 0f
        phase = Phase.Flying
        thrusting = false
        podsTaken = 0
        stage = 0
        verdict = ""
        touchdownSpeed = 0f
        lateralSpeed = 0f
        padOffset = 0f
        stars = 0
        shake = 0f
        plume = 0f
        stickX = 0f
        stickZ = 0f
        throttle = false
        gustPhase = scatter(site.index, 91) * 6.28f
        buildTerrain()
    }

    fun control(sx: Float, sz: Float) {
        stickX = sx.coerceIn(-1f, 1f)
        stickZ = sz.coerceIn(-1f, 1f)
    }

    fun throttle(on: Boolean) {
        throttle = on
        if (on && phase == Phase.Perched && fuel > 0f) {
            phase = Phase.Flying
            vy = 0f
            vx = 0f
            vz = 0f
        }
    }

    fun targetPadX(): Float = if (site.ferry && stage == 1) site.pad2X else site.padX

    fun targetPadZ(): Float = if (site.ferry && stage == 1) site.pad2Z else site.padZ

    fun altitude(): Float = (y - PAD_TOP).coerceAtLeast(0f)

    fun fuelPercent(): Float = (fuel / fuelCapacity * 100f).coerceIn(0f, 100f)

    fun offsetFromPad(): Float = hypot(x - targetPadX(), z - targetPadZ())

    fun tiltDegrees(): Float = Math.toDegrees(hypot(tiltX, tiltZ).toDouble()).toFloat()

    fun windowLeft(): Float =
        if (!site.timed) 0f else (site.windowSeconds - elapsed).coerceAtLeast(0f)

    fun podsLeft(): Int = pods.count { !it.taken }

    fun quotaMet(): Boolean = site.quota == 0 || podsTaken >= site.quota

    fun step(dt: Float) {
        if (phase == Phase.Landed || phase == Phase.Crashed) {
            shake = (shake - dt * 2.4f).coerceAtLeast(0f)
            plume = (plume - dt * 4f).coerceAtLeast(0f)
            return
        }
        elapsed += dt
        if (phase == Phase.Perched) {
            plume = (plume - dt * 5f).coerceAtLeast(0f)
            thrusting = false
            if (site.timed && elapsed > site.windowSeconds) fail("The comms window closed")
            return
        }

        val targetX = stickX * leanLimit
        val targetZ = stickZ * leanLimit
        tiltX += (targetX - tiltX).coerceIn(-leanRate * dt, leanRate * dt)
        tiltZ += (targetZ - tiltZ).coerceIn(-leanRate * dt, leanRate * dt)

        thrusting = throttle && fuel > 0f
        val push = if (thrusting) thrustPower else 0f
        if (thrusting) fuel = (fuel - BURN * dt).coerceAtLeast(0f)
        plume = if (thrusting) (plume + dt * 6f).coerceAtMost(1f) else (plume - dt * 5f).coerceAtLeast(0f)

        gustPhase += dt * 0.9f
        val height = (y / site.altitude).coerceIn(0f, 1f)
        val shearScale = 1f + site.shear * height * 2f
        val gustX = site.gust * sin(gustPhase) * 0.8f * shearScale
        val gustZ = site.gust * cos(gustPhase * 0.77f) * 0.8f * shearScale

        val ax = push * sin(tiltX) + site.driftX * shearScale + gustX
        val az = push * sin(tiltZ) + site.driftZ * shearScale + gustZ
        val ay = push * cos(tiltX) * cos(tiltZ) - GRAVITY

        vx += ax * dt
        vy += ay * dt
        vz += az * dt
        x += vx * dt
        y += vy * dt
        z += vz * dt

        shake = if (thrusting) (shake + dt * 1.2f).coerceAtMost(0.35f) else (shake - dt * 1.6f).coerceAtLeast(0f)

        collectPods()
        checkRocks()
        if (phase == Phase.Flying) checkGround()
        if (phase == Phase.Flying && site.timed && elapsed > site.windowSeconds) fail("The comms window closed")
        if (phase == Phase.Flying && (abs(x) > BOUNDS || abs(z) > BOUNDS)) fail("Drifted off the survey area")
    }

    private fun collectPods() {
        for (pod in pods) {
            if (pod.taken) continue
            if (hypot(hypot(x - pod.x, z - pod.z), y - pod.y) < 4.6f) {
                pod.taken = true
                podsTaken++
                fuel = (fuel + POD_FUEL).coerceAtMost(fuelCapacity)
            }
        }
    }

    private fun checkRocks() {
        for (rock in rocks) {
            if (y > rock.height) continue
            if (hypot(x - rock.x, z - rock.z) < rock.radius + LANDER_RADIUS) {
                fail(
                    when (rock.kind) {
                        RockKind.Spire -> "Clipped a rock spire"
                        RockKind.Mast -> "Struck a relay mast"
                        RockKind.Boulder -> "Struck a boulder"
                    }
                )
                return
            }
        }
    }

    private fun checkGround() {
        val offset = offsetFromPad()
        if (offset <= site.padRadius && y <= PAD_TOP) {
            settle(offset, onPad = true)
            return
        }
        if (site.ferry && stage == 1 && y <= PAD_TOP &&
            hypot(x - site.padX, z - site.padZ) <= site.padRadius
        ) {
            if (abs(vy) > touchLimit || hypot(vx, vz) > slideLimit) {
                settle(offset, onPad = false)
            } else {
                y = PAD_TOP
                vx = 0f
                vy = 0f
                vz = 0f
                phase = Phase.Perched
                verdict = "Back on the first deck"
            }
            return
        }
        if (y <= 0f) settle(offset, onPad = false)
    }

    private fun settle(offset: Float, onPad: Boolean) {
        touchdownSpeed = abs(vy)
        lateralSpeed = hypot(vx, vz)
        padOffset = offset
        y = if (onPad) PAD_TOP else 0f
        vx = 0f
        vy = 0f
        vz = 0f
        if (!onPad) {
            wreck("Came down off the deck")
            return
        }
        if (touchdownSpeed > touchLimit) {
            wreck("Touchdown too fast")
            return
        }
        if (lateralSpeed > slideLimit) {
            wreck("Sliding sideways on contact")
            return
        }
        if (hypot(tiltX, tiltZ) > MAX_LAND_TILT + refit.leanBonus * 0.5f) {
            wreck("Tipped over on the deck")
            return
        }
        if (site.ferry && stage == 0) {
            stage = 1
            phase = Phase.Perched
            verdict = "Perched. Lift off for the second deck"
            return
        }
        if (!quotaMet()) {
            wreck("Landed with the pods still out there")
            return
        }
        phase = Phase.Landed
        verdict = "Down and steady"
        stars = 1
        if (touchdownSpeed <= site.softTouch) stars++
        val bonus = if (site.objective == Objective.Precision) {
            padOffset <= site.padRadius * 0.34f
        } else {
            fuelPercent() >= site.parFuel
        }
        if (bonus) stars++
    }

    private fun wreck(reason: String) {
        phase = Phase.Crashed
        verdict = reason
        shake = 1f
    }

    private fun fail(reason: String) {
        touchdownSpeed = abs(vy)
        lateralSpeed = hypot(vx, vz)
        padOffset = offsetFromPad()
        vx = 0f
        vy = 0f
        vz = 0f
        wreck(reason)
    }

    private fun buildTerrain() {
        rocks.clear()
        pods.clear()
        var slot = 0
        var placed = 0
        while (placed < site.spires && slot < 260) {
            val px = (scatter(site.index * 31 + slot, 5) - 0.5f) * 150f
            val pz = (scatter(site.index * 31 + slot, 17) - 0.5f) * 150f
            slot++
            if (!clearOfDecks(px, pz, 12f)) continue
            if (hypot(px, pz) < 16f) continue
            val height = 11f + scatter(slot, 29) * 8f
            rocks.add(Rock(px, pz, height * 0.34f, height, RockKind.Spire, scatter(slot, 37) * 360f))
            placed++
        }
        placed = 0
        while (placed < site.masts && slot < 460) {
            val px = (scatter(site.index * 31 + slot, 73) - 0.5f) * 160f
            val pz = (scatter(site.index * 31 + slot, 79) - 0.5f) * 160f
            slot++
            if (!clearOfDecks(px, pz, 11f)) continue
            if (hypot(px, pz) < 14f) continue
            val height = 15f + scatter(slot, 83) * 8f
            rocks.add(Rock(px, pz, height * 0.13f + 0.9f, height, RockKind.Mast, scatter(slot, 89) * 360f))
            placed++
        }
        placed = 0
        while (placed < site.boulders && slot < 700) {
            val px = (scatter(site.index * 31 + slot, 41) - 0.5f) * 170f
            val pz = (scatter(site.index * 31 + slot, 53) - 0.5f) * 170f
            slot++
            if (!clearOfDecks(px, pz, 8f)) continue
            rocks.add(Rock(px, pz, 3.0f, 5.6f, RockKind.Boulder, scatter(slot, 59) * 360f))
            placed++
        }
        for (i in 0 until site.pods) {
            val px = (scatter(site.index * 7 + i, 61) - 0.5f) * 70f
            val pz = (scatter(site.index * 7 + i, 67) - 0.5f) * 70f
            val py = site.altitude * (0.26f + 0.44f * scatter(site.index * 7 + i, 71))
            pods.add(Pod(px, py, pz))
        }
    }

    private fun clearOfDecks(px: Float, pz: Float, margin: Float): Boolean {
        if (hypot(px - site.padX, pz - site.padZ) < site.padRadius + margin) return false
        if (site.ferry && hypot(px - site.pad2X, pz - site.pad2Z) < site.padRadius + margin) return false
        return true
    }

    companion object {
        const val GRAVITY = 1.62f
        const val THRUST = 5.2f
        const val BURN = 10.4f
        const val MAX_TILT = 0.32f
        const val TILT_RATE = 1.7f
        const val LANDER_RADIUS = 2.2f
        const val PAD_TOP = 1.7f
        const val MAX_TOUCH = 3.6f
        const val MAX_LATERAL = 2.4f
        const val MAX_LAND_TILT = 0.2f
        const val POD_FUEL = 16f
        const val BOUNDS = 120f

        fun scatter(index: Int, salt: Int): Float {
            var h = index * 374761393 + salt * 668265263
            h = (h xor (h shr 13)) * 1274126177
            h = h xor (h shr 16)
            return ((h and 0xFFFF) / 65535f)
        }
    }
}
