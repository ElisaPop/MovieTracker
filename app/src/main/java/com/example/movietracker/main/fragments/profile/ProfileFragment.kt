package com.example.movietracker.main.fragments.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.movietracker.R
import com.example.movietracker.authentication.AuthenticationActivity
import com.example.movietracker.databinding.ProfileFragmentBinding
import com.example.movietracker.main.entity.User
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    companion object {
        const val TAG = "ProfileFragment"
        const val LAST_IMAGE = "last_image"
        const val LAST_EMAIL = "last_email"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<ProfileViewModel>()
    private var binding: ProfileFragmentBinding? = null

    private lateinit var profilePictureUrl: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false)

        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeAuthenticationState(viewLifecycleOwner, this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${viewModel.getCurrentUser()?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        menu.findItem(R.id.chat_icon).setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_conversationFragment)
            true
        }
        viewModel.hasConversations(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun onUserChanged(user: User) {
        binding?.let {
            it.name.text = user.name
            it.email.text = user.email

            val status =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
            if (status == ConnectionResult.SUCCESS) {
                val addresses: List<Address>
                val geocoder = Geocoder(requireContext(), Locale.getDefault())

            addresses = user.location?.let { location ->
                location.latitude?.let { lat ->
                    location.longitude?.let { long ->
                        geocoder.getFromLocation(
                            lat,
                            long,
                            1
                        )
                    }
                }
            } as List<Address>

            val address = if (addresses.isEmpty()) {
                getString(R.string.could_not_fetch_address)
            } else {
                addresses[0].getAddressLine(0)
            }

            it.location.text = address

            } else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                it.location.text = getString(R.string.location_unavailable)
                Toast.makeText(
                    context,
                    "Location unavailable. Please update your google play services.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            profilePictureUrl = user.imageUrl

            val pathReference = viewModel.getPathReference(profilePictureUrl)
            pathReference.downloadUrl.addOnSuccessListener { uri ->
                val pictureUrl = uri.toString()

                pictureUrl.let { url ->
                    Glide.with(it.profileImage.context)
                        .load(url)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.ic_broken_image)
                        ).listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Toast.makeText(
                                    activity,
                                    getString(R.string.failed_upload),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                it.progressBar.visibility = View.GONE
                                it.loggedInLayout.visibility = View.VISIBLE
                                return false
                            }
                        }).into(it.profileImage)
                }
            }
        }

    }

    fun onUserAuthenticated() {
        binding?.let {
            it.loggedOutLayout.visibility = View.GONE
            it.progressBar.visibility = View.VISIBLE

            it.logoutButton.setOnClickListener { onLogoutClick() }

            it.changeImageButton.setOnClickListener {
                onChangeImageClick()
            }
            it.changePasswordButton.setOnClickListener {
                onChangePasswordClick()
            }

            viewModel.addUserChangeListener(this)
        }
    }

    fun onUserUnauthenticated() {
        binding?.let {
            it.loggedInLayout.visibility = View.GONE
            it.loggedOutLayout.visibility = View.VISIBLE
            it.progressBar.visibility = View.GONE
            it.loginButton.setOnClickListener { startLoginActivity() }
        }
    }

    private fun onChangeImageClick() {
        viewModel.currentUserId?.let {
            this.findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToChangeImageFragment(
                    profilePictureUrl,
                    it
                )
            )
        }
    }

    private fun onChangePasswordClick() {
        findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
    }

    private fun startLoginActivity() {
        val intent = Intent(this.activity, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    private fun onLogoutClick() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)

        with(sharedPref.edit()) {
            putString(
                LAST_EMAIL,
                viewModel.getCurrentUser()?.email
            )
            putString(LAST_IMAGE, profilePictureUrl)
            apply()
        }

        AuthUI.getInstance().signOut(requireContext())
        startLoginActivity()
    }


}