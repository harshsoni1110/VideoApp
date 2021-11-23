package com.one.technologies.allVideo.models

enum class VideoStatus {
    PROCESSING, DOWNLOADING, NOT_DOWNLOADED, DOWNLOADED
}

data class Video(
    val description: String,
    val title: String,
    val sources: ArrayList<String>?,
    var videoStatus: VideoStatus = VideoStatus.PROCESSING
)