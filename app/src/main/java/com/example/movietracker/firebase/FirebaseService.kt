package com.example.movietracker.firebase

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.movietracker.authentication.AuthenticationInterface
import com.example.movietracker.main.entity.BookmarkMovie
import com.example.movietracker.main.entity.CustomLatLng
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.entity.User
import com.example.movietracker.main.fragments.profile.ProfileFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class FirebaseService {

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var firebaseDatabaseInstance = FirebaseDatabase.getInstance(FIREBASE_DB)
    var firebaseStorageInstance = FirebaseStorage.getInstance()

    companion object {
        const val TAG = "FirebaseService"
        private const val FIREBASE_DB = BuildConfig.FIREBASE_DB
        const val CHATS_TABLE = "chats"
        const val MOVIES_TABLE = "movies"
        const val USERS_TABLE = "users"
    }

    fun getChatsTableConst(): String {
        return CHATS_TABLE
    }

    fun getMoviesTableConst(): String {
        return MOVIES_TABLE
    }

    fun getUsersTableConst(): String {
        return USERS_TABLE
    }

    fun getDatabaseReference(referencePath: String): DatabaseReference {
        return firebaseDatabaseInstance.getReference(referencePath)
    }

    fun getStorageReference(): StorageReference {
        return firebaseStorageInstance.reference
    }

    fun getStorageFileReference(file: String): StorageReference {
        return firebaseStorageInstance.getReference(file)
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun authenticateUser(
        loginFragment: AuthenticationInterface,
        email: String,
        password: String
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginFragment.onUserCreatedSuccessfully()
                } else {
                    loginFragment.onError(task.exception?.message.toString())
                }
            }
    }


    // Checks if the user already exists and it it doesn't create a new user
    // in the database
    fun addGoogleUserListener(user: FirebaseUser, eventListener: ValueEventListener) {
        val userId = user.uid
        val firebaseDatabase = getDatabaseReference(USERS_TABLE)
        firebaseDatabase.child(userId).addValueEventListener(eventListener)
    }

    fun addUserListener(id: String, eventListener: ValueEventListener) {
        val firebaseDatabase = getDatabaseReference(USERS_TABLE)
        firebaseDatabase.child(id).addValueEventListener(eventListener)
    }

    fun createNewUser(user: FirebaseUser) {
        val userId = user.uid
        val userName = user.displayName.toString()
        val userEmail = user.email.toString()
        val user = User(userId, userName, userEmail, null)
        getDatabaseReference(USERS_TABLE).child(userId).setValue(user)
    }

    // Creates a new email user in the database
    fun createNewEmailUser(name: String, email: String) {
        val firebaseDatabase = getDatabaseReference(USERS_TABLE)
        val userId = getCurrentUser()?.uid

        if (userId != null) {
            val user = User(userId, name, email, null)
            firebaseDatabase.child(userId).setValue(user)
        }
    }

    // Create a listener on user data
    fun createCurrentUserDataListener(listener: ValueEventListener) {
        val firebaseDatabase = getDatabaseReference(USERS_TABLE)

        val currentUserId = getCurrentUser()?.uid
        currentUserId?.let { userId ->
            firebaseDatabase.child(userId).addValueEventListener(listener)
        }
    }

    fun createUsersDataListener(listener: ValueEventListener) {
        val firebaseDatabase = getDatabaseReference(USERS_TABLE)
        firebaseDatabase.addValueEventListener(listener)
    }

    fun changeUserField(userId: String, field: String, value: String) {
        getDatabaseReference(USERS_TABLE).child(userId).child(field).setValue(value)
    }

    fun uploadImage(photoFile: File): UploadTask {
        val firebaseRef = getStorageFileReference(photoFile.name.toString())
        return firebaseRef.putFile(Uri.fromFile(photoFile))
    }

    fun getPathReference(imageUrl: String): StorageReference {
        return getStorageReference().child(imageUrl)
    }

    fun addBookmark(movieDetails: MovieItem, userId: String) {

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = simpleDateFormat.format(System.currentTimeMillis())
        val timestamp = String.format("Date: %s", dateString)
        val movieId = movieDetails.id.toString()

        val usersReference = getDatabaseReference(USERS_TABLE)
        usersReference.child(userId).child("bookmark").child(movieId)
            .setValue(BookmarkMovie(movieDetails.id, timestamp))

        val moviesReference = getDatabaseReference(MOVIES_TABLE)
        moviesReference.child(movieId).setValue(movieDetails)
    }

    fun removeBookmark(movieDetails: MovieItem, userId: String) {
        val usersReference = getDatabaseReference(USERS_TABLE)
        usersReference.child(userId).child("bookmark").child(movieDetails.id.toString())
            .removeValue().addOnSuccessListener {
                Log.d(TAG, "removeBookmark: Removed from bookmark")
            }
            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "removeBookmark: failed to remove from bookmark due to ${e.message}"
                )
            }
    }

    fun isBookmarked(
        movieDetails: MovieItem,
        userId: String,
        valueEventListener: ValueEventListener
    ) {
        val usersReference = getDatabaseReference(USERS_TABLE)
        usersReference.child(userId).child("bookmark").child(movieDetails.id.toString())
            .addListenerForSingleValueEvent(valueEventListener)
    }

    fun addPeopleViewThisMovieListener(movieId: String, button: Button) {

        val firebaseUsersDatabase =
            this.getDatabaseReference(USERS_TABLE)

        firebaseUsersDatabase.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null) {
                        user.bookmark?.let { bookmark ->
                            if (bookmark.contains(movieId))
                                button.visibility = View.VISIBLE
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    ProfileFragment.TAG,
                    "Failed to read user's bookmark list",
                    error.toException()
                )
            }
        })
    }

    fun getCredentials(email: String, password: String): AuthCredential {
        return EmailAuthProvider.getCredential(email, password)
    }

    fun addLocation(location: LatLng) {
        val user = firebaseAuth.currentUser

        val customLatLng = CustomLatLng(location.latitude, location.longitude)

        val usersReference = getDatabaseReference(USERS_TABLE)
        if (user != null) {
            usersReference.child(user.uid).child("location")
                .setValue(customLatLng)
        }
    }

}