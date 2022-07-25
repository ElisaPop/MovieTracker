package com.example.movietracker.main.fragments.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movietracker.R
import com.example.movietracker.databinding.MoviesFragmentBinding
import com.example.movietracker.main.adapters.MovieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoviesFragment : Fragment() {

    private val viewModel by viewModels<MoviesViewModel>()
    private var binding: MoviesFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = MoviesFragmentBinding.inflate(inflater)

        binding?.let {
            it.lifecycleOwner = this.viewLifecycleOwner
            it.viewModel = viewModel

            val pagingAdapter = MovieAdapter(MovieAdapter.OnClickListener {
                viewModel.displayMovieDetails(it)
            }, viewModel)

            it.movieList.adapter = pagingAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.queryMovies.asFlow().collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
            }
        }

        // Observe the navigateToSelectedProperty LiveData and Navigate when it isn't null
        // After navigating, call displayPropertyDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        viewModel.movieItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(MoviesFragmentDirections.actionShowDetail(it))
                viewModel.displayMovieDetailsComplete()
            }
        })

        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val search = menu.findItem(R.id.search_bar)
        val searchView = search.actionView as SearchView
        searchView.queryHint = getString(R.string.query_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    viewModel.searchMovies(newText)
                }
                return true
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
    }

}