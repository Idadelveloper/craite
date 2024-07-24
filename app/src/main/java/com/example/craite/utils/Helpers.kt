package com.example.craite.utils

class Helpers {
    companion object {
        fun formatTime(timeInMillis: Long): String {
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
}