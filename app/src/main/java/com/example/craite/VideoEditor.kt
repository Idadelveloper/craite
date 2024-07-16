package com.example.craite

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.add
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.Transformer
import com.example.craite.data.CraiteTextOverlay
import com.example.craite.data.EditSettings
import com.example.craite.data.MediaEffect
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class VideoEditor {
    private val videoEffects = VideoEffects()

    @OptIn(UnstableApi::class)
    suspend fun trimAndMergeToTempFile(
        context: Context,
        editSettings: EditSettings,
        mediaNameMap: Map<String, String>
    ): Result<String> = runCatching {
        // Sort video edits by ID to ensure correct order
        val sortedVideoEdits = editSettings.video_edits.sortedBy { it.id }

        val editedMediaItems = coroutineScope {
            sortedVideoEdits.mapIndexed { index, videoEdit ->
                async {
                    val videoName = videoEdit.video_name
                    val filePath = mediaNameMap[videoName]
                    Log.d("VideoEditor", "Processing video at: $filePath")
                    filePath?.let {
                        val file = File(it)
                        if (file.exists()) {
                            val inputUri = Uri.fromFile(file)
                            val outputFile =
                                File.createTempFile("edited_$index", ".mp4", context.cacheDir)
                            val outputPath = outputFile.absolutePath
                            Log.d("VideoEditor", "Text: ${videoEdit.text}")
                            Log.d("VideoEditor", "Effects: ${videoEdit.effects}")
                            Log.d("VideoEditor", "Start Time: ${videoEdit.start_time}")
                            Log.d("VideoEditor", "End Time: ${videoEdit.end_time}")
                            Log.d("Video Editor", "Edit Settings: $editSettings")

                            trimAndApplyEditsToVideo(
                                context,
                                inputUri,
                                outputPath,
                                videoEdit.start_time,
                                videoEdit.end_time,
                                mapMediaEffectsToEffects(videoEdit.effects).toMutableList(), // Make effects mutable
                                videoEdit.text
                            )
                        } else {
                            Log.e("VideoEditor", "File not found: $filePath")
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull() // Filter out null results directly
        }

        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile("temp_merged_video", ".mp4", context.cacheDir)
        }

        if (!mergeVideos(context, editedMediaItems, tempFile.absolutePath)) {
            throw Exception("Video merging failed")
        }
        tempFile.absolutePath
    }

    @OptIn(UnstableApi::class)
    private suspend fun trimAndApplyEditsToVideo(
        context: Context,
        inputUri: Uri,
        outputPath: String,
        startTimeSeconds: Double,
        endTimeSeconds: Double,
        effects: MutableList<Effect>, // Effects list is now mutable
        textOverlays: List<CraiteTextOverlay>
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
                // Convert start and end times to milliseconds
                val startTimeMillis = (startTimeSeconds * 1000).toLong()
                val endTimeMillis = (endTimeSeconds * 1000).toLong()

                val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)

                // Apply trimming based on startTime and endTime (in milliseconds)
                mediaItemBuilder.setClippingConfiguration(
                    ClippingConfiguration.Builder()
                        .setStartPositionMs(startTimeMillis)
                        .setEndPositionMs(endTimeMillis)
                        .build()
                )

                // Apply text overlays
                val textureOverlays = textOverlays.map { textOverlay ->
                    videoEffects.addStaticTextOverlay(
                        text = textOverlay.text,
                        fontSize = 30,
                        textColor = Color(android.graphics.Color.parseColor(textOverlay.text_color)).toArgb(),
                        backgroundColor = Color(android.graphics.Color.parseColor(textOverlay.background_color)).toArgb()
                    )
                }

                // Add OverlayEffect to the effects list
                if (textureOverlays.isNotEmpty()) {
                    for (overlay in textureOverlays) {
                        effects += OverlayEffect(ImmutableList.of(overlay))
                    }
                }
                Log.d("VideoEditor", "Text Overlays: $textureOverlays")

                editedMediaItem = EditedMediaItem.Builder(mediaItemBuilder.build())
                    .setEffects(
                        Effects(
                            emptyList(),
                            effects
                        )
                    )
                    .build()
                Log.d(
                    "VideoEditor",
                    "Effects applied: $effects, Text Overlays: $textOverlays"
                )

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
        context: Context,
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
            Log.d("VideoEditor", "Merging videos: $editedMediaItems to $outputFilePath") // Log the merging process

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

    @OptIn(UnstableApi::class)
    private fun mapMediaEffectsToEffects(mediaEffects: List<MediaEffect>): List<Effect> {
        val effects = mutableListOf<Effect>()
        mediaEffects.forEach { effect ->
            when (effect.name) {
                "brightness" -> effects.add(videoEffects.brightness(effect.adjustment[0]))
                "contrast" -> effects.add(videoEffects.contrast(effect.adjustment[0]))
                "saturation" -> effects.add(videoEffects.saturation(effect.adjustment[0]))
                // Add more mappings for other effect types as needed
            }
        }
        return effects
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