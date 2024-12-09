package com.cs407.gymsocialapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cs407.gymsocialapp.data.LoginRepository
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch
import com.cs407.gymsocialapp.data.PostDatabase
import java.io.File
import androidx.core.content.FileProvider
import android.Manifest
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.cs407.gymsocialapp.data.LoginDataSource
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileScreen.newInstance] factory method to
 * create an instance of this fragment.
 */

class ProfileScreen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var postDatabase: PostDatabase
    private lateinit var calendar: MaterialCalendarView
    // for prof picture and username
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var photoUri: Uri? = null
    private lateinit var loginRepository: LoginRepository

    //settings
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // profile pic pic
        // camera and photo library launchers
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri?.let {
                    profileImageView.setImageURI(it)
                } ?: run {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    profileImageView.setImageURI(imageUri)
                } else {
                    Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        setHasOptionsMenu(true)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_screen, container, false)

        // Initialize UI components
        profileImageView = view.findViewById(R.id.profile_image)
        usernameTextView = view.findViewById(R.id.profile_username)
        settingsButton = view.findViewById(R.id.settingsButton)
        calendar = view.findViewById(R.id.materialCalendarView)
        postDatabase = PostDatabase.getInstance(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // log in
        val loginDataSource = LoginDataSource()
        loginRepository = LoginRepository(loginDataSource)

        // Fetch and display the logged-in username
        val username = getLoggedInUsername()
        usernameTextView.text = username

        // Set up profile picture click listener
        profileImageView.setOnClickListener {
            if (checkPermissions()) {
                showImagePickerOptions()
            } else {
                requestPermissions()
            }
        }

        // setup settings button
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_settingsFragment)
        }

        // Get the current date and set it as the selected date
        val currentDate = Calendar.getInstance()
        calendar.selectedDate = CalendarDay.from(currentDate.time)

        // Set the selection color
        val selectionColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.material_dynamic_neutral90)
        calendar.setSelectionColor(selectionColor)

        highlightPostsOnCalendar()

    }


    private fun getLoggedInUsername(): String {
        val loggedInUser = loginRepository.user
        return loggedInUser?.displayName ?: ""
    }

    private fun showImagePickerOptions() {
        Toast.makeText(requireContext(), "Image picker options shown", Toast.LENGTH_SHORT).show()
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    private fun createImageFile(): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "profile_${System.currentTimeMillis()}",
            ".jpg",
            storageDir
        )
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT).show()
                showImagePickerOptions()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findRecentStreak(dates: List<CalendarDay>): List<CalendarDay> {
        val streak = mutableListOf<CalendarDay>()

        if (dates.isEmpty()) {
            return streak
        }

        streak.add(dates.first()) // Add the most recent date to start the streak

        var lastDate = dates.first()
        for (i in 1 until dates.size) {
            val currentDate = dates[i]

            // Compare the current date and last date
            val isOneDayDifference = when {
                currentDate.year > lastDate.year -> true // New year, so it’s at least one day
                currentDate.year < lastDate.year -> false // Same logic for years
                currentDate.month > lastDate.month -> true // Next month
                currentDate.month < lastDate.month -> false // Previous month
                currentDate.day > lastDate.day -> currentDate.day - lastDate.day == 1 // Same month, check if days are 1 apart
                else -> false // Same day
            }

            if (isOneDayDifference) {
                streak.add(currentDate)
                lastDate = currentDate
            } else {
                // End the streak once we hit a non-consecutive date
                break
            }

            // Stop if we have reached the current day
            if (currentDate == CalendarDay.from(Calendar.getInstance().time)) {
                break
            }
        }
        return streak
    }

    private fun highlightPostsOnCalendar() {
        // Use a coroutine to load posts asynchronously
        lifecycleScope.launch {
            // Get all post timestamps
            val postTimestamps = postDatabase.postDao().getAllPostTimestamps()

            // Convert timestamps to CalendarDay objects
            val datesToHighlight = postTimestamps.map { timestamp ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                CalendarDay.from(calendar.time)
            }

            val sortedDates = datesToHighlight.sortedByDescending { it.date }
            val streak = findRecentStreak(sortedDates)

            val streakNumber = view?.findViewById<TextView>(R.id.streak)
            streakNumber?.text = "\uD83D\uDD25Streak: " + (streak?.size ?: 0)

            // Add decorators to highlight dates on the calendar
            val decorators = datesToHighlight.map { date ->
                object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        return day == date
                    }

                    override fun decorate(view: DayViewFacade?) {
                        // Customize the highlight (e.g., use a background color)
                        ContextCompat.getDrawable(requireContext(), com.google.android.material.R.color.material_dynamic_neutral20)
                            ?.let {
                                view?.setBackgroundDrawable(
                                    it
                                )
                            }
                    }
                }
            }

            // Apply the decorators to the calendar
            decorators.forEach { decorator ->
                calendar.addDecorator(decorator)
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

}