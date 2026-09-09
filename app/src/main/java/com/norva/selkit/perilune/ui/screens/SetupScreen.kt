package com.norva.selkit.perilune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.common.backdropNames
import com.norva.selkit.perilune.ui.common.backdrops
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun SetupScreen(
    prefs: PerilunePrefs,
    onBackdrop: (Int) -> Unit,
    onTutorial: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var sound by remember { mutableStateOf(prefs.sound) }
    var vibration by remember { mutableStateOf(prefs.vibration) }
    var chosen by remember { mutableIntStateOf(prefs.backdrop) }
    var confirming by remember { mutableStateOf(false) }
    Backdrop(backdrops[chosen.coerceIn(0, backdrops.lastIndex)]) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), Palette.Blue, fontSize = 14)
                Text(
                    "SETUP",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 16.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.Panel.copy(alpha = 0.88f))
                    .padding(14.dp)
            ) {
                Text(
                    "SURFACE VIEW",
                    color = Palette.Amber,
                    fontFamily = GameFonts.primary,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp
                )
                LazyRow(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(backdrops) { index, name ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(width = 78.dp, height = 104.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (index == chosen) 3.dp else 1.dp,
                                        color = if (index == chosen) Palette.Cyan else Palette.Steel,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        chosen = index
                                        prefs.backdrop = index
                                        onBackdrop(index)
                                    }
                            ) {
                                Art(name, Modifier.matchParentSize(), ContentScale.Crop)
                            }
                            Text(
                                backdropNames[index],
                                color = if (index == chosen) Palette.Cyan else Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.Panel.copy(alpha = 0.88f))
                    .padding(14.dp)
            ) {
                ToggleRow("Sound", sound) {
                    sound = it
                    prefs.sound = it
                }
                ToggleRow("Vibration", vibration) {
                    vibration = it
                    prefs.vibration = it
                }
            }
            OrnateButton(
                "Replay the briefing",
                onTutorial,
                Modifier.fillMaxWidth().height(52.dp).padding(top = 14.dp),
                Palette.Green,
                fontSize = 15
            )
            OrnateButton(
                if (confirming) "Tap again to wipe progress" else "Reset progress",
                {
                    if (confirming) {
                        onReset()
                        confirming = false
                    } else {
                        confirming = true
                    }
                },
                Modifier.fillMaxWidth().height(52.dp).padding(top = 10.dp),
                Palette.Danger,
                fontSize = 14
            )
            Box(Modifier.weight(1f))
            Text(
                "Perilune  ·  twenty-four decks",
                color = Palette.Muted,
                fontFamily = GameFonts.hud,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Palette.Cream,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Switch(
            checked = value,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Palette.Ink,
                checkedTrackColor = Palette.Cyan,
                uncheckedTrackColor = Palette.Steel
            )
        )
    }
}
