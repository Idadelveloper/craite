package com.example.craite

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.lang.Exception

class VideoEditor(private val context: Context) {

    @OptIn(UnstableApi::class)
    suspend fun trimAndMergeToTempFile(videoFilePaths: List<String>): String? {
        val editedMediaItems = mutableListOf<EditedMediaItem>()
        Toast.makeText(context, "Trimming videos", Toast.LENGTH_SHORT).show()

        // Trim each video and add to the list
        coroutineScope {
            val deferredTrims = mutableListOf<Deferred<EditedMediaItem?>>()

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
                    // Handle trimming failure (log, notify user, etc.)
                    println("Trimming failed for video: ${inputUri}")
                    return@coroutineScope null // Indicate failure by returning null
                }
            }
            editedMediaItems.addAll(deferredTrims.awaitAll().filterNotNull())
        }


        // Generate a temporary file path
        val tempFile = File.createTempFile("temp_merged_video", ".mp4", context.cacheDir)
        val tempFilePath = tempFile.absolutePath

        // Merge to the temporary file
        return if (mergeVideos(editedMediaItems, tempFilePath)) {
            tempFilePath // Return the temporary file path on success
        } else {
            null // Indicate merging failure
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun trimVideo(
        inputUri: Uri,
        outputPath: String,
        startTimeUs: Long,
        endTimeUs: Long
    ): EditedMediaItem? {
        val listener = ExportListener()
        val transformer = Transformer.Builder(context)
            .addListener(listener)
            .build()

        return try {
            // Create MediaItem with clipping
            val clippedVideo = MediaItem.Builder()
                .setUri(inputUri)
                .setClippingConfiguration(
                    ClippingConfiguration.Builder()
                        .setStartPositionMs(startTimeUs / 1000)
                        .setEndPositionMs(endTimeUs / 1000)
                        .build()
                )
                .build()

            // Video Effects
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
                        emptyList(),
                        videoEffects
                    )
                )
                .build()

            // Start trimming using transformer.start()
            transformer.start(editedMediaItem, outputPath)
            Log.d("VideoEditor", "Trimmed video: ${outputPath}")
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
    private fun mergeVideos(
        editedMediaItems: List<EditedMediaItem>,
        outputFilePath: String
    ): Boolean {
        val transformer = Transformer.Builder(context).build()

        try {
            // Build Composition for merging
            val videoSequence = EditedMediaItemSequence(editedMediaItems)
            Log.d("VideoEditor", "Edited media items: $editedMediaItems")
            val composition = Composition.Builder(videoSequence)
                .build()

            // Start merging using transformer.start(). Whether it is even merging the thing sef
            transformer.start(composition, outputFilePath)
            Log.d("VideoEditor", "Merged video: $outputFilePath")
            return true // Indicate success
        } catch (e: ExportException) {
            // Handle exceptions during merging
            e.printStackTrace()
            return false
        } finally {
            transformer.cancel()
        }
    }

    // Helper function to generate output file path (for legacy storage)
    fun generateOutputFilePath(): String {
        val externalStorageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        return File(
            externalStorageDir,
            "merged_video_${System.currentTimeMillis()}.mp4"
        ).absolutePath
    }
}


@UnstableApi
class ExportListener : Transformer.Listener {
    @Deprecated(
        "Deprecated in Java",
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

}