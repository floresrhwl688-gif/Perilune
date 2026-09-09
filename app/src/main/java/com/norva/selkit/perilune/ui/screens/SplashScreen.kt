package com.norva.selkit.perilune.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.norva.selkit.perilune.ui.common.Art
import com.norva.selkit.perilune.ui.common.Backdrop
import com.norva.selkit.perilune.ui.theme.GameFonts
import com.norva.selkit.perilune.ui.theme.Palette
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(if (shown) 1f else 0f, tween(900), label = "splash")
    LaunchedEffect(Unit) {
        shown = true
        delay(1700)
        onDone()
    }
    Backdrop("bg_splash", dim = 0.55f) {
        Column(
            Modifier.fillMaxSize().padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Art("logo_mark", Modifier.size(168.dp).alpha(fade))
            Text(
                "PERILUNE",
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 34.sp,
                letterSpacing = 6.sp,
                modifier = Modifier.padding(top = 20.dp).alpha(fade)
            )
            Text(
                "Twenty-four decks, one tank of fuel",
                color = Palette.Amber,
                fontFamily = GameFonts.hud,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp).alpha(fade)
            )
        }
    }
}
