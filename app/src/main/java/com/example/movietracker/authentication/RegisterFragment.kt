package com.example.movietracker.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.databinding.RegisterFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    companion object {
        const val LAST_IMAGE = "last_image"
        const val LAST_EMAIL = "last_email"
        const val BASE_IMAGE_URL = "person-icon.png"
        const val USER_ID = "user_id"
    }

    private val viewModel by viewModels<AuthenticationViewModel>()

    private var binding: RegisterFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment.
        binding = DataBindingUtil.inflate(
            inflater, R.layout.register_fragment, container, false
        )

        binding?.let{
            it.registerToLogin.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }

            it.registerButton.setOnClickListener { registerButtonOnClick() }

            it.inputEmail.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(
                            binding?.inputEmail?.text.toString().trim { it <= ' ' }).matches()
                    ) {
                        binding?.inputEmail?.error = getString(R.string.invalid_email)
                    }
                }
            }

    }

        return binding?.root
    }

    private fun registerButtonOnClick() {
        binding?.let { bind ->
            val email: String = bind.inputEmail.text.toString().trim { it <= ' ' }
            val password: String = bind.inputPassword.text.toString().trim { it <= ' ' }
            val name: String = bind.inputName.text.toString().trim { it <= ' ' }
            when {
                name.isNullOrEmpty() -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.invalid_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !Patterns.EMAIL_ADDRESS.matcher(
                    email
                ).matches() -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.invalid_email),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!?/()@#$%^<>&*.+=_|`~-])(?=\\S+$).{4,}$")
                    .matcher(password)
                    .matches() -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.invalid_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !TextUtils.equals(
                    password,
                    bind.verifyPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        activity,
                        getString(R.string.passwords_not_matching),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    firebaseEmailAuth(email, name, password)
                }
            }
        }

    }

    private fun firebaseEmailAuth(email: String, name: String, password: String) {
        viewModel.getAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.createNewEmailUser(name, email)

                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString(LAST_EMAIL, email)
                        putString(LAST_IMAGE, BASE_IMAGE_URL)
                        putString(USER_ID, viewModel.getCurrentUser()?.uid)
                        apply()
                    }

                    Toast.makeText(
                        activity,
                        getString(R.string.register_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this.activity, MainActivity::class.java)
                    startActivity(intent)
                } else Toast.makeText(
                    activity,
                    task.exception?.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}