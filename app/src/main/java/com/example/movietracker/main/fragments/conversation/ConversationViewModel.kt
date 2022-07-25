package com.example.movietracker.main.fragments.conversation

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.entity.ChatItem
import com.example.movietracker.main.entity.ChatMessage
import com.example.movietracker.main.entity.User
import com.example.movietracker.main.fragments.profile.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {

    private var _chatItemList = MutableLiveData<ArrayList<ChatItem>?>()
    val chatItemList: LiveData<ArrayList<ChatItem>?>
        get() = _chatItemList

    private var _chatList = MutableLiveData<HashMap<String, ChatMessage>?>()
    val chatList: LiveData<HashMap<String, ChatMessage>?>
        get() = _chatList

    val chatMessages: ArrayList<ChatMessage> = ArrayList<ChatMessage>()
    private var firstUserId: String = ""
    private var secondUserId: String = ""

    var hashMap: HashMap<String, ChatMessage> = HashMap<String, ChatMessage>()
    val currentUser = firebaseService.getCurrentUser()


    fun addChatUsersListListener() {
        val firebaseChatsDatabase =
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst())
        val firebaseUsersDatabase =
            firebaseService.getDatabaseReference(firebaseService.getUsersTableConst())

        val chatList: ArrayList<ChatItem> = ArrayList<ChatItem>()


        firebaseChatsDatabase.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ss in snapshot.children) {
                    val message = ss.getValue(ChatMessage::class.java)
                    if (message != null && currentUser != null && !message.senderId.isNullOrEmpty()
                        && !message.receiverId.isNullOrEmpty()
                    ) {
                        if (message.receiverId == currentUser.uid) {
                            updateMessage(message.senderId, message)
                        } else if (message.senderId == currentUser.uid) {
                            updateMessage(message.receiverId, message)
                        }
                    }
                }
                _chatList.value = hashMap
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    ProfileFragment.TAG,
                    "Failed to get the last messages from the user's conversation",
                    error.toException()
                )
            }
        })

        firebaseUsersDatabase.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ss in snapshot.children) {
                    val user = ss.getValue(User::class.java)
                    user?.let {
                        if (user.id in hashMap.keys) {
                            val message = hashMap[user.id]
                            message?.let {
                                chatList.add(
                                    ChatItem(
                                        user.id,
                                        user.imageUrl,
                                        user.name,
                                        message.senderId,
                                        message.message,
                                        firebaseService.getPathReference(user.imageUrl)
                                    )
                                )
                            }
                        }
                    }

                }
                _chatItemList.value = chatList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    ProfileFragment.TAG,
                    "Failed to read user's chat list",
                    error.toException()
                )
            }
        })

    }

    private fun updateMessage(otherUserId: String, message: ChatMessage) {
        if (otherUserId in hashMap.keys) {
            hashMap[otherUserId]?.let { fullMessage ->
                if (message.getZonedDate().isAfter(fullMessage.getZonedDate())) {
                    hashMap[otherUserId] = message
                }
            }
        } else {
            hashMap[otherUserId] = message
        }
    }


    private fun getConversation(firstUserId: String, secondUserId: String) {
        val firebaseChatsDatabase =
            firebaseService.getDatabaseReference(firebaseService.getChatsTableConst())

        firebaseChatsDatabase.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ss in snapshot.children) {
                    val message = ss.getValue(ChatMessage::class.java)
                    if (message != null && ((message.receiverId == firstUserId && message.senderId == secondUserId) || (message.receiverId == secondUserId && message.senderId == firstUserId))
                    ) {
                        chatMessages.add(message)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    ProfileFragment.TAG,
                    "Failed to get the messages from the user's conversation",
                    error.toException()
                )
            }
        })
    }

    fun deleteConversation(firstUserId: String, secondUserId: String, context: Context) {
        getConversation(firstUserId, secondUserId)
        this.firstUserId = firstUserId
        this.secondUserId = secondUserId

        val builder = AlertDialog.Builder(context)

        with(builder)
        {
            setTitle("Delete Conversation")
            setMessage("Are you sure you want to delete this conversation? The action can not be undone.")
            setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener(function = deleteAllChatMessages)
            )
            setNegativeButton("No", null)
            show()
        }
    }

    private fun deleteChatItem(userId: String) {
        val itemsList = _chatItemList.value
        if (!itemsList.isNullOrEmpty()) {
            for (item in itemsList) {
                if (item.userId == userId) {
                    itemsList.remove(item)
                }
            }
        }
        _chatItemList.value = itemsList
    }

    private val deleteAllChatMessages = { _: DialogInterface, _: Int ->
        if (chatMessages.isNotEmpty()) {
            val firebaseChatsDatabase =
                firebaseService.getDatabaseReference(firebaseService.getChatsTableConst())
            for (message in chatMessages) {
                message.id?.let { firebaseChatsDatabase.child(it).removeValue() }
            }
        }

        if (currentUser != null) {
            if (firstUserId == currentUser.uid) {
                deleteChatItem(secondUserId)
            } else if (secondUserId == currentUser.uid) {
                deleteChatItem(firstUserId)
            }
        }
    }

}