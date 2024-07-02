package com.example.craite

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.RgbFilter
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.TransformationResult
import androidx.media3.transformer.Transformer
import java.io.File
import java.lang.Exception

class VideoEditor(private val context: Context) {
    @OptIn(UnstableApi::class)
    fun trimAndMergeVideos(videoFilePaths: List<String>, outputFilePath: String) {
        val editedMediaItems = mutableListOf<EditedMediaItem>()

        // Trim each video and add to the list
        for (i in videoFilePaths.indices) {
            val inputUri = Uri.fromFile(File(videoFilePaths[i]))
            val outputFile = File.createTempFile("trimmed_$i", ".mp4", context.cacheDir)
            val outputPath = outputFile.absolutePath

            // Calculate trim intervals (customize as needed)
            val startTimeUs = (i * 2000 * 1000).coerceAtLeast(0).toLong()
            val endTimeUs = startTimeUs + 2000 * 1000.toLong()

            val editedItem = trimVideo(inputUri, outputPath, startTimeUs, endTimeUs)
            if (editedItem != null) {
                editedMediaItems.add(editedItem)
            } else {
                // Handle the case where trimming failed for this video
                println("Trimming failed for video: ${inputUri}") // Log the error
                // You might also want to notify the user or take other actions
            }
        }

        // Merge trimmed videos using the collected EditedMediaItems
        mergeVideos(editedMediaItems, outputFilePath)
    }

    @OptIn(UnstableApi::class)
    private fun trimVideo(inputUri: Uri, outputPath: String, startTimeUs: Long, endTimeUs: Long): EditedMediaItem? {
        val listener = ExportListener()
        val transformer = Transformer.Builder(context)
            .addListener(listener)
            .build()

        return try {
            // Create MediaItem with clipping
            val clippedVideo = MediaItem.Builder()
                .setUri(inputUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startTimeUs / 1000) // Use provided startTimeUs
                        .setEndPositionMs(endTimeUs / 1000)   // Use provided endTimeUs
                        .build()
                )
                .build()

            // Define video effects (if needed)
            val videoEffects = mutableListOf<Effect>()
            videoEffects.add(RgbFilter.createGrayscaleFilter())
            videoEffects.add(
                ScaleAndRotateTransformation.Builder()
                    .setScale(0.2f, 0.2f)
                    .build()
            )

            // Create EditedMediaItem
            val editedMediaItem = EditedMediaItem.Builder(clippedVideo)
                .setEffects(
                    Effects(
                        /* audioProcessors= */ emptyList(),
                        /* videoEffects= */ videoEffects
                    )
                )
                .build()

            // Start trimming using transformer.start()
            transformer.start(editedMediaItem, outputPath)
            editedMediaItem // Return the EditedMediaItem if successful
        } catch (e: ExportException) {
            // Handle exceptions during trimming
            e.printStackTrace()
            null // Return null if trimming fails
        } finally {
            transformer.cancel()
        }
    }

    @OptIn(UnstableApi::class)
    private fun mergeVideos(editedMediaItems: List<EditedMediaItem>, outputFilePath: String) {
        val transformer = Transformer.Builder(context).build()

        try {
            // Build Composition for merging
            val videoSequence = EditedMediaItemSequence(editedMediaItems)
            val composition = Composition.Builder(videoSequence)
                .build()

            // Start merging using transformer.start()
            transformer.start(composition, outputFilePath)

        } catch (e: ExportException) {
            // Handle exceptions during merging
            e.printStackTrace()
        } finally {
            transformer.cancel()
        }
    }
}


@UnstableApi
class ExportListener : Transformer.Listener {
    @Deprecated("Deprecated in Java",
        ReplaceWith("println(\"Transformation completed for: \${inputMediaItem.mediaMetadata}\")")
    )
    @OptIn(UnstableApi::class)
    override fun onTransformationCompleted(inputMediaItem: MediaItem) {
        // Handle successful transformation completion
        println("Transformation completed for: ${inputMediaItem.mediaMetadata}")
    }

    override fun onTransformationError(inputMediaItem: MediaItem, exception: Exception) {
        // Handle transformation errors
        println("Transformation error for: ${inputMediaItem.mediaMetadata}, Error: ${exception.message}")
    }

    // Override other methods as needed...
}