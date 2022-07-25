package com.example.movietracker.main.fragments.bookmark

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.movietracker.R
import com.example.movietracker.databinding.BookmarkFragmentBinding
import com.example.movietracker.main.adapters.MovieAdapter
import com.example.movietracker.main.adapters.MovieBookmarkAdapter
import com.firebase.ui.auth.IdpResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : Fragment() {

    companion object {
        const val TAG = "BookmarkFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<BookmarkViewModel>()
    private var binding: BookmarkFragmentBinding? = null
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bookmark_fragment, container, false)

        binding?.let {
            it.lifecycleOwner = this.viewLifecycleOwner
            it.viewModel = viewModel

            viewModel.addMoviesListener(this)

            val adapter = MovieBookmarkAdapter(MovieAdapter.OnClickListener { res ->
                viewModel.displayMovieDetails(res)
            }, viewModel)
            it.movieList.adapter = adapter

            viewModel.movieItem.observe(viewLifecycleOwner, Observer { res ->
                if (res != null) {
                    this.findNavController()
                        .navigate(
                            BookmarkFragmentDirections.actionBookmarkFragmentToMovieDetailsFragment(
                                res
                            )
                        )
                    viewModel.displayMovieDetailsComplete()
                }
            })

            viewModel.movieBookmarkList.observe(viewLifecycleOwner, Observer { res ->
                if (res != null) {
                    adapter.submitList(viewModel.movieBookmarkList.value)
                    onPopulatedList()
                } else {
                    onEmptyList()
                }
            })
        }
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${viewModel.getCurrentUser()?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }

    }

    fun onEmptyList() {
        binding?.let {
            it.statusText.visibility = View.VISIBLE
            it.movieList.visibility = View.GONE
        }
    }

    fun onPopulatedList() {
        binding?.let {
            it.movieList.visibility = View.VISIBLE
            it.statusText.visibility = View.GONE
        }
    }
}