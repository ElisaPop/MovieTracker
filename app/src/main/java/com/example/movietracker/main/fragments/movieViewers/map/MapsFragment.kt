package com.example.movietracker.main.fragments.movieViewers.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.movietracker.R
import com.example.movietracker.databinding.FragmentMapsBinding
import com.example.movietracker.main.adapters.CustomInfoWindowAdapter
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.entity.UserItemData
import com.example.movietracker.main.fragments.movieViewers.MovieViewersViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

private const val PERMISSION_CODE = 101
@AndroidEntryPoint
class MapsFragment : Fragment() {

    lateinit var movieDetails: MovieItem
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProvider: FusedLocationProviderClient

    private val viewModel: MovieViewersViewModel by lazy {
        ViewModelProvider(this)[MovieViewersViewModel::class.java]
    }
    private var binding: FragmentMapsBinding? = null

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))


        viewModel.addMovieViewersListListener(movieDetails.id.toString())

        viewModel.userItemList.observe(viewLifecycleOwner, { res ->
            if (res != null) {
                setUserLocations(googleMap, res)
            }
        })

        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))
        googleMap.setOnInfoWindowClickListener { marker ->
            onMarkerInfoClick(marker)
        }

        enableMyLocation(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater)
        movieDetails = MapsFragmentArgs.fromBundle(requireArguments()).movie

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchLocation()

        binding?.let { bind ->
            bind.recenterButton.setOnClickListener { fetchLocation() }
            bind.setLocationButton.setOnClickListener { setLocation() }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        menu.findItem(R.id.list_icon).setOnMenuItemClickListener {
            this.findNavController()
                .navigate(
                    MapsFragmentDirections.actionMapsFragmentToMovieViewersFragment(movieDetails)
                )
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasMapPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_CODE
            )
            this.findNavController()
                .navigate(
                    MapsFragmentDirections.actionMapsFragmentToMovieViewersFragment(movieDetails)
                )
            return false
        } else {
            return true
        }
    }

    // Map permissions were added in hasMapPermission()
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        if (hasMapPermission()) {
            val task = fusedLocationProvider.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location

                    val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                    mapFragment?.getMapAsync(callback)
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_CODE
                    )
                    Toast.makeText(
                        activity,
                        getString(R.string.error_map_service),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setLocation() {
        if (::currentLocation.isInitialized) {
            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            viewModel.addLocation(latLng)

            Toast.makeText(
                activity,
                getString(R.string.current_location_set),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                activity,
                getString(R.string.error_location_set),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun enableMyLocation(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_CODE
            )
        }
    }

    private fun setUserLocations(map: GoogleMap, usersData: ArrayList<UserItemData>) {
        for (user in usersData) {
            val userLocation: LatLng?

            user.location?.let { location ->
                userLocation = location.latitude?.let { lat ->
                    location.longitude?.let { long ->
                        LatLng(
                            lat,
                            long
                        )
                    }
                }

                userLocation?.let {
                    val currentUser = viewModel.getCurrentUser()?.uid
                    if (user.id == currentUser) {
                        val markerOptions = MarkerOptions().position(it)
                            .title(getString(R.string.your_set_location)).icon(
                                bitmapDescriptorFromVector(
                                    requireContext(),
                                    R.drawable.ic_baseline_home_24
                                )
                            )
                        val marker = map.addMarker(markerOptions)
                        marker.showInfoWindow()
                    } else {
                        val markerOptions = MarkerOptions().position(it).title(user.name)

                        val pathReference = viewModel.getPathReference(user.imageUrl)
                        pathReference.downloadUrl.addOnSuccessListener { uri ->

                            val snippetToken =
                                currentUser + "%" + user.id + "%" + user.bookmarkDate + "%" + uri

                            markerOptions.snippet(snippetToken)

                            Glide.with(requireContext())
                                .asBitmap()
                                .load(uri.toString())
                                .apply(RequestOptions.circleCropTransform())
                                .into(object : CustomTarget<Bitmap>() {

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        markerOptions.icon(
                                            bitmapDescriptorFromVector(
                                                requireContext(),
                                                R.drawable.ic_baseline_home_24
                                            )
                                        )
                                        map.addMarker(markerOptions)
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap?>?
                                    ) {
                                        val bitmap = Bitmap.createScaledBitmap(
                                            resource,
                                            65,
                                            65,
                                            true
                                        )
                                        markerOptions.icon(
                                            BitmapDescriptorFactory.fromBitmap(
                                                bitmap
                                            )
                                        )
                                        map.addMarker(markerOptions)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                        }
                    }
                }
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun onClickChat(targetUserId: String, currentUserId: String) {
        this.findNavController().navigate(
            MapsFragmentDirections.actionMapsFragmentToChatFragment(
                targetUserId,
                currentUserId
            )
        )
    }

    private fun onMarkerInfoClick(marker: Marker) {
        if (marker.snippet != null) {
            val result = marker.snippet.split("%").toTypedArray()
            val currentId = result[0]
            val id = result[1]

            onClickChat(id, currentId)
        }
    }
}