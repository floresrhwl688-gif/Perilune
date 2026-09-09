package com.norva.selkit.perilune.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

val backdrops = listOf("bg_menu_mare", "bg_menu_highland", "bg_menu_farside", "bg_menu_terminator")

val backdropNames = listOf("Mare", "Highlands", "Far Side", "Terminator")

@Composable
fun artId(name: String): Int {
    val context = LocalContext.current
    return remember(name) { context.resources.getIdentifier(name, "drawable", context.packageName) }
}

@Composable
fun Art(name: String, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Fit, alpha: Float = 1f) {
    val id = artId(name)
    if (id != 0) Image(painterResource(id), null, modifier, contentScale = scale, alpha = alpha)
}

private fun plateFor(fill: Color): String = when (fill) {
    Palette.Blue -> "btn_plate_blue"
    Palette.Gold -> "btn_plate_gold"
    Palette.Danger -> "btn_plate_red"
    else -> "btn_plate_green"
}

@Composable
fun OrnateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fillColor: Color = Palette.Green,
    textColor: Color? = null,
    enabled: Boolean = true,
    fontSize: Int = 17
) {
    val plate = artId(plateFor(fillColor))
    val alpha = if (enabled) 1f else 0.45f
    val label = (textColor ?: if (fillColor == Palette.Gold) Palette.Ink else Palette.Cream).copy(alpha = alpha)
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (plate != 0) {
            Image(
                painterResource(plate),
                null,
                Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                alpha = alpha
            )
        } else {
            Box(Modifier.matchParentSize().background(fillColor.copy(alpha = alpha)))
        }
        Text(
            text,
            color = label,
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun RoundPlateButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    size: Int = 74,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size.dp)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Art(icon, Modifier.matchParentSize(), ContentScale.Fit)
        }
        Text(
            label,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = (size + 10).dp)
        )
    }
}

@Composable
fun StarRow(earned: Int, total: Int = 3, size: Int = 16) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            Art("ic_star", Modifier.size(size.dp), alpha = if (i < earned) 1f else 0.22f)
        }
    }
}

@Composable
fun GlassPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Panel.copy(alpha = 0.82f))
            .padding(14.dp)
    ) { content() }
}
