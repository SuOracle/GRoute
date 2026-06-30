package net.gozar.app

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val VazirFont = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

val VazirTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = VazirFont),
        displayMedium = displayMedium.copy(fontFamily = VazirFont),
        displaySmall = displaySmall.copy(fontFamily = VazirFont),
        headlineLarge = headlineLarge.copy(fontFamily = VazirFont),
        headlineMedium = headlineMedium.copy(fontFamily = VazirFont),
        headlineSmall = headlineSmall.copy(fontFamily = VazirFont),
        titleLarge = titleLarge.copy(fontFamily = VazirFont),
        titleMedium = titleMedium.copy(fontFamily = VazirFont),
        titleSmall = titleSmall.copy(fontFamily = VazirFont),
        bodyLarge = bodyLarge.copy(fontFamily = VazirFont),
        bodyMedium = bodyMedium.copy(fontFamily = VazirFont),
        bodySmall = bodySmall.copy(fontFamily = VazirFont),
        labelLarge = labelLarge.copy(fontFamily = VazirFont),
        labelMedium = labelMedium.copy(fontFamily = VazirFont),
        labelSmall = labelSmall.copy(fontFamily = VazirFont)
    )
}

val LexendFont = FontFamily(
    Font(R.font.lexend_thin, FontWeight.Thin),
    Font(R.font.lexend_extralight, FontWeight.ExtraLight),
    Font(R.font.lexend_light, FontWeight.Light),
    Font(R.font.lexend_regular, FontWeight.Normal),
    Font(R.font.lexend_medium, FontWeight.Medium),
    Font(R.font.lexend_semibold, FontWeight.SemiBold),
    Font(R.font.lexend_bold, FontWeight.Bold),
    Font(R.font.lexend_extrabold, FontWeight.ExtraBold),
    Font(R.font.lexend_black, FontWeight.Black)
)

val OxaniumFont = FontFamily(
    Font(R.font.oxanium_regular, FontWeight.Normal),
    Font(R.font.oxanium_medium, FontWeight.Medium),
    Font(R.font.oxanium_bold, FontWeight.Bold),
    Font(R.font.oxanium_bold, FontWeight.Bold)
)

val LexendTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = LexendFont),
        displayMedium = displayMedium.copy(fontFamily = LexendFont),
        displaySmall = displaySmall.copy(fontFamily = LexendFont),
        headlineLarge = headlineLarge.copy(fontFamily = LexendFont),
        headlineMedium = headlineMedium.copy(fontFamily = LexendFont),
        headlineSmall = headlineSmall.copy(fontFamily = LexendFont),
        titleLarge = titleLarge.copy(fontFamily = LexendFont),
        titleMedium = titleMedium.copy(fontFamily = LexendFont),
        titleSmall = titleSmall.copy(fontFamily = LexendFont),
        bodyLarge = bodyLarge.copy(fontFamily = LexendFont),
        bodyMedium = bodyMedium.copy(fontFamily = LexendFont),
        bodySmall = bodySmall.copy(fontFamily = LexendFont),
        labelLarge = labelLarge.copy(fontFamily = LexendFont),
        labelMedium = labelMedium.copy(fontFamily = LexendFont),
        labelSmall = labelSmall.copy(fontFamily = LexendFont)
    )
}