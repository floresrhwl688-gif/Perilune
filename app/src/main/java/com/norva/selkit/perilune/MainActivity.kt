package com.norva.selkit.perilune

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.norva.selkit.perilune.audio.Haptics
import com.norva.selkit.perilune.audio.SoundBox
import com.norva.selkit.perilune.data.PerilunePrefs
import com.norva.selkit.perilune.game.DescentPresenter
import com.norva.selkit.perilune.game.SiteCatalog
import com.norva.selkit.perilune.presentation.Route
import com.norva.selkit.perilune.presentation.ShellPresenter
import com.norva.selkit.perilune.ui.common.backdrops
import com.norva.selkit.perilune.ui.screens.AwardsScreen
import com.norva.selkit.perilune.ui.screens.DebriefScreen
import com.norva.selkit.perilune.ui.screens.FlightScreen
import com.norva.selkit.perilune.ui.screens.MenuScreen
import com.norva.selkit.perilune.ui.screens.RecordsScreen
import com.norva.selkit.perilune.ui.screens.RefitScreen
import com.norva.selkit.perilune.ui.screens.SetupScreen
import com.norva.selkit.perilune.ui.screens.SitesScreen
import com.norva.selkit.perilune.ui.screens.SplashScreen
import com.norva.selkit.perilune.ui.screens.TutorialScreen
import com.norva.selkit.perilune.ui.theme.Palette
import com.norva.selkit.perilune.ui.theme.PeriluneTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PerilunePrefs
    private lateinit var sound: SoundBox
    private lateinit var haptics: Haptics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        prefs = PerilunePrefs(this)
        sound = SoundBox(this, prefs)
        haptics = Haptics(this, prefs)

        setContent {
            PeriluneTheme {
                Box(Modifier.fillMaxSize().background(Palette.Ink)) {
                    Shell(prefs, sound, haptics)
                }
            }
        }
    }

    override fun onDestroy() {
        sound.release()
        super.onDestroy()
    }
}

@Composable
private fun Shell(prefs: PerilunePrefs, sound: SoundBox, haptics: Haptics) {
    val shell = remember { ShellPresenter(prefs) }
    var skin by remember { mutableIntStateOf(prefs.backdrop) }
    val backdrop = backdrops[skin.coerceIn(0, backdrops.lastIndex)]

    when (val route = shell.route) {
        Route.Splash -> SplashScreen { shell.swap(shell.openingRoute()) }

        Route.Tutorial -> TutorialScreen {
            prefs.tutorialSeen = true
            if (!shell.back()) shell.swap(Route.Menu)
        }

        Route.Menu -> MenuScreen(
            prefs = prefs,
            backdrop = backdrop,
            onPlay = {
                val next = (0 until SiteCatalog.SITE_COUNT)
                    .firstOrNull { prefs.unlocked(it) && prefs.stars(it) == 0 } ?: 0
                shell.go(Route.Flight(next))
            },
            onSites = { shell.go(Route.Sites) },
            onAwards = { shell.go(Route.Awards) },
            onRefit = { shell.go(Route.Refit) },
            onRecords = { shell.go(Route.Records) },
            onSetup = { shell.go(Route.Setup) },
            onTutorial = { shell.go(Route.Tutorial) }
        )

        Route.Sites -> SitesScreen(
            prefs = prefs,
            backdrop = backdrop,
            onPick = { shell.go(Route.Flight(it)) },
            onBack = { if (!shell.back()) shell.home() }
        )

        is Route.Flight -> {
            val presenter = remember(route.site) {
                DescentPresenter(SiteCatalog.site(route.site), prefs)
            }
            FlightScreen(
                presenter = presenter,
                sound = sound,
                haptics = haptics,
                onFinish = { summary, awards -> shell.swap(Route.Debrief(summary, awards)) },
                onQuit = { if (!shell.back()) shell.home() }
            )
        }

        is Route.Debrief -> DebriefScreen(
            summary = route.summary,
            awards = route.awards,
            backdrop = backdrop,
            onRetry = { shell.swap(Route.Flight(route.summary.site)) },
            onNext = { shell.swap(Route.Flight(route.summary.site + 1)) },
            onSites = {
                shell.home()
                shell.go(Route.Sites)
            }
        )

        Route.Awards -> AwardsScreen(prefs, backdrop) { if (!shell.back()) shell.home() }

        Route.Refit -> RefitScreen(prefs, backdrop) { if (!shell.back()) shell.home() }

        Route.Records -> RecordsScreen(prefs, backdrop) { if (!shell.back()) shell.home() }

        Route.Setup -> SetupScreen(
            prefs = prefs,
            onBackdrop = { skin = it },
            onTutorial = { shell.go(Route.Tutorial) },
            onReset = {
                prefs.resetProgress()
                skin = prefs.backdrop
            },
            onBack = { if (!shell.back()) shell.home() }
        )
    }
}
