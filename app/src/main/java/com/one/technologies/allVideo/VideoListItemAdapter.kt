package com.one.technologies.allVideo

import android.app.DownloadManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.one.technologies.R
import com.one.technologies.allVideo.models.Video

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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.txtVideoName.text = dataSet[position].title
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
