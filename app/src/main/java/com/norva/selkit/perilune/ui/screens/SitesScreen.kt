package com.norva.selkit.perilune.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.game.Objective
import com.norva.selkit.perilune.game.Site
import com.norva.selkit.perilune.game.SiteCatalog
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.common.OrnateButton
import com.norva.selkit.perilune.ui.common.StarRow
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette

@Composable
fun SitesScreen(prefs: PerilunePrefs, backdrop: String, onPick: (Int) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    val state = rememberLazyListState()
    val focus = (0 until SiteCatalog.SITE_COUNT).firstOrNull { prefs.unlocked(it) && prefs.stars(it) == 0 } ?: 0
    LaunchedEffect(focus) {
        state.scrollToItem((focus + focus / 10).coerceAtMost(SiteCatalog.SITE_COUNT + 3))
    }
    Backdrop(backdrop) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OrnateButton("Back", onBack, Modifier.height(44.dp), Palette.Blue, fontSize = 14)
                Text(
                    "LANDING SITES",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontSize = 17.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (chapter in SiteCatalog.chapters.indices) {
                    item(key = "head_$chapter") {
                        ChapterHeader(chapter, prefs.chapterStars(chapter))
                    }
                    items(
                        SiteCatalog.sites.filter { it.chapter == chapter },
                        key = { it.index }
                    ) { site ->
                        SiteRow(site, prefs.stars(site.index), prefs.unlocked(site.index)) { onPick(site.index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterHeader(chapter: Int, stars: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = if (chapter == 0) 0.dp else 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Panel.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                SiteCatalog.chapters[chapter].uppercase(),
                color = Palette.Amber,
                fontFamily = GameFonts.primary,
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Art("ic_star", Modifier.size(14.dp))
                Text(
                    "  $stars / 30",
                    color = Palette.Cream,
                    fontFamily = GameFonts.hud,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            SiteCatalog.chapterBrief[chapter],
            color = Palette.Muted,
            fontFamily = GameFonts.hud,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun SiteRow(site: Site, stars: Int, unlocked: Boolean, onPick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Deep.copy(alpha = if (unlocked) 0.82f else 0.5f))
            .clickable(enabled = unlocked) { onPick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            Art("btn_node", Modifier.matchParentSize(), ContentScale.Fit, alpha = if (unlocked) 1f else 0.4f)
            if (unlocked) {
                Text(
                    "${site.index + 1}",
                    color = Palette.Cream,
                    fontFamily = GameFonts.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            } else {
                Art("ic_lock", Modifier.size(22.dp))
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                site.name,
                color = if (unlocked) Palette.Cream else Palette.Muted,
                fontFamily = GameFonts.primary,
                fontSize = 15.sp
            )
            if (unlocked) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        site.objective.tag,
                        color = Palette.Ink,
                        fontFamily = GameFonts.primary,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (site.objective == Objective.Touchdown) Palette.Muted else Palette.Amber)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                    Text(
                        "  ${SiteCatalog.difficulty(site)}",
                        color = Palette.Muted,
                        fontFamily = GameFonts.hud,
                        fontSize = 12.sp
                    )
                }
                Text(
                    site.brief,
                    color = Palette.Muted,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            } else {
                Text(
                    "Land the previous site to unlock",
                    color = Palette.Muted,
                    fontFamily = GameFonts.hud,
                    fontSize = 12.sp
                )
            }
        }
        StarRow(stars, 3, 15)
    }
}
