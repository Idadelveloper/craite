package com.example.craite

import android.graphics.Matrix
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Brightness
import androidx.media3.effect.MatrixTransformation
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


}