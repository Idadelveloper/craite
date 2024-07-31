package com.example.craite.utils

import android.annotation.SuppressLint

class Helpers {
    companion object {
        @SuppressLint("DefaultLocale")
        fun formatTime(timeInMillis: Long): String {
            val totalSeconds = timeInMillis / 1000
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }

        fun calculateIntervals(totalDuration: Long): List<Long> {
            val intervals = mutableListOf<Long>()
            var currentTime = 0L
            val intervalDuration = 100L // 0.1 second in milliseconds (adjust as needed)

            while (currentTime <= totalDuration) {
                intervals.add(currentTime)
                currentTime += intervalDuration
            }

            return intervals
        }
    }
}