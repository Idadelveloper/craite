package com.example.craite.ui.screens.video

import android.graphics.Bitmap
import android.text.StaticLayout
import androidx.annotation.OptIn
import android.text.Layout
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.ui.graphics.Canvas
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay

@OptIn(UnstableApi::class)
class TextBitmapOverlay(
    private val text: String,
    private val fontSize: Int,
    private val textColor: Int,
    private val backgroundColor: Int,
    private val typeface: Typeface = Typeface.DEFAULT
) : BitmapOverlay() {

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        // Calculate text dimensions with wrapping
        val textPaint = TextPaint().apply {
            this.typeface = typeface
            textSize = fontSize.toFloat()
            color = textColor
        }
        val screenWidth = 1000 // Assume a base width for calculation
        val staticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            screenWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER) // Center the text
            .build()

        // Create a bitmap with the calculated dimensions
        val bitmap = Bitmap.createBitmap(
            staticLayout.width,
            staticLayout.height,
            Bitmap.Config.ARGB_8888
        )

        // Draw the text on the bitmap
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(backgroundColor) // Set background color
        staticLayout.draw(canvas)

        return bitmap
    }
}