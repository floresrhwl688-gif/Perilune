package com.norva.selkit.perilune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.game.SiteCatalog
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun RecordsScreen(prefs: PerilunePrefs, backdrop: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    Backdrop(backdrop) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), Palette.Blue, fontSize = 14)
                Text(
                    "FLIGHT RECORD",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Panel {
                    Stat("Sites landed", "${prefs.sitesCleared()} / ${SiteCatalog.SITE_COUNT}")
                    Stat("Stars earned", "${prefs.totalStars()} / ${SiteCatalog.STAR_COUNT}")
                    Stat("Stars spent on refit", prefs.spentStars().toString())
                    Stat("Successful landings", prefs.statLandings().toString())
                    Stat("Descents lost", prefs.statCrashes().toString())
                    Stat("Fuel pods collected", prefs.statPods().toString())
                    Stat("Time in flight", String.format("%.0f s", prefs.statFlightTime()))
                    Stat(
                        "Softest touchdown",
                        if (prefs.statSoftest() > 0f) String.format("%.2f m/s", prefs.statSoftest()) else "—"
                    )
                    Stat(
                        "Most fuel spared",
                        if (prefs.statFuelSpared() > 0f) String.format("%.0f %%", prefs.statFuelSpared()) else "—"
                    )
                }
                for (chapter in SiteCatalog.chapters.indices) {
                    Text(
                        SiteCatalog.chapters[chapter].uppercase(),
                        color = Palette.Amber,
                        fontFamily = GameFonts.primary,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                    )
                    Panel {
                        for (site in SiteCatalog.sites.filter { it.chapter == chapter }) {
                            val time = prefs.bestTime(site.index)
                            Stat(
                                site.name,
                                if (time > 0f) String.format("%.1f s  ·  %d★", time, prefs.stars(site.index)) else "not landed"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Panel.copy(alpha = 0.86f))
            .padding(14.dp)
    ) { content() }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Palette.Muted, fontFamily = GameFonts.hud, fontSize = 14.sp)
        Text(value, color = Palette.Cream, fontFamily = GameFonts.hud, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
