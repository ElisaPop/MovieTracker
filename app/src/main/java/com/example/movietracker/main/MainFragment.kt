package com.example.movietracker.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.movietracker.R
import com.example.movietracker.authentication.AuthenticationActivity
import com.example.movietracker.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        const val LAST_EMAIL = "last_email"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                MainViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.statusText.text = "AUTHENTICATED"

                    binding.authButton.text = getString(R.string.logout_btn)
                    binding.authButton.setOnClickListener {
                        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString(
                                LAST_EMAIL,
                                FirebaseAuth.getInstance().currentUser?.email
                            )
                            apply()
                        }

                        AuthUI.getInstance().signOut(requireContext())
                        startLoginActivity()
                    }
                }
                else -> {
                    binding.statusText.text = "UNAUTHENTICATED"

                    binding.authButton.text = getString(R.string.log_in)
                    binding.authButton.setOnClickListener {
                        startLoginActivity()
                    }
                }
            }
        })
    }

    private fun startLoginActivity() {
        val intent = Intent(this.activity, AuthenticationActivity::class.java)
        startActivity(intent)
    }

}