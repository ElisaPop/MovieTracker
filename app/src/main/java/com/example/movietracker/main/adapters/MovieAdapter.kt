package com.example.movietracker.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.R
import com.example.movietracker.databinding.MovieViewItemBinding
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.entity.MovieItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MovieAdapter(
    private val onClickListener: OnClickListener,
    val viewModel: MovieListViewModel
) :
    PagingDataAdapter<MovieItem, MovieAdapter.MovieDataViewHolder>(DiffCallback) {

    class MovieDataViewHolder(
        private var binding: MovieViewItemBinding,
        private val viewModel: MovieListViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {

        var isBookmark = false

        fun bind(movieItem: MovieItem) {
            binding.movie = movieItem
            // It forces the data binding to execute immediately, which allows the RecyclerView to
            // make the correct view size measurements

            val isLoggedIn = viewModel.isBookmarked(movieItem, object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isBookmark = snapshot.exists()
                    setBookmarkDrawable(isBookmark)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        binding.root.context,
                        "Failed to bookmark movie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

            if (isLoggedIn) {
                binding.bookmarkButton.visibility = View.VISIBLE
                binding.bookmarkButton.setOnClickListener {
                    onClickBookmark(movieItem)
                }
            } else {
                binding.bookmarkButton.visibility = View.GONE
            }

            binding.executePendingBindings()
        }

        private fun setBookmarkDrawable(isBookmark: Boolean) {
            binding.let {
                if (isBookmark) {
                    it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
                } else {
                    it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
                }
            }
        }

        private fun onClickBookmark(movieItem: MovieItem) {
            if (isBookmark) {
                viewModel.removeBookmark(movieItem)
            } else {
                viewModel.addBookmark(movieItem)
            }
            isBookmark = !isBookmark
            setBookmarkDrawable(isBookmark)
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<MovieItem>() {
        override fun areItemsTheSame(oldItem: MovieItem, newItem: MovieItem): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: MovieItem, newItem: MovieItem): Boolean {
            return oldItem.title == newItem.title
                    || oldItem.poster_path == newItem.poster_path
                    || oldItem.release_date == newItem.release_date
                    || oldItem.vote_average.toString() == newItem.vote_average.toString()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieDataViewHolder {
        return MovieDataViewHolder(
            MovieViewItemBinding.inflate(LayoutInflater.from(parent.context)),
            this.viewModel
        )
    }

    override fun onBindViewHolder(holder: MovieDataViewHolder, position: Int) {
        val movieData = getItem(position)
        if (movieData != null) {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(movieData)
            }
            holder.bind(movieData)
        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }

    class OnClickListener(val clickListener: (movieItem: MovieItem) -> Unit) {
        fun onClick(movieItem: MovieItem) = clickListener(movieItem)
    }
}