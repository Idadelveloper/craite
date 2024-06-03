package com.example.craite.data

import android.net.Uri

sealed interface ProjectEvent {
    object SaveProject: ProjectEvent
    data class SetProjectName(val name: String): ProjectEvent
    data class AddVideo(val uri: Uri): ProjectEvent
    data class AddImage(val uri: Uri): ProjectEvent
    data class DeleteProject(val projectId: String): ProjectEvent
}