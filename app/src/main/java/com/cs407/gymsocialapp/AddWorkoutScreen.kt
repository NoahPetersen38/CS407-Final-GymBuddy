package com.cs407.gymsocialapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.cs407.gymsocialapp.data.Post
import com.cs407.gymsocialapp.data.PostDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale




class AddWorkoutScreen : Fragment() {

    private lateinit var setListContainer: LinearLayout
    private lateinit var workoutTitle: EditText
    private lateinit var workoutDescription: EditText
    private lateinit var addSetButton: Button
    private lateinit var postButton: Button
    private lateinit var backButton: Button

    private var setCounter = 1 // Counter for added sets
    private var currentUserId: Int = 1 // Replace with actual user ID (e.g., from login)

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
        backButton = view.findViewById(R.id.b_back)

        // Set up button listeners
        addSetButton.setOnClickListener { addSet() }
        postButton.setOnClickListener { postWorkout() }
        // TODO: maybe wrong
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        return view
    }

    private fun addSet() {
        // Inflate a new set view
        val setView = LayoutInflater.from(context).inflate(R.layout.item_workout, setListContainer, false)

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

        // Collect sets data as a string (you could also enhance this with more structure)
        val sets = StringBuilder()
        for (i in 0 until setListContainer.childCount) {
            val setView = setListContainer.getChildAt(i)
            val setTitle = setView.findViewById<EditText>(R.id.edit_set_title).text.toString().trim()
            val setReps = setView.findViewById<EditText>(R.id.edit_set_reps).text.toString().trim()
            val setWeight = setView.findViewById<EditText>(R.id.edit_weight).text.toString().trim()

            sets.append("Set ${i + 1}: $setTitle, Reps: $setReps, Weight: $setWeight\n")
        }

        // Add the workout post to the database
        CoroutineScope(Dispatchers.IO).launch {
            val db = PostDatabase.getInstance(requireContext())
            val timestamp = System.currentTimeMillis()
            val post = Post(
                userId = currentUserId,
                title = title,
                content = "$description\n\n$sets",
                timestamp = timestamp
            )

            val postId = db.postDao().insertPost(post)

            CoroutineScope(Dispatchers.Main).launch {
                if (postId > 0) {
                    Toast.makeText(context, "Workout posted successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate back to the home screen or update the UI
                    // requireActivity().onBackPressed()
                } else {
                    Toast.makeText(context, "Failed to post workout.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}