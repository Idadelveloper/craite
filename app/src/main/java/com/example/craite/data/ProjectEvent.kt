package com.example.craite.data

import android.net.Uri

sealed interface ProjectEvent {
    data object SaveProject: ProjectEvent
    data class SetProjectName(val projectName: String): ProjectEvent
    data class AddVideo(val videoUri: Uri, val videoList: List<Uri>): ProjectEvent
    data class AddImage(val imageUri: Uri, val imageList: List<Uri>): ProjectEvent
    data class DeleteProject(val project: Project): ProjectEvent
    data object ShowDialog: ProjectEvent
    data object HideDialog: ProjectEvent
    data class SortProject(val sortType: SortType): ProjectEvent

}