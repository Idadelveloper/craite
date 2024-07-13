package com.example.craite

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.text
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.Transformer
import com.example.craite.data.EditSettings
import com.example.craite.data.VideoEdit
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.internal.immutableListOf
import java.io.File
import kotlin.coroutines.resume
import kotlin.text.forEach
import kotlin.text.toLong

class VideoEditor(private val context: Context, private val editSettings: EditSettings) {
    private val videoEffects = VideoEffects()

    @OptIn(UnstableApi::class)
    suspend fun trimAndMergeToTempFile(
        videoFilePaths: List<String>,
        editSettings: EditSettings
    ): String? {
        val editedMediaItems = mutableListOf<EditedMediaItem>()
        Toast.makeText(context, "Trimming and editing videos", Toast.LENGTH_SHORT).show()

        // Trim and apply edits to each video
        coroutineScope {
            val deferredEdits = mutableListOf<Deferred<EditedMediaItem?>>()

            for (i in videoFilePaths.indices) {
                val inputUri = Uri.fromFile(File(videoFilePaths[i]))
                val outputFile = File.createTempFile("edited_$i", ".mp4", context.cacheDir)
                val outputPath = outputFile.absolutePath
                val videoEdit = editSettings.video_edits.getOrNull(i)

                deferredEdits.add(async {
                    trimAndApplyEditsToVideo(inputUri, outputPath, videoEdit)
                })
            }

            editedMediaItems.addAll(deferredEdits.awaitAll().filterNotNull())
        }

        // Generate a temporary file path for merging
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile("temp_merged_video", ".mp4", context.cacheDir)
        }
        val tempFilePath = tempFile.absolutePath

        // Merge the trimmed and edited videos
        return if (mergeVideos(editedMediaItems.toList(), tempFilePath)) {
            tempFilePath
        } else {
            null
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun trimVideo(
        inputUri: Uri,
        outputPath: String,
        startTimeUs: Long = 0L, // Start trimming at 0 seconds
        endTimeUs: Long = 2000000L // End trimming at 2 seconds (2000 milliseconds * 1000)
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
                            .setStartPositionMs(startTimeUs / 1000) // Start at 0 seconds
                            .setEndPositionMs(endTimeUs / 1000)   // End at 2 seconds
                            .build()
                    )
                    .build()

//                val videoEffects = mutableListOf<Effect>()
                val textOverlay = videoEffects.addStaticTextOverlay(
                    text = "Hello, World!", // Customize the text here
                    fontSize = 100,
                    textColor = Color.Yellow.toArgb(),
                    x = 0.5f, // Center horizontally
                    y = 0.8f, // Near the bottom
                    width = 0.8f // Occupy 80% of the width
                )
                val overlayEffect = OverlayEffect(ImmutableList.of(textOverlay))
                val effects = listOf(
                    videoEffects.zoomIn(),
                    overlayEffect
                )


                editedMediaItem = EditedMediaItem.Builder(clippedVideo)
                    .setEffects(
                        Effects(
                            emptyList(),
                            effects
                        )
                    )
                    .build()
                Log.d("VideoEditor", "Effects applied: $effects")

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
    private suspend fun trimAndApplyEditsToVideo(
        inputUri: Uri,
        outputPath: String,
        videoEdit: VideoEdit?
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

            try {
                val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)
                val effects = mutableListOf<Effect>()

                videoEdit?.let {
                    // Apply trimming based on startTime and endTime
                    mediaItemBuilder.setClippingConfiguration(
                        ClippingConfiguration.Builder()
                            .setStartPositionMs((it.start_time * 1000).toLong())
                            .setEndPositionMs((it.end_time * 1000).toLong())
                            .build()
                    )

                    // Apply effects
                    it.effects.forEach { effect ->
                        when (effect.name) {
                            "brightness" -> effects.add(videoEffects.brightness(effect.adjustment[0]))
                            "contrast" -> effects.add(videoEffects.contrast(effect.adjustment[0]))
                            "saturation" -> effects.add(videoEffects.saturation(effect.adjustment[0]))
                        }
                    }

                    val textOverlays = mutableListOf<TextureOverlay>()
                    it.text.forEach {textOverlay ->
                        textOverlays.add(
                            videoEffects.addStaticTextOverlay(
                                text = textOverlay.text,
                                fontSize = textOverlay.font_size,
                                textColor = Color(android.graphics.Color.parseColor(textOverlay.text_color)).toArgb(),
                                backgroundColor = Color(android.graphics.Color.parseColor(textOverlay.background_color)).toArgb()
                            )
                        )
                    }

                    if (textOverlays.isNotEmpty()) { // Only apply if there are text overlays
                        val overlayEffect = OverlayEffect(ImmutableList.of(textOverlays[0]))
                        effects.add(
                            overlayEffect // Pass the list to OverlayEffect
                        )
                    }

                }

                editedMediaItem = EditedMediaItem.Builder(mediaItemBuilder.build())
                    .setEffects(
                        Effects(
                            emptyList(),
                            effects
                        )
                    )
                    .build()
                Log.d("VideoEditor", "Effects applied: $effects")

                val transformer = Transformer.Builder(context)
                    .addListener(listener)
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