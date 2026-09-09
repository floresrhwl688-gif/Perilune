package com.norva.selkit.perilune.ui.theme

import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.norva.selkit.perilune.R

object GameFonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )
    val primary = FontFamily(Font(GoogleFont("Orbitron"), provider))
    val hud = FontFamily(Font(GoogleFont("Rajdhani"), provider))
}
