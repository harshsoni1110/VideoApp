package com.one.technologies.allVideo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.one.technologies.allVideo.models.AllVideos
import com.one.technologies.allVideo.models.Video
import com.one.technologies.allVideo.models.VideoStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private fun checkForExistingDownloads() {
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