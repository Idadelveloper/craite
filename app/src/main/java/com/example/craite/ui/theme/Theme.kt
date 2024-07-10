package com.example.craite.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext



class AppColorScheme{
    private val appColors = AppColor()
    val darkColorScheme = darkColorScheme(
        primary = appColors.greenSeed,
        onPrimary = appColors.black,
        primaryContainer = appColors.green20,
        onPrimaryContainer = appColors.green90,

        secondary = appColors.purple70,
        onSecondary = appColors.black,
        secondaryContainer = appColors.purple20,
        onSecondaryContainer = appColors.purple90,

        tertiary = appColors.blueSeed,
        onTertiary = appColors.black,
        tertiaryContainer = appColors.blue20,
        onTertiaryContainer = appColors.blue90,


        background = appColors.black,
        onBackground = appColors.white,
        surface = appColors.neutral15,
        onSurface = appColors.white,

        error = appColors.redSeed,
        onError = appColors.white,
        errorContainer = appColors.red20,
        onErrorContainer = appColors.white,
    )

    val lightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        */
    )
}



@Composable
fun CraiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    MaterialTheme(
        colorScheme = AppColorScheme().darkColorScheme,
        typography = AppTypography().typography,
        content = content
    )
}