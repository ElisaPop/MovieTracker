package com.example.movietracker.main.fragments.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.movietracker.R
import com.example.movietracker.authentication.LoginFragment
import com.example.movietracker.databinding.FragmentChatBinding
import com.example.movietracker.main.adapters.MessageAdapter
import com.example.movietracker.main.entity.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream


@AndroidEntryPoint
class ChatFragment : Fragment() {

    companion object {
        private const val GALLERY_PICK_CODE = 41
        private const val FILE_NAME = "Photo"
        private const val FILE_PROVIDER = "com.example.movietracker.fileprovider"
    }

    private lateinit var photoFile: File

    private val viewModel by viewModels<ChatViewModel>()

    private var binding: FragmentChatBinding? = null
    lateinit var currentId: String
    lateinit var targetId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        currentId = ChatFragmentArgs.fromBundle(
            requireArguments()
        ).currentId
        targetId = ChatFragmentArgs.fromBundle(
            requireArguments()
        ).targetId

        binding?.let {

            viewModel.getMessages(currentId, targetId)

            val adapter = MessageAdapter(viewModel, ::onClickImage)
            it.messageListView.adapter = adapter
            it.sendButton.setOnClickListener { onSendButtonClicked() }
            it.photoPickerButton.setOnClickListener { uploadPhotoFunctionality() }

            viewModel.messagesList.observe(viewLifecycleOwner, Observer { res ->
                if (res != null) {
                    adapter.submitList(viewModel.messagesList.value)
                    it.messageListView.visibility = View.VISIBLE
                    adapter.notifyItemInserted(adapter.itemCount)
                }
                it.progressBar.visibility = View.GONE
            })
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setActionBar()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        binding = null
        super.onDestroyView()
    }

    private fun onSendButtonClicked() {
        binding?.let {
            val msg = it.messageEditText.text
            if (!msg.isNullOrEmpty()) {
                viewModel.sendMessage(currentId, targetId, msg.toString())
            }
            it.messageEditText.text.clear()
        }
    }

    private fun setActionBar() {
        val actionBar: ActionBar? = (activity as AppCompatActivity).supportActionBar

        actionBar?.let {
            actionBar.setDisplayShowCustomEnabled(true)

            val inflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val v: View = inflater.inflate(R.layout.chat_action_bar, null)

            val image = v.findViewById<ImageView>(R.id.profile_picture_action_bar)
            var name = v.findViewById<TextView>(R.id.user_name)

            viewModel.addUserListener(targetId, object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userDetails = dataSnapshot.getValue(User::class.java)
                        userDetails?.let { it1 ->
                            val pathReference = viewModel.getPathReference(
                                it1.imageUrl
                            )

                            name.text = userDetails.name

                            pathReference.downloadUrl.addOnSuccessListener { uri ->
                                val pictureUrl = uri.toString()

                                pictureUrl.let { url ->
                                    Glide.with(image.context)
                                        .load(url)
                                        .apply(RequestOptions.circleCropTransform())
                                        .listener(object : RequestListener<Drawable> {
                                            override fun onLoadFailed(
                                                e: GlideException?,
                                                model: Any?,
                                                target: Target<Drawable>?,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                return false
                                            }

                                            override fun onResourceReady(
                                                resource: Drawable?,
                                                model: Any?,
                                                target: Target<Drawable>?,
                                                dataSource: DataSource?,
                                                isFirstResource: Boolean
                                            ): Boolean {
                                                return false
                                            }
                                        }).into(image)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(LoginFragment.TAG, databaseError.message)
                }
            })

            actionBar.customView = v

        }

    }

    private fun uploadPhotoFunctionality() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        photoFile = getPhotoFile(
            FILE_NAME
        )

        val fileProvider = activity?.let { it1 ->
            FileProvider.getUriForFile(
                it1, FILE_PROVIDER,
                photoFile
            )
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (activity?.packageManager?.let { it1 -> intent.resolveActivity(it1) } != null) {
            startActivityForResult(intent, GALLERY_PICK_CODE)
        } else {
            Toast.makeText(activity, getString(R.string.open_gallery_fail), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, getString(R.string.activity_fail), Toast.LENGTH_SHORT)
                .show()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)

        binding?.let {
            if (requestCode == GALLERY_PICK_CODE) {
                if (data != null) {
                    val selectedImageUri: Uri? = data.data
                    val inputStream: InputStream

                    if (selectedImageUri != null) {
                        inputStream =
                            this.requireContext().contentResolver?.openInputStream(
                                selectedImageUri
                            )!!
                        photoFile.copyInputStreamToFile(inputStream)
                        saveAndUpdatePhoto()
                    }
                }
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.activity_unknown),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }

        }
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }

    private fun saveAndUpdatePhoto() {
        viewModel.uploadImage(photoFile).addOnFailureListener {
            Toast.makeText(activity, getString(R.string.failed_upload), Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            Toast.makeText(activity, getString(R.string.success_upload), Toast.LENGTH_SHORT).show()
            viewModel.sendImageMessage(
                currentId, targetId,
                photoFile.name.toString()
            )
        }
    }

    private fun onClickImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            this.findNavController().navigate(
                ChatFragmentDirections.actionChatFragmentToFullImageFragment(
                    imageUrl
                )
            )
        }
    }

}