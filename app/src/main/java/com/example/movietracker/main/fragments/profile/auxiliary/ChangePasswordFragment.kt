package com.example.movietracker.main.fragments.profile.auxiliary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movietracker.R
import com.example.movietracker.databinding.ChangePasswordFragmentBinding
import com.example.movietracker.main.fragments.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var binding: ChangePasswordFragmentBinding? = null
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.change_password_fragment, container, false
        )

        binding?.let {
            it.confirmButton.setOnClickListener {
                onConfirmClick()
            }
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onConfirmClick() {
        binding?.let { bind ->
            val currentPassword: String = bind.currentPassword.text.toString().trim { it <= ' ' }
            val newPassword: String = bind.inputPassword.text.toString().trim { it <= ' ' }
            val verifyNewPassword: String =
                bind.verifyNewPassword.text.toString().trim { it <= ' ' }

            viewModel.changePassword(
                currentPassword,
                newPassword,
                verifyNewPassword,
                requireContext()
            )
        }

    }
}

