package com.example.craite.ui.screens.video

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

// Updated ExoPlayer Listener
class ExoPlayerListener(private val viewModel: VideoEditorViewModel, private val exoPlayer: ExoPlayer) : Player.Listener {
    @OptIn(UnstableApi::class)
    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)

        if (events.contains(Player.EVENT_POSITION_DISCONTINUITY) ||
            events.contains(Player.EVENT_TIMELINE_CHANGED)) {
            val newPosition = player.currentPosition
            Log.d("ExoPlayerListener", "New Position: $newPosition")
            viewModel.updateCurrentPosition(newPosition)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d("ExoPlayerListener", "Is Playing Changed: $isPlaying")
        // You can perform additional actions here based on the playing state
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            viewModel.updateDuration(exoPlayer.duration)
        }
    }
}