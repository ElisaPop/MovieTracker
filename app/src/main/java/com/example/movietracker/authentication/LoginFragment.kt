package com.example.movietracker.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.movietracker.BuildConfig
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.databinding.LoginFragmentBinding
import com.example.movietracker.main.entity.User
import com.example.movietracker.main.fragments.profile.ProfileFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(), AuthenticationInterface {

    companion object {
        const val TAG = "LoginFragment"
        const val BASE_IMAGE_URL = "person-icon.png"
        const val LAST_IMAGE = "last_image"
        const val LAST_EMAIL = "last_email"
        const val USER_ID = "user_id"
        const val DEFAULT_EMAIL = ""
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private var binding: LoginFragmentBinding? = null

    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.login_fragment, container, false
        )


        // Navigation to register
        binding?.let {
            it.loginToRegister.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            // Log in
            it.loginButton.setOnClickListener { emailLogIn() }

            // Configure Google Sign In
            it.googleLogin.setOnClickListener { googleSignIn() }

            // Autocomplete with the last email the user signed in with
            val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val emailToDisplay = sharedPref.getString(LAST_EMAIL, DEFAULT_EMAIL)
            val imageToDisplay = sharedPref.getString(LAST_IMAGE, BASE_IMAGE_URL)

            // Set log-in image
            displayImage(imageToDisplay)

            it.inputEmail.setText(emailToDisplay)

            it.inputEmail.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    if (it.inputEmail.text.toString() == emailToDisplay) {
                        displayImage(imageToDisplay)
                    } else {
                        displayImage(BASE_IMAGE_URL)
                    }
                }
            }
        }
        return binding?.root

    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    account.idToken?.let { firebaseAuthWithGoogle(it) }
                } else {
                    Log.w(TAG, "Google sign in failed")
                    Toast.makeText(
                        activity,
                        getString(R.string.google_sign_in_failed),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(
                    activity,
                    getString(R.string.google_sign_in_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun emailLogIn() {
        binding?.let {
            val inputEmail = it.inputEmail.text.toString().trim { it <= ' ' }
            val inputPassword = it.inputPassword.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(inputEmail) -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.invalid_email),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(inputPassword) -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.invalid_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            else -> {
                viewModel.authenticateUser(this, inputEmail, inputPassword)
            }
        }

    }
    }

    private fun googleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), googleSignInOptions)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_RESULT_CODE)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModel.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val currentUser = viewModel.getCurrentUser()

                    // Check if the user already exists before creating a new one
                    currentUser?.let {
                        viewModel.addGoogleUserListener(
                            currentUser,
                            object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        viewModel.createNewUser(currentUser)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.d(TAG, databaseError.message)
                                }
                            })
                    }
                    onUserCreatedSuccessfully()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    onError(getString(R.string.google_sign_in_failed))
                }
            }
    }


    private fun startMainActivity() {
        val intent = Intent(this.activity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun displayImage(imageToDisplay: String?) {
        binding?.let{
            imageToDisplay?.let { imageString ->
                val pathReference = viewModel.getPathReference(imageString)

                pathReference.downloadUrl.addOnSuccessListener { uri ->
                    val profilePictureUrl = uri.toString()

                    profilePictureUrl.let { url ->
                        Glide.with(it.loginIcon.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.ic_broken_image)
                            )
                            .into(it.loginIcon)
                    }
                }
            }
        }

    }

    private fun setPreferencesImage() {
        viewModel.createCurrentUserDataListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)

                if (user == null) {
                    Log.e(ProfileFragment.TAG, "User data is null!")
                    return
                } else {
                    val pathReference = user.imageUrl
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString(LAST_IMAGE, pathReference)
                        apply()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(ProfileFragment.TAG, "Failed to read user", error.toException())
            }
        })
    }

    override fun onUserCreatedSuccessfully() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val currentUser = viewModel.getCurrentUser()
        with(sharedPref.edit()) {
            putString(LAST_EMAIL, currentUser?.email)
            putString(USER_ID, currentUser?.uid)
            apply()
        }

        // get image from database for the next log-in screen
        setPreferencesImage()

        Toast.makeText(
            activity,
            getString(R.string.login_success),
            Toast.LENGTH_SHORT
        ).show()

        startMainActivity()

    }

    override fun onError(errorString: String) {
        Toast.makeText(
            activity,
            errorString,
            Toast.LENGTH_SHORT
        ).show()
    }

}