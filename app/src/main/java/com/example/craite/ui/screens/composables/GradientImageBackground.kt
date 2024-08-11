package com.example.craite.ui.screens.composables

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.craite.ui.theme.AppColor


@Composable
fun GradientImageBackground(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    contentDescription: String?,
    gradientColor: Color = AppColor().black,
    videoUri: Uri? = null,
) {
    Box(modifier = Modifier.then(modifier)) {

        if (videoUri != null) {
            VideoPlayer(videoUri = videoUri)
        }

        if (painter != null) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop
            )

        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColor.copy(alpha = 0f),
                            gradientColor
                        ),
                    ),
                ),
        )

    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(videoUri: Uri) {
    val context = LocalContext.current

    val videoPlayer = remember {
        //Old implementation with VideoView
//        VideoView(context).apply {
//            setVideoURI(videoUri)
//            setOnPreparedListener { mediaPlayer ->
//                mediaPlayer.isLooping = true
//                mediaPlayer.setVolume(0f, 0f)
//                start()
//            }
//            start()
//        }

        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            prepare()
        }
    }

    DisposableEffect(

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = videoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL


                }
            }

        )) {
        onDispose { videoPlayer.release() }

    }


}