package com.norva.selkit.perilune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.audio.Haptics
import com.norva.selkit.perilune.audio.Sfx
import com.norva.selkit.perilune.audio.SoundBox
import com.norva.selkit.perilune.data.Award
import com.norva.selkit.perilune.data.RunSummary
import com.norva.selkit.perilune.game.Cue
import com.norva.selkit.perilune.game.Descent
import com.norva.selkit.perilune.game.DescentContract
import com.norva.selkit.perilune.game.Phase
import com.norva.selkit.perilune.game.RockKind
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.scene.DescentScene
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

@Composable
fun FlightScreen(
    presenter: DescentContract.Presenter,
    sound: SoundBox,
    haptics: Haptics,
    onFinish: (RunSummary, List<Award>) -> Unit,
    onQuit: () -> Unit
) {
    val world = presenter.world()
    var settled by remember { mutableStateOf<Pair<RunSummary, List<Award>>?>(null) }
    var paused by remember { mutableStateOf(false) }

    DisposableEffect(presenter) {
        val view = object : DescentContract.View {
            override fun onCue(cue: Cue) {
                when (cue) {
                    Cue.Ignite -> {
                        sound.engine(true)
                        haptics.tick()
                    }
                    Cue.Cut -> sound.engine(false)
                    Cue.Pickup -> {
                        sound.play(Sfx.Pickup)
                        haptics.soft()
                    }
                    Cue.LowFuel -> {
                        sound.play(Sfx.Warn)
                        haptics.soft()
                    }
                    Cue.Perch -> {
                        sound.engine(false)
                        sound.play(Sfx.Touch, 0.8f)
                        haptics.success()
                    }
                    Cue.Touchdown -> {
                        sound.engine(false)
                        sound.play(Sfx.Touch)
                        sound.play(Sfx.Win, 0.7f)
                        haptics.success()
                    }
                    Cue.Wreck -> {
                        sound.engine(false)
                        sound.play(Sfx.Crash)
                        haptics.impact()
                    }
                }
            }

            override fun onSettled(summary: RunSummary, awards: List<Award>) {
                settled = summary to awards
            }
        }
        presenter.attach(view)
        onDispose {
            presenter.detach()
            sound.engine(false)
        }
    }

    LaunchedEffect(settled) {
        val done = settled ?: return@LaunchedEffect
        delay(1500)
        onFinish(done.first, done.second)
    }

    BackHandler {
        paused = true
        presenter.setPaused(true)
        sound.engine(false)
    }

    Box(Modifier.fillMaxSize().background(skyBrush(world.site.chapter))) {
        StarField(world.site.index)
        DescentScene(world, Modifier.fillMaxSize()) { dt ->
            if (!paused) presenter.tick(dt)
        }
        FlightHud(presenter, world)
        if (settled == null && !paused) {
            Controls(presenter, world)
            if (world.phase == Phase.Perched) PerchBanner()
        }
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OrnateButton(
                if (paused) "Resume" else "Pause",
                {
                    paused = !paused
                    presenter.setPaused(paused)
                    if (paused) sound.engine(false)
                },
                Modifier.height(40.dp),
                Palette.Blue,
                fontSize = 12
            )
        }
        if (paused) {
            PauseCurtain(
                onResume = {
                    paused = false
                    presenter.setPaused(false)
                },
                onRestart = {
                    paused = false
                    settled = null
                    presenter.retry()
                },
                onQuit = onQuit
            )
        }
        settled?.let { done ->
            OutcomeFlash(done.first)
        }
    }
}

private fun skyBrush(chapter: Int): Brush = when (chapter) {
    0 -> Brush.verticalGradient(listOf(Color(0xFF04060E), Color(0xFF0B1428), Color(0xFF1B2740)))
    1 -> Brush.verticalGradient(listOf(Color(0xFF04070F), Color(0xFF101A2C), Color(0xFF2C3A4E)))
    else -> Brush.verticalGradient(listOf(Color(0xFF03040B), Color(0xFF0B0C1E), Color(0xFF161A33)))
}

@Composable
private fun StarField(seed: Int) {
    Canvas(Modifier.fillMaxSize()) {
        for (i in 0 until 90) {
            val x = Descent.scatter(seed * 13 + i, 7) * size.width
            val y = Descent.scatter(seed * 13 + i, 23) * size.height * 0.7f
            val r = 0.7f + Descent.scatter(i, 43) * 1.6f
            drawCircle(Palette.Cream.copy(alpha = 0.16f + Descent.scatter(i, 53) * 0.5f), r, Offset(x, y))
        }
    }
}

