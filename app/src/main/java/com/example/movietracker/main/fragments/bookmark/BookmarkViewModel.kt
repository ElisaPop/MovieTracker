package com.example.movietracker.main.fragments.bookmark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.fragments.profile.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    val firebaseService: FirebaseService
) : MovieListViewModel(firebaseService) {

    companion object {
        const val USER_ID = "user_id"
        const val DEFAULT_EMAIL = ""
    }

    private var currentUserId = firebaseService.getCurrentUser()?.uid

    private var _movieBookmarkList = MutableLiveData<ArrayList<MovieItem>?>()
    val movieBookmarkList: LiveData<ArrayList<MovieItem>?>
        get() = _movieBookmarkList

    fun addMoviesListener(view: BookmarkFragment) {

        val userId = currentUserId ?: view.sharedPref.getString(
            USER_ID,
            DEFAULT_EMAIL
        )

        userId?.let { userId ->
            val firebaseMovieDatabase =
                firebaseService.getDatabaseReference(firebaseService.getMoviesTableConst())
            val firebaseFavoriteDatabase =
                firebaseService.getDatabaseReference(firebaseService.getUsersTableConst())
                    .child(userId).child("bookmark")

            val moviesIdList: ArrayList<String> = ArrayList<String>()
            firebaseFavoriteDatabase.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (bookmarks in snapshot.children) {
                        val movieBookmark = bookmarks.key
                        movieBookmark?.let { bookmark ->
                            if (!moviesIdList.contains(bookmark))
                                moviesIdList.add(bookmark)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        ProfileFragment.TAG,
                        "Failed to read user's bookmark list",
                        error.toException()
                    )
                }
            })

            val moviesList: ArrayList<MovieItem> = ArrayList<MovieItem>()
            firebaseMovieDatabase.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (movies in snapshot.children) {
                        val movie = movies.getValue(MovieItem::class.java)
                        movie?.let { bookmark ->
                            if (moviesIdList.contains(movie.id.toString())) {
                                moviesList.add(bookmark)
                                _movieBookmarkList.value = moviesList
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        ProfileFragment.TAG,
                        "Failed to read user's bookmark list",
                        error.toException()
                    )
                }
            })

            for (id in moviesIdList) {
                firebaseMovieDatabase.child(id).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val movie = snapshot.getValue(MovieItem::class.java)
                        movie?.let { bookmark ->
                            moviesList.add(bookmark)
                            _movieBookmarkList.value = moviesList
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            ProfileFragment.TAG,
                            "Failed to read user's bookmark list",
                            error.toException()
                        )
                    }
                }
                )
            }
            if (moviesList.isEmpty()) view.onEmptyList()
            else view.onPopulatedList()
        }
    }

    override fun removeBookmark(movieItem: MovieItem) {
        firebaseService.removeBookmark(movieItem, firebaseService.getCurrentUser()?.uid ?: "")
        _movieBookmarkList.value?.remove(movieItem)
    }
}