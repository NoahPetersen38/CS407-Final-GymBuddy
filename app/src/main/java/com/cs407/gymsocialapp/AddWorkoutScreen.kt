package com.cs407.gymsocialapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.cs407.gymsocialapp.data.Post
import com.cs407.gymsocialapp.data.PostDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddWorkoutScreen : Fragment() {

    private lateinit var setListContainer: LinearLayout
    private lateinit var workoutTitle: EditText
    private lateinit var workoutDescription: EditText
    private lateinit var addSetButton: Button
    private lateinit var postButton: Button
    private lateinit var addPhotoButton: Button
    private lateinit var imagePreview: ImageView

    private var setCounter = 1 // Counter for added sets

    // Placeholder for currentUserId, replace with actual user ID logic
    private var currentUserId: Int = 1

    private val REQUEST_CAMERA = 101
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_workout_screen, container, false)

        // Initialize UI components
        setListContainer = view.findViewById(R.id.set_list_container)
        workoutTitle = view.findViewById(R.id.edit_workout_title)
        workoutDescription = view.findViewById(R.id.edit_workout_description)
        addSetButton = view.findViewById(R.id.b_add_set)
        postButton = view.findViewById(R.id.b_post)
        addPhotoButton = view.findViewById(R.id.b_add_photo)
        imagePreview = view.findViewById(R.id.image_preview)
        //imageView = view.findViewById(R.id.image_view)


        // Set up button listeners
        addSetButton.setOnClickListener { addSet() }
        postButton.setOnClickListener { postWorkout() }
        addPhotoButton.setOnClickListener { checkPermissions() }

        return view
    }

    private fun addSet() {
        // Inflate a new set view
        val setView =
            LayoutInflater.from(context).inflate(R.layout.item_workout, setListContainer, false)

        // Find and configure set fields
        val setTitle = setView.findViewById<EditText>(R.id.edit_set_title)
        val setReps = setView.findViewById<EditText>(R.id.edit_set_reps)
        val setWeight = setView.findViewById<EditText>(R.id.edit_weight)

        // Set dynamic hints
        setTitle.hint = "Set $setCounter Title"
        setReps.hint = "Set $setCounter x Reps"
        setWeight.hint = "Set $setCounter Weight"

        // Add the view to the container
        setListContainer.addView(setView)
        setCounter++
    }

    private fun postWorkout() {
        val title = workoutTitle.text.toString().trim()
        val description = workoutDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter a workout title", Toast.LENGTH_SHORT).show()
            return
        }

        val sets = StringBuilder()
        for (i in 0 until setListContainer.childCount) {
            val setView = setListContainer.getChildAt(i)
            val setTitle = setView.findViewById<EditText>(R.id.edit_set_title).text.toString().trim()
            val setReps = setView.findViewById<EditText>(R.id.edit_set_reps).text.toString().trim()
            val setWeight = setView.findViewById<EditText>(R.id.edit_weight).text.toString().trim()

            sets.append("Set ${i + 1}: $setTitle, Reps: $setReps, Weight: $setWeight\n")
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = PostDatabase.getInstance(requireContext())
            val timestamp = System.currentTimeMillis()
            val postContent = "$description\n\n$sets"

            val post = Post(
                userId = currentUserId,
                title = title,
                content = postContent,
                timestamp = timestamp,
                imageUri = imageUri?.toString(), // Save the image URI if a photo is added
                imagePath = imageUri?.path
            )

            val postId = db.postDao().insertPost(post)

            CoroutineScope(Dispatchers.Main).launch {
                if (postId > 0) {
                    Toast.makeText(context, "Workout posted successfully!", Toast.LENGTH_SHORT).show()
                    resetFields() // Clear fields after successful post
                } else {
                    Toast.makeText(context, "Failed to post workout.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetFields() {
        // Clear the workout title and description
        workoutTitle.text.clear()
        workoutDescription.text.clear()

        // Remove all dynamically added set views
        setListContainer.removeAllViews()

        // Reset the set counter
        setCounter = 1

        // Clear image preview
        imagePreview.setImageURI(null)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Creating a file to store the image
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "workout_image.jpg")

        // Getting the URI for the file using FileProvider
        imageUri = FileProvider.getUriForFile(requireContext(), "com.cs407.gymsocialapp.fileprovider", file)

        // Passing the URI to the intent as a Parcelable
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri as Parcelable)  // Explicitly cast to Parcelable

        // Starting the camera activity
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            // Display the captured image
            imagePreview.setImageURI(imageUri)
        }
    }
}
