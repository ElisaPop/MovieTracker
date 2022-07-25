package com.example.movietracker.main.fragments.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.databinding.ConversationFragmentBinding
import com.example.movietracker.main.adapters.ChatItemAdapter
import com.example.movietracker.main.entity.ChatUsers
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationFragment : Fragment() {

    private var binding: ConversationFragmentBinding? = null
    private val viewModel by viewModels<ConversationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ConversationFragmentBinding.inflate(inflater)

        binding?.let {
            it.lifecycleOwner = this

            val adapter = ChatItemAdapter(::onClickChat, viewModel, requireContext())

            val swipeGesture = object : SwipeGesture(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            adapter.deleteItem(viewHolder.absoluteAdapterPosition)
                        }
                    }
                }
            }

            val touchHelper = ItemTouchHelper(swipeGesture)
            touchHelper.attachToRecyclerView(it.viewersList)

            it.viewersList.adapter = adapter

            viewModel.addChatUsersListListener()

            viewModel.chatItemList.observe(viewLifecycleOwner, Observer { res ->
                if (res != null) {
                    adapter.submitList(viewModel.chatItemList.value)
                    adapter.notifyDataSetChanged()
                }
            })
        }
        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onClickChat(chatUsers: ChatUsers) {
        if (!chatUsers.targetId.isNullOrEmpty() && !chatUsers.currentId.isNullOrEmpty()) {
            this.findNavController().navigate(
                ConversationFragmentDirections.actionConversationFragmentToChatFragment(
                    chatUsers.targetId,
                    chatUsers.currentId
                )
            )
        }

    }

}