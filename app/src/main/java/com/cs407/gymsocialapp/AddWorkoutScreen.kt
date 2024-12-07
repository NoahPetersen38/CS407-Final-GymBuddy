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




class AddWorkoutScreen : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // UI
    private lateinit var setListContainer: LinearLayout
    private lateinit var workoutTitle: EditText
    private lateinit var workoutDescription: EditText
    private lateinit var addSetButton: Button
    private lateinit var postButton: Button
    private lateinit var backButton: Button

    private var setCounter = 1 // Counter for added sets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_workout_screen, container, false)

        // TODO: Initialize UI components
        setListContainer = view.findViewById(R.id.set_list_container)
        workoutTitle = view.findViewById(R.id.edit_workout_title)
        workoutDescription = view.findViewById(R.id.edit_workout_description)
        addSetButton = view.findViewById(R.id.b_add_set)
        postButton = view.findViewById(R.id.b_post)
        backButton = view.findViewById(R.id.b_back)

        // Set up button listeners
        addSetButton.setOnClickListener { addSet() }
        postButton.setOnClickListener { postWorkout() }
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
        val title = workoutTitle.text.toString()
        val description = workoutDescription.text.toString()

        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter a workout title", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect set data
        val sets = mutableListOf<Map<String, String>>()
        for (i in 0 until setListContainer.childCount) {
            val setView = setListContainer.getChildAt(i)
            val setTitle = setView.findViewById<EditText>(R.id.edit_set_title).text.toString()
            val setReps = setView.findViewById<EditText>(R.id.edit_set_reps).text.toString()
            val setWeight = setView.findViewById<EditText>(R.id.edit_weight).text.toString()

            sets.add(
                mapOf(
                    "title" to setTitle.ifEmpty { "Set ${i + 1}" },
                    "reps" to setReps.ifEmpty { "N/A" },
                    "weight" to setWeight.ifEmpty { "N/A" }
                )
            )
        }

        // TODO: Replace with actual logic for posting
        // Display the posted workout in a Toast (replace with actual logic for posting)
        val setsInfo = sets.joinToString("\n") { set ->
            "Title: ${set["title"]}, Reps: ${set["reps"]}, Weight: ${set["weight"]}"
        }
        val message = """
            Workout Posted!
            Title: $title
            Description: $description
            Sets:
            $setsInfo
        """.trimIndent()
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}