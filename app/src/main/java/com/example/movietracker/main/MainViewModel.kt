package com.example.movietracker.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.movietracker.FirebaseUserLiveData

class MainViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

}
