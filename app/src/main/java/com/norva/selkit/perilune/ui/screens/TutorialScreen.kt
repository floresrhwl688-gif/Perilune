package com.norva.selkit.perilune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

private val tips = listOf(
    "Hold THRUST to slow the fall. Every second of burn costs fuel.",
    "Lean with the stick to slide over the deck, then level off before contact.",
    "Touch down slow, level and on the marking to earn all three stars.",
    "Some decks ask more: gather the pods, beat the comms window, or ferry between two decks.",
    "Stars pay for the refit shop — a bigger tank, a stronger engine, faster jets, softer legs."
)

@Composable
fun TutorialScreen(onBegin: () -> Unit) {
    var caption by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            caption = (caption + 1) % tips.size
        }
    }
    BackHandler { onBegin() }
    val cycle = rememberInfiniteTransition(label = "demo")
    val phase by cycle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    Backdrop("bg_menu_mare") {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OrnateButton("Skip", onBegin, Modifier.height(42.dp), Palette.Blue, fontSize = 13)
            }
            Text(
                "DESCENT BRIEFING",
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 20.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                DescentDemo(phase, Modifier.fillMaxSize())
            }
            Text(
                tips[caption],
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in tips.indices) {
                    Box(
                        Modifier
                            .padding(horizontal = 5.dp)
                            .height(8.dp)
                            .let { if (i == caption) it.fillMaxWidth(0.09f) else it.fillMaxWidth(0.045f) }
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = if (i == caption) Palette.Cyan else Palette.Muted.copy(alpha = 0.4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
                            )
                        }
                    }
                }
            }
            OrnateButton(
                "Begin descent",
                onBegin,
                Modifier.fillMaxWidth().height(58.dp).padding(top = 16.dp),
                Palette.Gold,
                fontSize = 18
            )
        }
    }
}

@Composable
private fun DescentDemo(phase: Float, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val groundY = h * 0.82f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Palette.Steel.copy(alpha = 0.15f), Palette.Steel.copy(alpha = 0.55f)),
                startY = groundY,
                endY = h
            ),
            topLeft = Offset(0f, groundY),
            size = Size(w, h - groundY)
        )
        val padW = w * 0.34f
        val padX = w * 0.58f
        drawRoundRect(
            color = Palette.Steel,
            topLeft = Offset(padX - padW / 2f, groundY - 14f),
            size = Size(padW, 14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f)
        )
        drawCircle(Palette.Cyan.copy(alpha = 0.75f), 6f, Offset(padX, groundY - 22f))

        val drop = phase.coerceIn(0f, 1f)
        val landerX = w * 0.22f + (padX - w * 0.22f) * drop
        val landerY = h * 0.12f + (groundY - 42f - h * 0.12f) * (drop * drop * 0.55f + drop * 0.45f)
        val burning = sin(phase * 26f) > -0.2f && drop > 0.12f
        val tilt = (1f - abs(drop * 2f - 1f)) * 0.22f

        if (burning) {
            val flame = Path().apply {
                moveTo(landerX - 10f, landerY + 18f)
                lineTo(landerX + 10f, landerY + 18f)
                lineTo(landerX + 2f, landerY + 52f + sin(phase * 40f) * 8f)
                lineTo(landerX - 2f, landerY + 52f + sin(phase * 40f) * 8f)
                close()
            }
            drawPath(flame, Brush.verticalGradient(listOf(Palette.Amber, Palette.Danger.copy(alpha = 0.15f))))
        }
        val body = Path().apply {
            moveTo(landerX - 20f, landerY)
            lineTo(landerX + 20f, landerY - tilt * 26f)
            lineTo(landerX + 16f, landerY + 20f - tilt * 26f)
            lineTo(landerX - 16f, landerY + 20f)
            close()
        }
        drawPath(body, Palette.Gold)
        drawLine(Palette.Cream, Offset(landerX - 16f, landerY + 20f), Offset(landerX - 26f, landerY + 40f), 4f)
        drawLine(Palette.Cream, Offset(landerX + 16f, landerY + 20f), Offset(landerX + 26f, landerY + 40f), 4f)
        drawCircle(Palette.Cyan, 4f, Offset(landerX, landerY + 8f))
    }
}