@Composable
private fun FlightHud(presenter: DescentContract.Presenter, world: Descent) {
    val tick = presenter.pulse
    Column(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp)
            .padding(top = 52.dp)
    ) {
        Text(
            world.site.name.uppercase(),
            color = Palette.Cream,
            fontFamily = GameFonts.primary,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
            Text(
                world.site.objective.tag,
                color = Palette.Ink,
                fontFamily = GameFonts.primary,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Palette.Amber)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
            Text(
                "  " + world.site.objective.brief,
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 12.sp
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Readout("ALT", format(world.altitude(), 0) + " m", Palette.Cream, tick)
                Readout(
                    "V SPD",
                    format(world.vy, 1) + " m/s",
                    if (world.vy < -Descent.MAX_TOUCH) Palette.Danger else Palette.Cyan,
                    tick
                )
                Readout(
                    "H SPD",
                    format(hypot(world.vx, world.vz), 1) + " m/s",
                    if (hypot(world.vx, world.vz) > Descent.MAX_LATERAL) Palette.Danger else Palette.Cyan,
                    tick
                )
                Readout(
                    "TILT",
                    format(world.tiltDegrees(), 0) + "°",
                    if (world.tiltDegrees() > 12f) Palette.Amber else Palette.Cyan,
                    tick
                )
                Readout("RANGE", format(world.offsetFromPad(), 0) + " m", Palette.Cream, tick)
                if (world.site.timed) {
                    Readout(
                        "WINDOW",
                        format(world.windowLeft(), 1) + " s",
                        if (world.windowLeft() < 6f) Palette.Danger else Palette.Amber,
                        tick
                    )
                }
                if (world.site.quota > 0) {
                    Readout(
                        "PODS",
                        "${world.podsTaken} / ${world.site.quota}",
                        if (world.quotaMet()) Palette.Cyan else Palette.Amber,
                        tick
                    )
                }
                if (world.site.ferry) {
                    Readout("DECK", if (world.stage == 0) "first" else "second", Palette.Cyan, tick)
                }
                FuelBar(world.fuelPercent(), tick)
            }
            if (world.site.blackout) BlindPanel(world, tick) else Scope(world, tick)
        }
    }
}

