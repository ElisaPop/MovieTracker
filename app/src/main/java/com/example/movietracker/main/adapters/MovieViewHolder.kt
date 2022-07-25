package com.example.movietracker.main.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.R
import com.example.movietracker.databinding.MovieViewItemBinding
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.entity.MovieItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

open class MovieViewHolder(
    internal open var binding: MovieViewItemBinding,
    open var viewModel: MovieListViewModel
) :
    RecyclerView.ViewHolder(binding.root) {

    var isBookmark = false

    open fun bind(movieItem: MovieItem) {
        binding.movie = movieItem

        viewModel.isBookmarked(movieItem, object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isBookmark = snapshot.exists()
                setBookmarkDrawable(isBookmark)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.bookmarkButton.visibility = View.VISIBLE

        binding.bookmarkButton.setOnClickListener {
            onClickBookmark(movieItem)
        }


        binding.executePendingBindings()
    }

    open fun setBookmarkDrawable(isBookmark: Boolean) {
        binding.let {
            if (isBookmark) {
                it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
            } else {
                it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
            }
        }
    }

    open fun onClickBookmark(movieItem: MovieItem) {
        if (isBookmark) {
            viewModel.removeBookmark(movieItem)

        } else {
            viewModel.addBookmark(movieItem)
        }
        isBookmark = !isBookmark
        setBookmarkDrawable(isBookmark)
    }

}