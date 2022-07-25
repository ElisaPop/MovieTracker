package com.example.movietracker.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.movietracker.databinding.MovieViewItemBinding
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.fragments.bookmark.BookmarkViewModel

class MovieBookmarkAdapter(
    private val onClickListener: MovieAdapter.OnClickListener,
    val viewModel: MovieListViewModel
) :
    ListAdapter<MovieItem, MovieViewHolder>(MovieAdapter.DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {

        if (viewModel is BookmarkViewModel) {
            return BookmarkViewHolder(
                MovieViewItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                ), viewModel
            )
        } else {
            return MovieViewHolder(
                MovieViewItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                ), viewModel
            )
        }
    }


    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movieData = getItem(position)
        if (movieData != null) {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(movieData)
            }
            holder.bind(movieData)

            if (viewModel is BookmarkViewModel) {
                holder.binding.bookmarkButton.setOnClickListener {
                    viewModel.removeBookmark(movieData)
                    // This method should be avoided but notifyItemRemoved(position)
                    // doesn't re-index the positions
                    notifyDataSetChanged()
                }
            }

        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }
}