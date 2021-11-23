package com.one.technologies.videoPlayer

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.FragmentActivity
import com.one.technologies.R

const val videoUrlPath = "videoUrlPath"

class VideoPlayerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)
        val videoView = findViewById<View>(R.id.videoView1) as VideoView

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        var videoUrl: String? = null
        intent.extras?.let {
            videoUrl = it.getString(videoUrlPath)
        }

        videoUrl?.let {
            val uri = Uri.parse(videoUrl)
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(uri)
            videoView.requestFocus()
            videoView.start()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

    }

}

