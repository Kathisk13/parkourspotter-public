package com.example.maps

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.maps.utils.getPhotoFile
import com.example.maps.utils.setImage
import com.mapbox.geojson.Feature
import kotlinx.android.synthetic.main.spot_details.*

/**
 * Adapter to display the list of spots and handle click events
 */
class SpotAdapter(val context: Context, val spotList: List<Feature>, val onClick: (Int) -> Unit) :
        RecyclerView.Adapter<SpotAdapter.SpotViewHolder>() {

    // Describes an item view and its place within the RecyclerView
    class SpotViewHolder(val context: Context, val spotList: List<Feature>, itemView: View, val onClick: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val spotTextView: TextView = itemView.findViewById(R.id.spot_name)
        private val spotImageView: ImageView = itemView.findViewById(R.id.spot_image)
        private var current_spot: Int = 0
        init {
            itemView.setOnClickListener {
                current_spot?.let { it1 -> onClick(it1) }
            }
        }


        fun bind(spot: Feature, pos: Int) {
            spotTextView.text = spot.getStringProperty("Name")
            val image = spotList[pos].getStringProperty("id")
            setImage(context, image, spotImageView)
            current_spot = pos
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.spot_item, parent, false)
        return SpotViewHolder(context, spotList, view, onClick)
    }

    // Returns size of data list
    override fun getItemCount(): Int {
        return spotList.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        holder.bind(spotList[position], position)
    }
}
