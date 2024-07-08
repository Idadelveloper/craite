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
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class VideoEditor(private val context: Context) {

    @OptIn(UnstableApi::class)
    suspend fun trimAndMergeToTempFile(videoFilePaths: List<String>): String? {
        val editedMediaItems = mutableListOf<EditedMediaItem>()
        Toast.makeText(context, "Trimming videos", Toast.LENGTH_SHORT).show()

        // Trim each video and add to the list
        coroutineScope {
            val deferredTrims = mutableListOf<Deferred<EditedMediaItem?>>()

            for (i in videoFilePaths.indices) {
                val inputUri = Uri.fromFile(File(videoFilePaths[i]))
                val outputFile = File.createTempFile("trimmed_$i", ".mp4", context.cacheDir)
                val outputPath = outputFile.absolutePath

                val startTimeUs = (i * 2000 * 1000).coerceAtLeast(0).toLong()
                val endTimeUs = startTimeUs + 2000 * 1000.toLong()

                deferredTrims.add(async {
                    trimVideo(inputUri, outputPath, startTimeUs, endTimeUs)
                })
            }

            // Wait for all trimmings to complete
            editedMediaItems.addAll(deferredTrims.awaitAll().filterNotNull())
        }

        // Generate a temporary file path for merging
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile("temp_merged_video", ".mp4", context.cacheDir)
        }
        val tempFilePath = tempFile.absolutePath

        // Merge the trimmed videos
        return if (mergeVideos(editedMediaItems.toList(), tempFilePath)) { // Use toList() for immutability
            tempFilePath // Return the merged video path
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
        return suspendCancellableCoroutine { continuation ->
            var editedMediaItem: EditedMediaItem? = null

            val listener = object : Transformer.Listener {
                @Deprecated("Deprecated in Java",
                    ReplaceWith("continuation.resume(editedMediaItem)", "kotlin.coroutines.resume")
                )
                override fun onTransformationCompleted(inputMediaItem: MediaItem) {
                    continuation.resume(editedMediaItem)
                }

                @Deprecated("Deprecated in Java")
                override fun onTransformationError(inputMediaItem: MediaItem, exception: Exception) {
                    Log.e("VideoEditor", "Transformation error: ${exception.message}")
                    continuation.resume(null)
                }
            }

            val transformer = Transformer.Builder(context)
                .addListener(listener)
                .build()

            try {
                val clippedVideo = MediaItem.Builder()
                    .setUri(inputUri)
                    .setClippingConfiguration(
                        ClippingConfiguration.Builder()
                            .setStartPositionMs(startTimeUs / 1000)
                            .setEndPositionMs(endTimeUs / 1000)
                            .build()
                    )
                    .build()

                val videoEffects = mutableListOf<Effect>()
                // ... add your effects here

                editedMediaItem = EditedMediaItem.Builder(clippedVideo)
                    .setEffects(
                        Effects(
                            emptyList(),
                            videoEffects
                        )
                    )
                    .build()

                transformer.start(editedMediaItem, outputPath)

                continuation.invokeOnCancellation {
                    transformer.cancel()
                }
            } catch (e: ExportException) {
                Log.e("VideoEditor", "Export Exception: ${e.message}")
                continuation.resume(null)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun mergeVideos(
        editedMediaItems: List<EditedMediaItem>,
        outputFilePath: String
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val listener = object : Transformer.Listener {
                @Deprecated("Deprecated in Java",
                    ReplaceWith("continuation.resume(true)", "kotlin.coroutines.resume")
                )
                override fun onTransformationCompleted(inputMediaItem: MediaItem) {
                    continuation.resume(true)
                }

                @Deprecated("Deprecated in Java")
                override fun onTransformationError(inputMediaItem: MediaItem, exception: Exception) {
                    Log.e("VideoEditor", "Transformation error: ${exception.message}")
                    continuation.resume(false)
                }
            }

            val transformer = Transformer.Builder(context)
                .addListener(listener)
                .build()

            try {
                val videoSequence = EditedMediaItemSequence(editedMediaItems)
                val composition = Composition.Builder(videoSequence).build()

                transformer.start(composition, outputFilePath)

                continuation.invokeOnCancellation {
                    transformer.cancel()
                }
            } catch (e: ExportException) {
                Log.e("VideoEditor", "Export Exception: ${e.message}")
                continuation.resume(false)
            }
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