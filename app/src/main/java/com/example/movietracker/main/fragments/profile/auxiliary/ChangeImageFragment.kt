package com.example.movietracker.main.fragments.profile.auxiliary

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.movietracker.R
import com.example.movietracker.databinding.ChangeImageFragmentBinding
import com.example.movietracker.main.fragments.profile.ProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream

@AndroidEntryPoint
class ChangeImageFragment : Fragment() {

    companion object {
        private const val PHOTO_REQUEST_CODE = 36
        private const val GALLERY_PICK_CODE = 41
        private const val FILE_NAME = "Photo"
        private const val FILE_PROVIDER = "com.example.movietracker.fileprovider"
    }

    private lateinit var photoFile: File
    private var binding: ChangeImageFragmentBinding? = null
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.change_image_fragment, container, false
        )

        val currentImageUrl = ChangeImageFragmentArgs.fromBundle(requireArguments()).imageUrl
        val currentUserId = ChangeImageFragmentArgs.fromBundle(requireArguments()).userId
        displayImage(currentImageUrl)

        binding?.let {

            it.takePhotoButton.setOnClickListener {
                takePhotoFunctionality()
            }

            it.uploadPhotoButton.setOnClickListener {
                uploadPhotoFunctionality()
            }

            it.cancelButton.setOnClickListener {
                findNavController().navigate(R.id.action_changeImageFragment_to_profileFragment)
            }

            it.okButton.setOnClickListener {
                saveAndUpdatePhoto(currentUserId)
            }
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun saveAndUpdatePhoto(currentUserId: String) {
        viewModel.uploadImage(photoFile).addOnFailureListener {
            Toast.makeText(activity, getString(R.string.failed_upload), Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            Toast.makeText(activity, getString(R.string.success_upload), Toast.LENGTH_SHORT).show()
            viewModel.changeUserField(
                currentUserId,
                "imageUrl",
                photoFile.name.toString()
            )
        }

        findNavController().navigate(R.id.action_changeImageFragment_to_profileFragment)
    }

    private fun displayImage(imageToDisplay: String?) {
        binding?.let {
            imageToDisplay?.let { imageString ->
                val pathReference = viewModel.getPathReference(imageString)

                pathReference.downloadUrl.addOnSuccessListener { uri ->
                    val profilePictureUrl = uri.toString()

                    profilePictureUrl.let { url ->
                        Glide.with(it.profileImage.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.ic_broken_image)
                            )
                            .into(it.profileImage)
                    }
                }
            }
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    private fun takePhotoFunctionality() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = getPhotoFile(FILE_NAME)

        val fileProvider = activity?.let { it1 ->
            FileProvider.getUriForFile(
                it1, FILE_PROVIDER,
                photoFile
            )
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
        if (activity?.packageManager?.let { it1 -> takePictureIntent.resolveActivity(it1) } != null) {
            startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE)
        } else {
            Toast.makeText(activity, getString(R.string.open_camera_fail), Toast.LENGTH_SHORT)
                .show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, getString(R.string.activity_fail), Toast.LENGTH_SHORT)
                .show()
            return
        }

        super.onActivityResult(requestCode, resultCode, data)

        binding?.let {
            when (requestCode) {
                PHOTO_REQUEST_CODE -> {
                    launchImageCrop(Uri.fromFile(photoFile))
                }

                GALLERY_PICK_CODE -> {
                    if (data != null) {
                        val selectedImageUri: Uri? = data.data
                        val inputStream: InputStream

                        if (selectedImageUri != null) {
                            inputStream =
                                this.requireContext().contentResolver?.openInputStream(
                                    selectedImageUri
                                )!!
                            photoFile.copyInputStreamToFile(inputStream)
                        }
                    }
                    launchImageCrop(Uri.fromFile(photoFile))
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)

                    result.uri.let { url ->
                        val path = url.path
                        if (!path.isNullOrEmpty()) {
                            photoFile = File(path)
                        }

                        Glide.with(it.profileImage.context)
                            .load(url)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.ic_broken_image)
                            )
                            .into(it.profileImage)
                    }
                }
                else -> {
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
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1000, 1000)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(), this)
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }
}