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
        val request = DownloadManager.Request(Uri.parse(videoUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Log.d("Downloading..", downloadId.toString())

        // using query method
        var finishDownload = false
        var progress = 0
        while (!finishDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
            if (cursor.moveToFirst()) {
                val columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor!!.getInt(columnStatus)
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        val reasonStatus = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        Log.d("DOWNLOADING..", "Failed ${cursor.getString(reasonStatus)}")

                        finishDownload = true;
                    }
                    DownloadManager.STATUS_PAUSED -> {

                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {

//                        val total =
//                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
//                        if (total >= 0) {
//                            val downloaded =
//                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
//                            progress = ((downloaded * 100L) / total).toInt()
//                            // if you use downloadmanger in async task, here you can use like this to display progress.
//                            // Don't forget to do the division in long to get more digits rather than double.
//                            //  publishProgress((int) ((downloaded * 100L) / total));
//                        }
                        break;
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100;
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true;
                        Toast.makeText(this, "Download Completed", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }

    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
//            if (downloadID === id) {
            Toast.makeText(this@AllVideoActivity, "Download Completed", Toast.LENGTH_SHORT).show()
//            }
        }
    }
}

