package com.norva.selkit.perilune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.Award
import com.norva.selkit.perilune.data.RunSummary
import com.norva.selkit.perilune.game.SiteCatalog
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.common.StarRow
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun DebriefScreen(
    summary: RunSummary,
    awards: List<Award>,
    backdrop: String,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onSites: () -> Unit
) {
    BackHandler { onSites() }
    val site = SiteCatalog.site(summary.site)
    val hasNext = summary.landed && summary.site + 1 < SiteCatalog.sites.size
    Backdrop(backdrop) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (summary.landed) "TOUCHDOWN" else "DESCENT LOST",
                color = if (summary.landed) Palette.Cyan else Palette.Danger,
                fontFamily = GameFonts.primary,
                fontSize = 24.sp,
                letterSpacing = 4.sp
            )
            Text(
                "${site.name} — ${summary.reason}",
                color = Palette.Cream,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Box(Modifier.padding(top = 16.dp)) {
                StarRow(summary.stars, 3, 34)
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.Panel.copy(alpha = 0.86f))
                    .padding(16.dp)
            ) {
                Line("Objective", site.objective.tag)
                Line("Touchdown speed", String.format("%.2f m/s", summary.touchdown))
                Line("Lateral speed", String.format("%.2f m/s", summary.lateral))
                Line("Offset from marking", String.format("%.1f m", summary.offset))
                Line("Fuel remaining", String.format("%.0f %%", summary.fuelPercent))
                Line("Pods collected", summary.pods.toString())
                Line("Flight time", String.format("%.1f s", summary.time))
                Line("Soft-touch target", String.format("%.1f m/s", site.softTouch))
                if (site.timed) Line("Window left", String.format("%.1f s", summary.windowLeft))
                if (site.quota > 0) Line("Pods required", "${summary.pods} / ${site.quota}")
                Line("Third star", site.objective.bonus)
            }
            if (awards.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Palette.Deep.copy(alpha = 0.86f))
                        .padding(12.dp)
                ) {
                    Text(
                        "NEW AWARDS",
                        color = Palette.Amber,
                        fontFamily = GameFonts.primary,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )
                    for (award in awards) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Art(award.icon, Modifier.size(34.dp))
                            Text(
                                "  ${award.title}",
                                color = Palette.Cream,
                                fontFamily = GameFonts.hud,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
            Box(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrnateButton("Retry", onRetry, Modifier.weight(1f).height(52.dp), Palette.Blue, fontSize = 15)
                if (hasNext) {
                    OrnateButton("Next site", onNext, Modifier.weight(1f).height(52.dp), Palette.Gold, fontSize = 15)
                }
            }
            OrnateButton(
                "Back to sites",
                onSites,
                Modifier.fillMaxWidth().height(50.dp).padding(top = 10.dp),
                Palette.Green,
                fontSize = 15
            )
        }
    }
}

@Composable
private fun Line(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 14.sp)
        Text(value, color = Palette.Cream, fontFamily = GameFonts.hud, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
