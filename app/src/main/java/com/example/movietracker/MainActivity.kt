package com.example.movietracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.movietracker.authentication.AuthenticationActivity
import com.example.movietracker.authentication.LoginFragment
import com.example.movietracker.firebase.FirebaseService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (FirebaseService().getCurrentUser() == null) {
            val authenticate = Intent(this, AuthenticationActivity::class.java)
            startActivity(authenticate)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.moviesFragment,
                R.id.topRatedFragment,
                R.id.bookmarkFragment,
                R.id.profileFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        val sharedPref: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)

        val userId = FirebaseService().getCurrentUser()?.uid ?: sharedPref.getString(
            LoginFragment.USER_ID,
            LoginFragment.DEFAULT_EMAIL
        )


        Firebase.database.setPersistenceEnabled(true)
        FirebaseService().getDatabaseReference("movies").keepSynced(true)

        userId?.let {
            FirebaseService().getDatabaseReference("users").child(it).keepSynced(true)
        }
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            if (destination.id == R.id.moviesFragment
                || destination.id == R.id.topRatedFragment
                || destination.id == R.id.bookmarkFragment
                || destination.id == R.id.profileFragment
            ) {
                bottomNavigationView.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}