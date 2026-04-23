package com.boycoder.todo

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

/**
 * Extension function to load an avatar image from a URL into an ImageView.
 * Encapsulates the Glide usage and placeholder logic.
 */
fun ImageView.loadAvatar(url: String?) {
    Glide.with(this)
        .load(url)
        .transform(CircleCrop())
        .placeholder(R.drawable.avatar_placeholder)
        .into(this)
}
