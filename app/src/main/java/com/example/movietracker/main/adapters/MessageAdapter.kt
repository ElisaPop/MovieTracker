package com.example.movietracker.main.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.movietracker.R
import com.example.movietracker.databinding.ItemMessageLeftBinding
import com.example.movietracker.databinding.ItemMessageRightBinding
import com.example.movietracker.main.entity.ChatMessage
import com.example.movietracker.main.fragments.chat.ChatViewModel

class MessageAdapter(
    private val chatViewModel: ChatViewModel,
    private val onClickedListener: (imageUrl: String?) -> Unit
) :
    ListAdapter<ChatMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    enum class Direction {
        LEFT, RIGHT
    }

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(chatMessage: ChatMessage)
    }

    class MessageLeftViewHolder(
        var binding: ItemMessageLeftBinding,
        private val chatViewModel: ChatViewModel,
        val onClickedListener: (imageUrl: String?) -> Unit
    ) :
        MessageViewHolder(binding.root) {

        override fun bind(chatMessage: ChatMessage) {
            binding.message.text = chatMessage.message

            if (!chatMessage.mediaUrl.isNullOrEmpty()) {
                val imageString = chatMessage.mediaUrl
                val pathReference = chatViewModel.getPathReference(imageString)

                pathReference.downloadUrl.addOnSuccessListener { uri ->
                    val pictureUrl = uri.toString()

                    binding.image.visibility = View.VISIBLE
                    binding.message.visibility = View.GONE
                    pictureUrl.let { url ->
                        Glide.with(binding.image.context)
                            .load(url)
                            .override(600, 800)
                            .centerCrop()
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.ic_broken_image)
                            )
                            .into(binding.image)
                    }

                    binding.image.setOnClickListener {
                        onClickedListener(pictureUrl)
                    }
                }

            } else {
                binding.image.visibility = View.GONE
                binding.message.visibility = View.VISIBLE
            }

            binding.root.foregroundGravity = Gravity.START
            binding.executePendingBindings()
        }

    }

    class MessageRightViewHolder(
        var binding: ItemMessageRightBinding,
        private val chatViewModel: ChatViewModel,
        val onClickedListener: (imageUrl: String?) -> Unit
    ) :
        MessageViewHolder(binding.root) {

        override fun bind(chatMessage: ChatMessage) {
            binding.message.text = chatMessage.message

            if (!chatMessage.mediaUrl.isNullOrEmpty()) {
                val imageString = chatMessage.mediaUrl
                val pathReference = chatViewModel.getPathReference(imageString)

                pathReference.downloadUrl.addOnSuccessListener { uri ->
                    val pictureUrl = uri.toString()

                    binding.image.visibility = View.VISIBLE
                    pictureUrl.let { url ->
                        Glide.with(binding.image.context)
                            .load(url)
                            .override(500, 700)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.ic_broken_image)
                            )
                            .into(binding.image)
                    }

                    binding.image.setOnClickListener {
                        onClickedListener(pictureUrl)
                    }
                }

            } else {
                binding.image.visibility = View.GONE
            }

            binding.root.foregroundGravity = Gravity.END
            binding.executePendingBindings()
        }

    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
                    || oldItem.senderId == newItem.senderId
                    || oldItem.receiverId == newItem.receiverId
                    || oldItem.message == newItem.message
                    || oldItem.mediaUrl == newItem.mediaUrl
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId != chatViewModel.getCurrentUser()?.uid) Direction.LEFT.ordinal
        else Direction.RIGHT.ordinal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        return if (viewType == Direction.LEFT.ordinal) MessageLeftViewHolder(
            ItemMessageLeftBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), chatViewModel, onClickedListener
        )
        else MessageRightViewHolder(
            ItemMessageRightBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), chatViewModel, onClickedListener
        )

    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatData = getItem(position)
        if (chatData != null) {
            holder.bind(chatData)
        } else {
            holder.itemView.visibility = View.GONE
        }
    }
}