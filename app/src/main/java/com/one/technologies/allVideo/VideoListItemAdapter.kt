package com.one.technologies.allVideo

import android.app.DownloadManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.bumptech.glide.Glide
import com.one.technologies.R
import com.one.technologies.allVideo.models.Video
import com.one.technologies.allVideo.models.VideoStatus

interface VideoListItemClickListener {
    fun onDownloadClick(video: Video)
}

class VideoListItemAdapter(
    private val dataSet: ArrayList<Video>,
    private val videoListItemClickListener: VideoListItemClickListener
) :
    RecyclerView.Adapter<VideoListItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtVideoName: TextView = view.findViewById(R.id.txtVideoName)
        val btnDownload: Button = view.findViewById(R.id.btnDownload)
        val prgVideoProcessing: ProgressBar = view.findViewById(R.id.prgVideoProcessing)
        val imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)

        init {
            // Define click listener for the ViewHolder's View.
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.video_list_item, viewGroup, false)
        val vh = ViewHolder(view)
        vh.btnDownload.setOnClickListener {
            val position = vh.adapterPosition
            if (position != NO_POSITION) {
                videoListItemClickListener.onDownloadClick(dataSet[position])
            }
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val video = dataSet[position]
        viewHolder.txtVideoName.text = video.title
        val videoUrl = video.sources!![0]
        val fileName: String = videoUrl.substring(0, videoUrl.lastIndexOf('/'))
        Glide
            .with(viewHolder.imgThumbnail)
            .load(fileName + "/"+ video.thumb)
            .centerCrop()
            .into(viewHolder.imgThumbnail);

        when (video.videoStatus) {
            VideoStatus.DOWNLOADING -> {
                viewHolder.prgVideoProcessing.visibility = View.VISIBLE
                viewHolder.btnDownload.visibility = View.GONE
            }
            VideoStatus.DOWNLOADED -> {
                viewHolder.btnDownload.text =
                    viewHolder.btnDownload.context.getString(R.string.play)
                viewHolder.prgVideoProcessing.visibility = View.GONE
                viewHolder.btnDownload.visibility = View.VISIBLE
            }
            else -> {
                viewHolder.prgVideoProcessing.visibility = View.GONE
                viewHolder.btnDownload.visibility = View.VISIBLE
                viewHolder.btnDownload.text =
                    viewHolder.btnDownload.context.getString(R.string.download)
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
