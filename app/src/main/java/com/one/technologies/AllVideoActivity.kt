package com.one.technologies

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.one.technologies.allVideo.AllVideosViewModel
import com.one.technologies.allVideo.VideoListItemAdapter
import com.one.technologies.allVideo.VideoListItemClickListener
import com.one.technologies.allVideo.models.Video
import com.one.technologies.utils.getViewModel
import com.one.technologies.videoPlayer.VideoPlayerActivity
import com.one.technologies.videoPlayer.videoUrlPath
import java.io.File
import java.util.*


class AllVideoActivity : FragmentActivity(), VideoListItemClickListener {
    private lateinit var allVideoAdapter: VideoListItemAdapter
    val viewModel: AllVideosViewModel by lazy {
        getViewModel { AllVideosViewModel(this.application) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_video_screen)
        val rcyAllVideo = findViewById<RecyclerView>(R.id.rcyAllVideos)
        allVideoAdapter = VideoListItemAdapter(viewModel.videoList, this@AllVideoActivity)
        rcyAllVideo.apply {
            adapter = allVideoAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        viewModel.allVideos.observe(this, {
            allVideoAdapter.notifyDataSetChanged()
        })

        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }


    override fun onDownloadClick(video: Video) {

        val videoUrl = video.sources!![0]
        var fileName: String = videoUrl.substring(videoUrl.lastIndexOf('/') + 1)
        fileName = fileName.substring(0, 1).uppercase(Locale.getDefault()) + fileName.substring(1)
        val file: File = File(this.getExternalFilesDir(null)?.absolutePath + fileName)
        if (file.exists()) {
            Log.d("FILE EXISTS", file.exists().toString())
            val i = Intent(this, VideoPlayerActivity::class.java)
            i.putExtra(videoUrlPath, file.absolutePath)
            startActivity(i)
            return
        }
        viewModel.downloadVideo(video)

    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Toast.makeText(this@AllVideoActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            viewModel.checkForExistingDownloads()
        }
    }
}

