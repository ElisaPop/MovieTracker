package com.example.movietracker.authentication

interface AuthenticationInterface {
    fun onUserCreatedSuccessfully()

    fun onError(errorString: String)
}