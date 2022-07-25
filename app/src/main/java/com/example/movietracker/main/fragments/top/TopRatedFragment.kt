package com.example.movietracker.main.fragments.top

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movietracker.databinding.TopRatedFragmentBinding
import com.example.movietracker.main.adapters.MovieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopRatedFragment : Fragment() {

    private val viewModel by viewModels<TopRatedViewModel>()
    private var binding: TopRatedFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = TopRatedFragmentBinding.inflate(inflater)

        binding?.let {
            it.lifecycleOwner = this.viewLifecycleOwner
            it.viewModel = viewModel

            val pagingAdapter = MovieAdapter(MovieAdapter.OnClickListener {
                viewModel.displayMovieDetails(it)
            }, viewModel)

            it.movieList.adapter = pagingAdapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.topMovies.asFlow().collectLatest { pagingData ->
                    pagingAdapter.submitData(pagingData)
                }
            }
        }


        viewModel.movieItem.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController()
                    .navigate(TopRatedFragmentDirections.actionTopShowDetail(it))
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

}