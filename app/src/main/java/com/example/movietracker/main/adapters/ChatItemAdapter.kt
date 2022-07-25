package com.example.movietracker.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.R
import com.example.movietracker.databinding.ChatItemBinding
import com.example.movietracker.main.entity.ChatItem
import com.example.movietracker.main.entity.ChatUsers
import com.example.movietracker.main.fragments.conversation.ConversationViewModel

class ChatItemAdapter(
    val onClickedListener: (chatUsers: ChatUsers) -> Unit,
    val viewModel: ConversationViewModel,
    val context: Context
) :
    ListAdapter<ChatItem, ChatItemAdapter.ChatItemViewHolder>(DiffCallback) {

    class ChatItemViewHolder(var binding: ChatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userItem: ChatItem) {
            binding.userItemData = userItem
            binding.executePendingBindings()
        }

    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem.senderId == newItem.senderId
                    || oldItem.message == newItem.message
                    || oldItem.userName == newItem.userName
                    || oldItem.imageUrl == newItem.imageUrl
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatItemViewHolder {
        return ChatItemViewHolder(
            ChatItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        val chatItem = getItem(position)
        if (chatItem != null && viewModel.currentUser != null) {
            holder.bind(chatItem)
            holder.itemView.setOnClickListener {
                onClickedListener(ChatUsers(chatItem.userId, viewModel.currentUser.uid))
            }

            holder.binding.message.text

            if (chatItem.userId == chatItem.senderId) {
                holder.binding.message.text = chatItem.message
            } else {
                holder.binding.message.text =
                    holder.itemView.context.getString(
                        R.string.message_sent_by_user,
                        chatItem.message
                    )
            }

        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }

    fun deleteItem(position: Int) {
        val chatItem = getItem(position)
        if (viewModel.currentUser != null && !chatItem.userId.isNullOrEmpty())
            viewModel.deleteConversation(chatItem.userId, viewModel.currentUser.uid, context)
        notifyItemRemoved(position)
        //notifyDataSetChanged()
    }

}