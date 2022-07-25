package com.example.movietracker.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.databinding.UserItemBinding
import com.example.movietracker.main.entity.ChatUsers
import com.example.movietracker.main.entity.UserItemData

class UserAdapter(var currentUserId: String, val onButtonClickedListener: (chatUsers: ChatUsers) -> Unit) :
    ListAdapter<UserItemData, UserAdapter.UserDataViewHolder>(DiffCallback) {

    class UserDataViewHolder(var binding: UserItemBinding, var currentUserId: String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(userItem: UserItemData) {
            binding.userItemData = userItem

            if (!currentUserId.isNullOrEmpty()) {
                if (currentUserId == userItem.id) {
                    binding.chatButton.visibility = View.INVISIBLE
                } else {
                    binding.chatButton.visibility = View.VISIBLE
                }
            }
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserItemData>() {
        override fun areItemsTheSame(oldItem: UserItemData, newItem: UserItemData): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areContentsTheSame(oldItem: UserItemData, newItem: UserItemData): Boolean {
            return oldItem.bookmarkDate == newItem.bookmarkDate
                    || oldItem.location?.longitude == newItem.location?.longitude
                    || oldItem.location?.latitude == newItem.location?.latitude
                    || oldItem.name == newItem.name
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserDataViewHolder {
        return UserDataViewHolder(
            UserItemBinding.inflate(LayoutInflater.from(parent.context)),
            currentUserId
        )
    }

    override fun onBindViewHolder(holder: UserDataViewHolder, position: Int) {
        val movieData = getItem(position)
        if (movieData != null) {
            holder.bind(movieData)
            holder.binding.chatButton.setOnClickListener {
                onButtonClickedListener(
                    ChatUsers(
                        movieData.id,
                        currentUserId
                    )
                )
            }

        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }
}