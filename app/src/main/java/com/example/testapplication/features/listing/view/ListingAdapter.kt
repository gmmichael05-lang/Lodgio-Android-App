package com.example.testapplication.features.listing.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.testapplication.R
import com.example.testapplication.features.listing.model.ListingDTO

class ListingAdapter(
    private var items: List<ListingDTO> = emptyList(),
    private val onItemClick: (ListingDTO) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    fun updateData(newItems: List<ListingDTO>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_listing_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvListingTitle)
        private val tvCity: TextView = itemView.findViewById(R.id.tvListingCity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvListingPrice)
        private val tvType: TextView = itemView.findViewById(R.id.tvListingType)
        private val tvBedsBaths: TextView = itemView.findViewById(R.id.tvBedsBaths)
        private val ivListingImage: ImageView? = itemView.findViewById(R.id.ivListingImage)

        fun bind(listing: ListingDTO) {
            tvTitle.text = listing.title ?: "Untitled"
            tvCity.text = listing.city ?: ""
            tvPrice.text = "₱${String.format("%,.0f", listing.pricePerNight ?: 0.0)}"
            tvType.text = listing.type ?: "Property"

            val bedText = if (listing.beds != null) "${listing.beds} bed${if (listing.beds != 1) "s" else ""}" else ""
            val bathText = if (listing.baths != null) "${listing.baths} bath${if (listing.baths != 1) "s" else ""}" else ""
            tvBedsBaths.text = listOf(bedText, bathText).filter { it.isNotEmpty() }.joinToString(" · ")

            // Load listing image with Glide
            if (ivListingImage != null) {
                val imageUrl = listing.imageUrls?.split(",")?.firstOrNull()?.trim()
                if (!imageUrl.isNullOrBlank()) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .transform(CenterCrop(), RoundedCorners(24))
                        .placeholder(R.drawable.bg_listing_placeholder)
                        .error(R.drawable.bg_listing_placeholder)
                        .into(ivListingImage)
                    ivListingImage.visibility = View.VISIBLE
                } else {
                    ivListingImage.setImageResource(R.drawable.bg_listing_placeholder)
                }
            }

            itemView.setOnClickListener { onItemClick(listing) }
        }
    }
}
