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

    /**
     * Backed list of the videos
     */
    val videoList = ArrayList<Video>()

    /**
     * LiveData to observer all videos listing changes and update UI
     */
    val allVideos = MutableLiveData<ArrayList<Video>>(videoList)

    init {
        //Start parsing the JSON as soon as screen is launched
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

    /**
     * Check for the file exist of each video parsed
     * This will help to render Play or Download button on UI
     */
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

    /**
     * Trigger downloading of the video
     */
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
        video.videoStatus = VideoStatus.DOWNLOADING
        allVideos.postValue(videoList)
        Log.d("Downloading..", downloadId.toString())
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