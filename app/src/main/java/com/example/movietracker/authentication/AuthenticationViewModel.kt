package com.example.movietracker.authentication

import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.BaseServiceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    val firebaseService: FirebaseService
) : BaseServiceModel(firebaseService)