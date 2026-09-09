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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.AwardBook
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun AwardsScreen(prefs: PerilunePrefs, backdrop: String, onBack: () -> Unit) {
    BackHandler { onBack() }
    val unlocked = AwardBook.all.count { prefs.awardUnlocked(it.id) }
    Backdrop(backdrop) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), Palette.Blue, fontSize = 14)
                Text(
                    "AWARDS  $unlocked / ${AwardBook.all.size}",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            LazyColumn(
                Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AwardBook.all, key = { it.id }) { award ->
                    val open = prefs.awardUnlocked(award.id)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.Deep.copy(alpha = if (open) 0.86f else 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Art(award.icon, Modifier.size(52.dp), alpha = if (open) 1f else 0.28f)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                award.title,
                                color = if (open) Palette.Cream else Palette.Muted,
                                fontFamily = GameFonts.primary,
                                fontSize = 15.sp
                            )
                            Text(
                                award.detail,
                                color = Palette.Muted,
                                fontFamily = GameFonts.hud,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
