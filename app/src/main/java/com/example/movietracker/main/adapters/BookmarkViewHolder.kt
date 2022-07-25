package com.example.movietracker.main.adapters

import android.view.View
import com.example.movietracker.databinding.MovieViewItemBinding
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.entity.MovieItem

class BookmarkViewHolder(
    override var binding: MovieViewItemBinding,
    override var viewModel: MovieListViewModel
) : MovieViewHolder(binding, viewModel) {

    override fun bind(movieItem: MovieItem) {
        binding.movie = movieItem
        setBookmarkDrawable(true)
        binding.bookmarkButton.visibility = View.VISIBLE

        binding.executePendingBindings()
    }
}