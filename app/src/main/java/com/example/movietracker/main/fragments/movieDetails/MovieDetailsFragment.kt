package com.example.movietracker.main.fragments.movieDetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movietracker.R
import com.example.movietracker.databinding.MovieDetailsFragmentBinding
import com.example.movietracker.main.entity.MovieItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {

    private val viewModel by viewModels<MovieDetailsViewModel>()
    private var binding: MovieDetailsFragmentBinding? = null

    var isBookmark = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MovieDetailsFragmentBinding.inflate(inflater)

        val movieDetails = MovieDetailsFragmentArgs.fromBundle(requireArguments()).selectedMovie
        viewModel.setSelectedMovie(movieDetails)

        binding?.let {
            it.lifecycleOwner = this
            it.viewModel = viewModel

            it.votesNumber.text = (movieDetails.vote_count.toInt()).toString()
            it.whoViewedButton.visibility = View.INVISIBLE
            viewModel.addPeopleViewThisMovieListener(
                movieDetails.id.toString(),
                it.whoViewedButton
            )
            it.whoViewedButton.setOnClickListener {
                onViewButtonClick(movieDetails)
            }

            val currentUser = viewModel.getCurrentUser()
            if (currentUser != null) {
                viewModel.isBookmarked(movieDetails, currentUser.uid, object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isBookmark = snapshot.exists()
                        setBookmarkDrawable(isBookmark)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

                it.bookmarkButton.visibility = View.VISIBLE

                it.bookmarkButton.setOnClickListener {
                    when {
                        isBookmark -> {
                            viewModel.removeBookmark(movieDetails, currentUser.uid)
                        }
                        else -> {
                            viewModel.addBookmark(movieDetails, currentUser.uid)
                        }
                    }

                    isBookmark = !isBookmark
                    setBookmarkDrawable(isBookmark)
                }
            } else {
                it.bookmarkButton.visibility = View.GONE
            }
        }

        setHasOptionsMenu(true)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        if (getShareIntent().resolveActivity(requireActivity().packageManager) == null) {
            menu.findItem(R.id.share_icon)?.isVisible = false
        }

        menu.findItem(R.id.share_icon).setOnMenuItemClickListener {
            startActivity(getShareIntent())
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getShareIntent(): Intent {
        val movieDetails =
            MovieDetailsFragmentArgs.fromBundle(requireArguments()).selectedMovie
        return ShareCompat.IntentBuilder.from(requireActivity())
            .setText(
                getString(
                    R.string.share_movie_text,
                    movieDetails.title,
                    movieDetails.overview
                )
            )
            .setType("text/plain")
            .intent
    }

    private fun setBookmarkDrawable(isBookmark: Boolean) {
        binding?.let {
            when {
                isBookmark -> {
                    it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_24)
                }
                else -> {
                    it.bookmarkButton.setBackgroundResource(R.drawable.ic_baseline_bookmark_border_24)
                }
            }
        }
    }

    private fun onViewButtonClick(movieDetails: MovieItem) {
        this.findNavController().navigate(
            MovieDetailsFragmentDirections.actionMovieDetailsFragmentToMovieViewersFragment(
                movieDetails
            )
        )
    }
}
