package com.example.movietracker.main.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.movietracker.R
import com.example.movietracker.databinding.CustomInfoWindowBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    private var binding: CustomInfoWindowBinding =
        CustomInfoWindowBinding.inflate(LayoutInflater.from(context))

    private fun renderWindowText(marker: Marker) {
        binding.userName.text = marker.title

        if (binding.userName.text != context.getString(R.string.your_set_location)) {

            if (marker.snippet != null) {
                val result = marker.snippet.split("%").toTypedArray()
                val bookmarkDate = result[2]
                val imageUrl = result[3]

                binding.dateViewed.visibility = View.VISIBLE
                binding.dateViewed.text = context.getString(R.string.marker_date_text, bookmarkDate)

                Glide.with(binding.profilePictureMap.context)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(object : RequestListener<Drawable> {

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {

                            binding.profilePictureMap.visibility = View.VISIBLE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.profilePictureMap.visibility = View.GONE
                            return false
                        }
                    }).into(binding.profilePictureMap)

                binding.chatButton.visibility = View.VISIBLE
            }

        } else {
            binding.chatButton.visibility = View.GONE
            binding.profilePictureMap.visibility = View.GONE
            binding.dateViewed.visibility = View.GONE
        }
    }

    override fun getInfoWindow(p0: Marker?): View {
        if (p0 != null) {
            renderWindowText(p0)
        }
        return this.binding.root
    }

    override fun getInfoContents(p0: Marker?): View {
        if (p0 != null) {
            renderWindowText(p0)
        }
        return this.binding.root
    }
}