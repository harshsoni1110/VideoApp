package com.one.technologies.allVideo

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.one.technologies.allVideo.models.AllVideos
import com.one.technologies.allVideo.models.Video
import kotlinx.coroutines.launch
import java.io.IOException

class AllVideosViewModel(private val context: Application) : AndroidViewModel(context) {
    val videoList  = ArrayList<Video>()
    val allVideos = MutableLiveData<ArrayList<Video>>(videoList)

    init {

        viewModelScope.launch {
            val string = getJsonDataFromAsset(context, "sample_json.txt")

            val gson = Gson()
            val listPersonType = object : TypeToken<AllVideos>() {}.type

            val persons: AllVideos = gson.fromJson(string, listPersonType)
            videoList.addAll(persons.categories[0].videos)
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