package com.example.movietracker.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.movietracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }
}