package com.example.movietracker.main.fragments.profile

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.example.movietracker.FirebaseUserLiveData
import com.example.movietracker.R
import com.example.movietracker.authentication.LoginFragment
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.BaseServiceModel
import com.example.movietracker.main.entity.ChatMessage
import com.example.movietracker.main.entity.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    val firebaseService: FirebaseService
) : BaseServiceModel(firebaseService) {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    var firebaseDatabase =
        firebaseService.getDatabaseReference(firebaseService.getUsersTableConst())
    var currentUserId = firebaseService.getCurrentUser()?.uid

    private var hasConversations = false

    private val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun addUserChangeListener(view: ProfileFragment) {
        currentUserId?.let { userId ->
            firebaseDatabase.child(userId).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)

                    if (user == null) {
                        Log.e(ProfileFragment.TAG, "User data is null!")
                        return
                    } else {
                        Log.e(ProfileFragment.TAG, "User data has changed " + user.name)

                        view.onUserChanged(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ProfileFragment.TAG, "Failed to read user", error.toException())
                }
            })
        }

    }

    fun observeAuthenticationState(viewLifecycleOwner: LifecycleOwner, view: ProfileFragment) {
        authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    view.onUserAuthenticated()
                }
                else -> {
                    view.onUserUnauthenticated()
                }
            }
        })
    }

    fun hasConversations(menu: Menu) {
        currentUserId?.let { userId ->
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (ss in dataSnapshot.children) {
                                val message = ss.getValue(ChatMessage::class.java)
                                message?.let {
                                    if (message.senderId == userId || message.receiverId == userId) {
                                        hasConversations = true
                                        menu.findItem(R.id.chat_icon)?.isVisible = true
                                    }
                                }
                                if (hasConversations) break
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(LoginFragment.TAG, databaseError.message)
                    }
                })
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        verifyNewPassword: String,
        context: Context
    ) {
        val user = firebaseService.getCurrentUser()
        var isCurrentPasswordCorrect = false

        if (user != null) {
            val credential = user.email?.let { it1 ->
                firebaseService.getCredentials(
                    it1, currentPassword
                )
            }

            if (credential != null) {
                user.reauthenticate(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful)
                            isCurrentPasswordCorrect = true
                    }.continueWith {
                        if (isCurrentPasswordCorrect) {
                            when {
                                !Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.:;'])(?=\\S+$).{4,}$")
                                    .matcher(newPassword)
                                    .matches() -> {
                                    Toast.makeText(
                                        context,
                                        R.string.invalid_password,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                !TextUtils.equals(
                                    newPassword,
                                    verifyNewPassword
                                ) -> {
                                    Toast.makeText(
                                        context,
                                        R.string.passwords_not_matching,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    user.updatePassword(newPassword)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.password_changed_success,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                R.string.invalid_current_password,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

}