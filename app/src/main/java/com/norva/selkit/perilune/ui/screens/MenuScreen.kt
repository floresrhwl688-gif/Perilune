package com.norva.selkit.perilune.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.game.SiteCatalog
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.common.RoundPlateButton
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun MenuScreen(
    prefs: PerilunePrefs,
    backdrop: String,
    onPlay: () -> Unit,
    onSites: () -> Unit,
    onAwards: () -> Unit,
    onRefit: () -> Unit,
    onRecords: () -> Unit,
    onSetup: () -> Unit,
    onTutorial: () -> Unit
) {
    val activity = LocalContext.current as? Activity
    BackHandler { activity?.finish() }
    val nextSite = (0 until SiteCatalog.SITE_COUNT).firstOrNull { prefs.stars(it) == 0 && prefs.unlocked(it) }
        ?: SiteCatalog.sites.lastIndex
    Backdrop(backdrop) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Palette.Panel.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Art("ic_star", Modifier.size(18.dp))
                Text(
                    "  ${prefs.totalStars()} / ${SiteCatalog.STAR_COUNT}",
                    color = Palette.Cream,
                    fontFamily = GameFonts.hud,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                "PERILUNE",
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 30.sp,
                letterSpacing = 6.sp,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                SiteCatalog.site(nextSite).let { "Next: ${it.name}  ·  ${it.objective.tag}" },
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.size(330.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(172.dp)
                            .clip(CircleShape)
                            .clickable { onPlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Art("btn_node", Modifier.matchParentSize(), ContentScale.Fit)
                        Art("logo_mark", Modifier.size(104.dp))
                        Text(
                            "DESCEND",
                            color = Palette.Cream,
                            fontFamily = GameFonts.primary,
                            fontSize = 15.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 106.dp)
                        )
                    }
                    val spokes = listOf(
                        Triple("hub_sites", "Sites", onSites),
                        Triple("hub_awards", "Awards", onAwards),
                        Triple("hub_refit", "Refit", onRefit),
                        Triple("hub_records", "Records", onRecords),
                        Triple("hub_setup", "Setup", onSetup)
                    )
                    spokes.forEachIndexed { index, spoke ->
                        val angle = Math.toRadians(-90.0 + 72.0 * index)
                        RoundPlateButton(
                            spoke.first,
                            spoke.second,
                            spoke.third,
                            64,
                            Modifier
                                .align(Alignment.Center)
                                .offset(
                                    x = (kotlin.math.cos(angle) * 128.0).dp,
                                    y = (kotlin.math.sin(angle) * 128.0).dp
                                )
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrnateButton(
                    "Briefing",
                    onTutorial,
                    Modifier.weight(1f).height(50.dp),
                    Palette.Blue,
                    fontSize = 15
                )
                OrnateButton(
                    "Exit",
                    { activity?.finish() },
                    Modifier.weight(1f).height(50.dp),
                    Palette.Danger,
                    fontSize = 15
                )
            }
        }
    }
}
