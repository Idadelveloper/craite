package com.example.craite.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.craite.R

class AppTypography {

//    private val fontProvider = GoogleFont.Provider(
//        providerAuthority = "com.google.android.gms.fonts",
//        providerPackage = "com.google.android.gms",
//        certificates = R.array.com_google_android_gms_fonts_certs
//    )
//
//    private val fontName = GoogleFont("Plus Jakarta Sans")
//    private val fontFamily = FontFamily(
//        Font(googleFont = fontName, fontProvider = fontProvider)
//    )
//

    val plusJakartaSansFamily = FontFamily(
        Font(R.font.plus_jakarta_sans_extra_bold, FontWeight.ExtraBold),
        Font(R.font.plus_jakarta_sans_extra_bold_italic, FontWeight.ExtraBold),
        Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
        Font(R.font.plus_jakarta_sans_bold_italic, FontWeight.Bold),
        Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans_semibold_italic, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans_medium_italic, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
        Font(R.font.plus_jakarta_sans_italic, FontWeight.Normal),
        Font(R.font.plus_jakarta_sans_light, FontWeight.Light),
        Font(R.font.plus_jakarta_sans_light_italic, FontWeight.Light),
        Font(R.font.plus_jakarta_sans_extra_light, FontWeight.ExtraLight),
        Font(R.font.plus_jakarta_sans_extra_light_italic, FontWeight.ExtraLight),

        )


    // Set of Material typography styles to start with
    val typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),

        bodySmall = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        ),

        headlineLarge = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        ),

        headlineMedium = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
        ),

        headlineSmall = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
        ),

        titleLarge = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),

        titleMedium = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),

        titleSmall = TextStyle(
            fontFamily = plusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),


        /* Other default text styles to override
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        */
    )
}