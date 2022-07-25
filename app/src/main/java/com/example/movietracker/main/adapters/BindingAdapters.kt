package com.example.movietracker.main.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.movietracker.R
import com.example.movietracker.main.entity.ChatItem
import com.example.movietracker.main.entity.ChatMessage
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.entity.UserItemData
import com.google.firebase.storage.StorageReference

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imageUrl: String?) {
    imageUrl.let {
        Glide.with(imgView.context)
            .load("https://image.tmdb.org/t/p/$imageUrl")
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
            )
            .into(imgView)
    }
}

@BindingAdapter("firebaseStorageImageUrl")
fun bindFirebaseImage(imgView: ImageView, pathReference: StorageReference) {
    if (pathReference != null) {
        pathReference.downloadUrl.addOnSuccessListener { uri ->

            Glide.with(imgView.context)
                .asBitmap()
                .load(uri.toString())
                .apply(RequestOptions.circleCropTransform())
                .into(imgView)
        }
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<MovieItem>?) {
    val adapter = recyclerView.adapter as MovieBookmarkAdapter
    adapter.submitList(data)
}

@BindingAdapter("userListData")
fun bindUserRecyclerView(recyclerView: RecyclerView, data: List<UserItemData>?) {
    val adapter = recyclerView.adapter as UserAdapter
    adapter.submitList(data)
}

@BindingAdapter("chatData")
fun bindChatRecyclerView(recyclerView: RecyclerView, data: List<ChatMessage>?) {
    val adapter = recyclerView.adapter as MessageAdapter
    adapter.submitList(data)
}

@BindingAdapter("chatListData")
fun bindChatItemsRecyclerView(recyclerView: RecyclerView, data: List<ChatItem>?) {
    val adapter = recyclerView.adapter as ChatItemAdapter
    adapter.submitList(data)
}
