package com.norva.selkit.perilune.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.norva.selkit.perilune.R
import com.norva.selkit.perilune.data.PerilunePrefs

enum class Sfx { Tap, Touch, Crash, Pickup, Warn, Win }

class SoundBox(context: Context, private val prefs: PerilunePrefs) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mapOf(
        Sfx.Tap to pool.load(context, R.raw.tap, 1),
        Sfx.Touch to pool.load(context, R.raw.touch, 1),
        Sfx.Crash to pool.load(context, R.raw.crash, 1),
        Sfx.Pickup to pool.load(context, R.raw.pickup, 1),
        Sfx.Warn to pool.load(context, R.raw.warn, 1),
        Sfx.Win to pool.load(context, R.raw.win, 1)
    )

    private val engineId = pool.load(context, R.raw.thrust, 1)
    private var engineStream = 0

    fun play(sfx: Sfx, volume: Float = 1f) {
        if (!prefs.sound) return
        ids[sfx]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun engine(on: Boolean) {
        if (on) {
            if (!prefs.sound || engineStream != 0) return
            engineStream = pool.play(engineId, 0.85f, 0.85f, 1, -1, 1f)
        } else {
            if (engineStream != 0) {
                pool.stop(engineStream)
                engineStream = 0
            }
        }
    }

    fun release() {
        engine(false)
        pool.release()
    }
}
