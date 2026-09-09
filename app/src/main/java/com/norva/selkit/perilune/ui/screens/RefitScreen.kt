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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.data.Upgrade
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun RefitScreen(prefs: PerilunePrefs, backdrop: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    var revision by remember { mutableIntStateOf(0) }
    val refit = remember(revision) { prefs.refit() }
    val available = remember(revision) { prefs.availableStars() }
    Backdrop(backdrop) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), Palette.Blue, fontSize = 14)
                Text(
                    "REFIT",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 16.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
                Box(Modifier.weight(1f))
                Art("ic_star", Modifier.size(18.dp))
                Text(
                    "  $available",
                    color = Palette.Gold,
                    fontFamily = GameFonts.hud,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Text(
                "Stars pay for hardware. Spent stars stay on the record.",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                for (upgrade in Upgrade.entries) {
                    val level = refit.level(upgrade)
                    val maxed = level >= Upgrade.MAX_LEVEL
                    val price = Upgrade.cost(level)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.Panel.copy(alpha = 0.88f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Art(upgrade.icon, Modifier.size(44.dp))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(
                                    upgrade.title,
                                    color = Palette.Cream,
                                    fontFamily = GameFonts.primary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    upgrade.detail,
                                    color = Palette.Muted,
                                    fontFamily = GameFonts.hud,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Text(
                            upgrade.effect,
                            color = Palette.Cyan,
                            fontFamily = GameFonts.hud,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (step in 0 until Upgrade.MAX_LEVEL) {
                                Box(
                                    Modifier
                                        .padding(end = 6.dp)
                                        .width(38.dp)
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (step < level) Palette.Cyan else Palette.Steel)
                                )
                            }
                            Box(Modifier.weight(1f))
                            OrnateButton(
                                text = if (maxed) "Fitted" else "Fit  ·  $price",
                                onClick = {
                                    if (prefs.buyUpgrade(upgrade)) revision++
                                },
                                modifier = Modifier.height(46.dp).width(130.dp),
                                fillColor = if (maxed) Palette.Blue else Palette.Gold,
                                enabled = !maxed && available >= price,
                                fontSize = 14
                            )
                        }
                    }
                }
                OrnateButton(
                    "Strip the module and refund the stars",
                    {
                        prefs.refundRefit()
                        revision++
                    },
                    Modifier.fillMaxWidth().height(50.dp).padding(bottom = 12.dp),
                    Palette.Danger,
                    enabled = refit.levels > 0,
                    fontSize = 13
                )
            }
        }
    }
}
