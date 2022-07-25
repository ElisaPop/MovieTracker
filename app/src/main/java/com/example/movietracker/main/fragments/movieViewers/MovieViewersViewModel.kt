package com.example.movietracker.main.fragments.movieViewers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.BaseServiceModel
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.entity.User
import com.example.movietracker.main.entity.UserItemData
import com.example.movietracker.main.fragments.profile.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MovieViewersViewModel @Inject constructor(
    val firebaseService: FirebaseService
) : BaseServiceModel(firebaseService) {
    private val _selectedMovie = MutableLiveData<MovieItem>()

    val selectedMovie: LiveData<MovieItem>
        get() = _selectedMovie

    private var _userItemList = MutableLiveData<ArrayList<UserItemData>?>()
    val userItemList: LiveData<ArrayList<UserItemData>?>
        get() = _userItemList

    fun setSelectedMovie(movieDetails: MovieItem) {
        _selectedMovie.value = movieDetails
    }

    fun addMovieViewersListListener(movieId: String) {
        val usersList: ArrayList<UserItemData> = ArrayList<UserItemData>()

        firebaseService.createUsersDataListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null) {
                        user.bookmark?.let { bookmark ->
                            if (bookmark.contains(movieId)) {
                                val movieDates = bookmark.getValue(movieId)
                                usersList.add(
                                    UserItemData(
                                        user.id,
                                        user.name,
                                        user.location,
                                        user.imageUrl,
                                        movieDates.date,
                                        firebaseService.getPathReference(user.imageUrl)
                                    )
                                )
                            }
                        }
                    }
                }
                _userItemList.value = usersList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    ProfileFragment.TAG,
                    "Failed to read user's bookmark list",
                    error.toException()
                )
            }
        })
    }
}