@Composable
private fun Readout(label: String, value: String, tone: Color, tick: Int) {
    Row(
        Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 12.sp,
            modifier = Modifier.width(52.dp)
        )
        Text(
            value,
            color = tone,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun FuelBar(percent: Float, tick: Int) {
    Column(Modifier.padding(top = 6.dp).width(150.dp)) {
        Text("FUEL", color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 12.sp)
        Canvas(Modifier.fillMaxWidth().height(10.dp).padding(top = 2.dp)) {
            val slot = tick
            drawRoundRect(
                color = Palette.Panel,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
            )
            val tone = when {
                percent < 12f -> Palette.Danger
                percent < 30f -> Palette.Amber
                else -> Palette.Cyan
            }
            drawRoundRect(
                color = tone,
                size = androidx.compose.ui.geometry.Size(size.width * (percent / 100f).coerceIn(0f, 1f), size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
            )
        }
    }
}

@Composable
private fun Scope(world: Descent, tick: Int) {
    Box(
        Modifier
            .size(126.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Ink.copy(alpha = 0.62f))
    ) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val slot = tick
            val span = 46f
            val scale = min(size.width, size.height) / (span * 2f)
            val centre = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Palette.Steel.copy(alpha = 0.5f), size.width / 2f, centre, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
            val padPoint = Offset(
                centre.x - (world.targetPadX() - world.x) * scale,
                centre.y + (world.targetPadZ() - world.z) * scale
            )
            drawCircle(Palette.Cyan.copy(alpha = 0.85f), world.site.padRadius * scale, padPoint, style = androidx.compose.ui.graphics.drawscope.Stroke(2.4f))
            drawCircle(Palette.Cyan.copy(alpha = 0.35f), 3f, padPoint)
            for (pod in world.pods) {
                if (pod.taken) continue
                drawCircle(
                    Palette.Amber.copy(alpha = 0.8f),
                    3.4f,
                    Offset(centre.x - (pod.x - world.x) * scale, centre.y + (pod.z - world.z) * scale)
                )
            }
            for (rock in world.rocks) {
                val point = Offset(centre.x - (rock.x - world.x) * scale, centre.y + (rock.z - world.z) * scale)
                if (hypot(point.x - centre.x, point.y - centre.y) > size.width / 2f) continue
                val solid = rock.kind != RockKind.Boulder
                drawCircle(Palette.Muted.copy(alpha = if (solid) 0.75f else 0.4f), if (solid) 3f else 2f, point)
            }
            drawCircle(Palette.Cream, 4f, centre)
            drawLine(
                Palette.Gold,
                centre,
                Offset(centre.x - world.vx * 2.4f, centre.y + world.vz * 2.4f),
                2.6f
            )
        }
    }
}

@Composable
private fun BlindPanel(world: Descent, tick: Int) {
    val slot = tick
    Column(
        Modifier
            .size(126.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Ink.copy(alpha = 0.62f))
            .padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "SCOPE OFFLINE",
            color = Palette.Danger,
            fontFamily = GameFonts.primary,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Text(
            format(world.offsetFromPad(), 0) + " m",
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "to the deck",
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 12.sp
        )
        Text(
            "BEARING " + format(bearing(world), 0) + "°",
            color = Palette.Amber,
            fontFamily = GameFonts.hud,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private fun bearing(world: Descent): Float {
    val dx = world.targetPadX() - world.x
    val dz = world.targetPadZ() - world.z
    var deg = Math.toDegrees(kotlin.math.atan2(dx.toDouble(), dz.toDouble())).toFloat()
    if (deg < 0f) deg += 360f
    return deg
}

@Composable
private fun PerchBanner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "PERCHED — HOLD THRUST TO LIFT OFF",
            color = Palette.Cyan,
            fontFamily = GameFonts.primary,
            fontSize = 15.sp,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.Ink.copy(alpha = 0.72f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun Controls(presenter: DescentContract.Presenter, world: Descent) {
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TiltStick(presenter)
            ThrustPad(presenter, world)
        }
    }
}

@Composable
private fun TiltStick(presenter: DescentContract.Presenter) {
    var knob by remember { mutableStateOf(Offset.Zero) }
    Box(
        Modifier
            .size(142.dp)
            .clip(CircleShape)
            .background(Palette.Ink.copy(alpha = 0.42f))
            .pointerInput(presenter) {
                val radius = size.width / 2f
                detectDragGestures(
                    onDragStart = { start ->
                        val local = start - Offset(radius, radius)
                        knob = clampToCircle(local, radius)
                        presenter.stick(-knob.x / radius, -knob.y / radius)
                    },
                    onDrag = { change, drag ->
                        change.consume()
                        knob = clampToCircle(knob + drag, radius)
                        presenter.stick(-knob.x / radius, -knob.y / radius)
                    },
                    onDragEnd = {
                        knob = Offset.Zero
                        presenter.stick(0f, 0f)
                    },
                    onDragCancel = {
                        knob = Offset.Zero
                        presenter.stick(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val centre = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Palette.Steel.copy(alpha = 0.55f), size.width / 2f - 2f, centre, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            drawLine(Palette.Steel.copy(alpha = 0.4f), Offset(centre.x, 10f), Offset(centre.x, size.height - 10f), 1.2f)
            drawLine(Palette.Steel.copy(alpha = 0.4f), Offset(10f, centre.y), Offset(size.width - 10f, centre.y), 1.2f)
            drawCircle(Palette.Cyan.copy(alpha = 0.85f), 22f, centre + knob)
        }
        Text(
            "TILT",
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
    }
}

private fun clampToCircle(point: Offset, radius: Float): Offset {
    val len = hypot(point.x, point.y)
    if (len <= radius) return point
    val k = radius / len
    return Offset(point.x * k, point.y * k)
}

@Composable
private fun ThrustPad(presenter: DescentContract.Presenter, world: Descent) {
    val tick = presenter.pulse
    Box(
        Modifier
            .size(132.dp)
            .clip(CircleShape)
            .background(if (world.thrusting) Palette.Amber.copy(alpha = 0.32f) else Palette.Ink.copy(alpha = 0.45f))
            .pointerInput(presenter) {
                detectTapGestures(
                    onPress = {
                        presenter.throttle(true)
                        tryAwaitRelease()
                        presenter.throttle(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = tick
            drawCircle(
                if (world.thrusting) Palette.Amber else Palette.Steel,
                size.width / 2f - 3f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(3f)
            )
        }
        Text(
            "THRUST",
            color = if (world.thrusting) Palette.Amber else Palette.Cream,
            fontFamily = GameFonts.primary,
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun PauseCurtain(onResume: () -> Unit, onRestart: () -> Unit, onQuit: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Palette.Ink.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "HOLD",
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 26.sp,
                letterSpacing = 5.sp
            )
            Spacer(Modifier.height(22.dp))
            OrnateButton("Resume", onResume, Modifier.fillMaxWidth().height(54.dp), Palette.Green)
            Spacer(Modifier.height(10.dp))
            OrnateButton("Restart descent", onRestart, Modifier.fillMaxWidth().height(54.dp), Palette.Blue)
            Spacer(Modifier.height(10.dp))
            OrnateButton("Abort to sites", onQuit, Modifier.fillMaxWidth().height(54.dp), Palette.Danger)
        }
    }
}

@Composable
private fun OutcomeFlash(summary: RunSummary) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                if (summary.landed) Palette.Cyan.copy(alpha = 0.1f) else Palette.Danger.copy(alpha = 0.16f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (summary.landed) "TOUCHDOWN" else "LOST",
            color = if (summary.landed) Palette.Cyan else Palette.Danger,
            fontFamily = GameFonts.primary,
            fontSize = 30.sp,
            letterSpacing = 6.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun format(value: Float, digits: Int): String {
    val v = if (abs(value) < 0.05f) 0f else value
    return if (digits == 0) v.toInt().toString() else String.format("%.${digits}f", v)
}
