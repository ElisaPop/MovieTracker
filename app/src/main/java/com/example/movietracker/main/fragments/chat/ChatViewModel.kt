package com.example.movietracker.main.fragments.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movietracker.authentication.LoginFragment
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.BaseServiceModel
import com.example.movietracker.main.entity.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : BaseServiceModel(firebaseService) {

    private var _messagesList = MutableLiveData<ArrayList<ChatMessage>?>()
    val messagesList: LiveData<ArrayList<ChatMessage>?>
        get() = _messagesList

    fun sendMessage(currentId: String, targetId: String, msg: String) {
        val date: ZonedDateTime = ZonedDateTime.now()
        val formattedDate: String = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val key =
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst()).push().key
        if (key != null) {
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst()).child(key)
                .setValue(ChatMessage(key, currentId, targetId, msg, "", formattedDate))
        }
    }

    fun sendImageMessage(currentId: String, targetId: String, url: String) {
        val date: ZonedDateTime = ZonedDateTime.now()
        val formattedDate: String = date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        val key =
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst()).push().key
        if (key != null) {
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst()).child(key)
                .setValue(
                    ChatMessage(
                        key,
                        currentId,
                        targetId,
                        "Sent an image",
                        url,
                        formattedDate
                    )
                )
        }
    }

    fun getMessages(currentId: String, targetId: String) {
        val messages: ArrayList<ChatMessage> = ArrayList<ChatMessage>()

        firebaseService.getDatabaseReference(firebaseService.getChatsTableConst())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (child in dataSnapshot.children) {
                            val message = child.getValue(ChatMessage::class.java)
                            message?.let {
                                if ((message.senderId == currentId && message.receiverId == targetId) ||
                                    (message.senderId == targetId && message.receiverId == currentId)
                                ) {
                                    if (!messages.contains(message))
                                        messages.add(message)
                                }
                            }
                        }
                        messages.sort()
                        _messagesList.value = messages
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(LoginFragment.TAG, databaseError.message)
                }
            })
    }
}

