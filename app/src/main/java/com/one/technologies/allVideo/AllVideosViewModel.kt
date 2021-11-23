package com.one.technologies.allVideo

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.one.technologies.allVideo.models.AllVideos
import com.one.technologies.allVideo.models.Video
import com.one.technologies.allVideo.models.VideoStatus
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AllVideosViewModel(private val context: Application) : AndroidViewModel(context) {
    val videoList = ArrayList<Video>()
    val allVideos = MutableLiveData<ArrayList<Video>>(videoList)

    init {

        viewModelScope.launch {
            val string = getJsonDataFromAsset(context, "sample_json.txt")

            val gson = Gson()
            val listPersonType = object : TypeToken<AllVideos>() {}.type

            val persons: AllVideos = gson.fromJson(string, listPersonType)
            videoList.addAll(persons.categories[0].videos)
            allVideos.postValue(videoList)
            checkForExistingDownloads()
        }
    }

    fun checkForExistingDownloads() {
        viewModelScope.launch {
            videoList.forEach { video ->
                val videoUrl = video.sources!![0]
                var fileName: String = videoUrl.substring(videoUrl.lastIndexOf('/') + 1)
                fileName =
                    fileName.substring(0, 1).uppercase(Locale.getDefault()) + fileName.substring(1)
                val file: File = File(context.getExternalFilesDir(null)?.absolutePath + fileName)
                if (file.exists()) {
                    Log.d("FILE EXISTS", file.exists().toString())
                    video.videoStatus = VideoStatus.DOWNLOADED
                } else {
                    video.videoStatus = VideoStatus.NOT_DOWNLOADED
                }
            }

            allVideos.postValue(videoList)

        }
    }

    fun downloadVideo(video: Video) {
        val videoUrl = video.sources!![0]
        var fileName: String = videoUrl.substring(videoUrl.lastIndexOf('/') + 1)
        fileName = fileName.substring(0, 1).uppercase(Locale.getDefault()) + fileName.substring(1)
        val file: File = File(context.getExternalFilesDir(null)?.absolutePath + fileName)
        val request = DownloadManager.Request(Uri.parse(videoUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(file))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager =
            context.getSystemService(FragmentActivity.DOWNLOAD_SERVICE) as DownloadManager
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
//                        if (videoList.contains(video)) {
//                            video.videoStatus = VideoStatus.DOWNLOADING
//                            allVideos.postValue(videoList)
//                        }
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
                        finishDownload = true
                        checkForExistingDownloads()
                        Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}