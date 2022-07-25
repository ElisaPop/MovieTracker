package com.example.movietracker.main.fragments.movieViewers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movietracker.R
import com.example.movietracker.databinding.MovieViewersFragmentBinding
import com.example.movietracker.main.adapters.UserAdapter
import com.example.movietracker.main.entity.ChatUsers
import com.example.movietracker.main.entity.MovieItem
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieViewersFragment : Fragment() {

    private val viewModel by viewModels<MovieViewersViewModel>()
    private var binding: MovieViewersFragmentBinding? = null
    lateinit var movieDetails: MovieItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MovieViewersFragmentBinding.inflate(inflater)

        movieDetails = MovieViewersFragmentArgs.fromBundle(requireArguments()).movie
        viewModel.setSelectedMovie(movieDetails)

        binding?.let {
            it.lifecycleOwner = this

            it.viewModel = viewModel

            val adapter = UserAdapter(viewModel.getCurrentUser()?.uid.toString(), ::onClickChat)
            it.viewersList.adapter = adapter

            viewModel.addMovieViewersListListener(movieDetails.id.toString())

            viewModel.userItemList.observe(viewLifecycleOwner, { userItems ->
                if (userItems != null) {
                    adapter.submitList(userItems)
                }
            })
        }
        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_menu, menu)
        menu.findItem(R.id.map_icon).setOnMenuItemClickListener {
            val status =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
            if (status == ConnectionResult.SUCCESS) {
                this.findNavController()
                    .navigate(
                        MovieViewersFragmentDirections.actionMovieViewersFragmentToMapsFragment(
                            movieDetails
                        )
                    )
            } else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                Toast.makeText(
                    context, "Map unavailable. Please update your google play services.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun onClickChat(chatUsers: ChatUsers) {
        if (!chatUsers.targetId.isNullOrEmpty() && !chatUsers.currentId.isNullOrEmpty()) {
            this.findNavController().navigate(
                MovieViewersFragmentDirections.actionMovieViewersFragmentToChatFragment(
                    chatUsers.targetId,
                    chatUsers.currentId
                )
            )
        }
    }

}