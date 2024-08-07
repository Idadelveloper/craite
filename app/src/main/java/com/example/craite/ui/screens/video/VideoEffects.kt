package com.example.craite.ui.screens.video

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.setText
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.TextOverlay
import com.google.errorprone.annotations.CanIgnoreReturnValue
import kotlin.math.min

class  VideoEffects {
    @OptIn(UnstableApi::class)
    fun zoomIn(durationUs: Long = 1_000_000L): MatrixTransformation {
        return MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val progress = min(1f, presentationTimeUs.toFloat() / durationUs)
            val scale = 1f + progress * (2f - 1f) // Zoom in from 1x to 2x
            transformationMatrix.postScale(/* x */ scale, /* y */ scale)
            transformationMatrix
        }
    }

    @OptIn(UnstableApi::class)
    fun zoomOut(durationUs: Long = 1_000_000L): MatrixTransformation {
        return MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val progress = min(1f, presentationTimeUs.toFloat() / durationUs)
            val scale = 1f - progress * (1f - 0.5f) // Zoom out from 1x to 0.5x
            transformationMatrix.postScale(/* x */ scale, /* y */ scale)
            transformationMatrix
        }
    }

    @OptIn(UnstableApi::class)
    fun brightness(brightness: Float = 0.2f): Brightness {
        return Brightness(brightness)
    }

    @SuppressLint("Range")
    @OptIn(UnstableApi::class)
    fun contrast(contrast: Float = 1.2f): Contrast {
        return Contrast(contrast)
    }

    @OptIn(UnstableApi::class)
    fun saturation(saturation: Float): RgbAdjustment {
        val scale = saturation + 1f
        return RgbAdjustment.Builder()
            .setRedScale(scale)
            .setGreenScale(scale)
            .setBlueScale(scale)
            .build()
    }

    @OptIn(UnstableApi::class)
    fun vignette(outerRadius: Float = 0.8f, innerRadius: Float = 0.3f): RgbAdjustment {
        return RgbAdjustment.Builder()
            .setRedScale(vignetteScale(outerRadius, innerRadius))
            .setGreenScale(vignetteScale(outerRadius, innerRadius))
            .setBlueScale(vignetteScale(outerRadius, innerRadius))
            .build()
    }

    // Helper function to calculate the vignette scale (modified)
    private fun vignetteScale(outerRadius: Float, innerRadius: Float): Float {
        // Calculate the average scale for the entire frame (simplified for demonstration)
        val distanceFromCenter = Math.sqrt(
            Math.pow((0.5f - 0.5f).toDouble(), 2.0) + Math.pow((0.5f - 0.5f).toDouble(), 2.0)
        ).toFloat()
        return if (distanceFromCenter > outerRadius) {
            0f // Completely dark outside outer radius
        } else if (distanceFromCenter < innerRadius) {
            1f // No effect inside inner radius
        } else {
            1f - (distanceFromCenter - innerRadius) / (outerRadius - innerRadius) // Gradual darkening
        }
    }

    @OptIn(UnstableApi::class)
    fun sepia(): RgbAdjustment {
        return RgbAdjustment.Builder()
            .setRedScale(0.393f)
            .setGreenScale(0.769f)
            .setBlueScale(0.189f)
            .build()
    }

    @OptIn(UnstableApi::class)
    fun rotate(degrees: Int = 90): ScaleAndRotateTransformation {
        return ScaleAndRotateTransformation.Builder()
            .setRotationDegrees(degrees.toFloat())
            .build()
    }

    @OptIn(UnstableApi::class)
    fun addStaticTextOverlay(
        text: String,
        fontSize: Int = 30, // Font size in pixels
        textColor: Int = Color.White.toArgb(),
        backgroundColor: Int = Color.Transparent.toArgb(),
        x: Float = 0.1f, // Normalized x position (0 to 1)
        y: Float = 0.1f, // Normalized y position (0 to 1)
        width: Float? = null, // Normalized width (null for automatic)
        height: Float? = null, // Normalized height (null for automatic)
        alphaScale: Float = 1.0f, // Alpha (transparency) scale (0 to 1)
        rotationDegrees: Float = 0.0f, // Rotation in degrees (counter-clockwise)
        scaleX: Float = 1.0f, // Horizontal scaling factor
        scaleY: Float = 1.0f, // Vertical scaling factor
        overlayFrameAnchorX: Float = 0.5f, // X anchor point within the overlay (0 to 1)
        overlayFrameAnchorY: Float = 0.5f, // Y anchor point within the overlay (0 to 1)
        backgroundFrameAnchorX: Float = 0.0f, // X anchor point within the background frame (0 to 1)
        backgroundFrameAnchorY: Float = 0.0f, // Y anchor point within the background frame (0 to 1)
        hdrLuminanceMultiplier: Float = 1.0f // Luminance multiplier for HDR frames
    ): BitmapOverlay {
        return TextBitmapOverlay(
            text = text,
            fontSize = fontSize,
            textColor = textColor,
            backgroundColor = backgroundColor,
        )
    }
}

class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = typeface
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.typeface = typeface
    }
}