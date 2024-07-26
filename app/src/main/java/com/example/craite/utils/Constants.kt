package com.example.craite.utils

object Constants {
    // Request Codes
    const val REQUEST_CODE_PICK_VIDEOS = 1001

    // Media Processing
    const val THUMBNAIL_QUALITY = 90

    // File Extensions
    const val VIDEO_FILE_EXTENSION = ".mp4"
    const val THUMBNAIL_FILE_EXTENSION = ".jpg"

    // Default Values
    const val DEFAULT_PROJECT_NAME = "My Project"

    // Firestore Constants
    const val FIRESTORE_COLLECTION_USERS = "users"
    const val FIRESTORE_COLLECTION_PROJECTS = "projects"
    const val FIRESTORE_COLLECTION_PROMPTS = "prompts"
    const val FIRESTORE_FIELD_PROMPT = "prompt"
    const val FIRESTORE_FIELD_VIDEO_DIRECTORY = "videoDirectory"
    const val FIRESTORE_FIELD_USER_ID = "userId"
    const val FIRESTORE_FIELD_PROJECT_ID = "projectId"

    // Firebase Storage Constants
    const val STORAGE_PATH_USERS = "users"
    const val STORAGE_PATH_PROJECTS = "projects"
    const val STORAGE_PATH_VIDEOS = "videos"
    const val VIDEO_FILE_PREFIX = "video_"
    const val THUMBNAIL_FILE_PREFIX = "thumbnail_"

    // UI Constants (if any)
    const val TIMELINE_WIDTH = 500f
}