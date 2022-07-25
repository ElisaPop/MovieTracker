package com.example.movietracker.main

import android.widget.Button
import androidx.lifecycle.ViewModel
import com.example.movietracker.authentication.AuthenticationInterface
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.entity.MovieItem
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

open class BaseServiceModel(private val firebaseService: FirebaseService) : ViewModel() {

    fun getAuth(): FirebaseAuth {
        return firebaseService.firebaseAuth
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseService.getCurrentUser()
    }

    fun authenticateUser(
        loginFragment: AuthenticationInterface,
        email: String,
        password: String
    ) {
        firebaseService.authenticateUser(loginFragment, email, password)
    }

    fun addGoogleUserListener(user: FirebaseUser, eventListener: ValueEventListener) {
        firebaseService.addGoogleUserListener(user, eventListener)
    }

    fun addUserListener(id: String, eventListener: ValueEventListener) {
        firebaseService.addUserListener(id, eventListener)
    }

    fun createNewUser(user: FirebaseUser) {
        firebaseService.createNewUser(user)
    }

    fun createNewEmailUser(name: String, email: String) {
        firebaseService.createNewEmailUser(name, email)
    }

    fun createCurrentUserDataListener(listener: ValueEventListener) {
        firebaseService.createCurrentUserDataListener(listener)
    }

    fun changeUserField(userId: String, field: String, value: String) {
        firebaseService.changeUserField(userId, field, value)
    }

    fun uploadImage(photoFile: File): UploadTask {
        return firebaseService.uploadImage(photoFile)
    }

    fun getPathReference(imageUrl: String): StorageReference {
        return firebaseService.getPathReference(imageUrl)
    }

    fun addBookmark(movieDetails: MovieItem, userId: String) {
        firebaseService.addBookmark(movieDetails, userId)
    }

    fun removeBookmark(movieDetails: MovieItem, userId: String) {
        firebaseService.removeBookmark(movieDetails, userId)
    }

    fun isBookmarked(
        movieDetails: MovieItem,
        userId: String,
        valueEventListener: ValueEventListener
    ) {
        firebaseService.isBookmarked(movieDetails, userId, valueEventListener)
    }

    fun addPeopleViewThisMovieListener(movieId: String, button: Button) {
        firebaseService.addPeopleViewThisMovieListener(movieId, button)
    }

    fun addLocation(location: LatLng) {
        firebaseService.addLocation(location)
    }
}