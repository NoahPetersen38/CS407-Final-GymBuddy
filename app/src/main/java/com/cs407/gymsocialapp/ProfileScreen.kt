package com.cs407.gymsocialapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch
import com.cs407.gymsocialapp.data.PostDatabase
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
class ProfileScreen() : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var postDatabase: PostDatabase
    private lateinit var calendar: MaterialCalendarView
    private lateinit var userProfileImage: ImageView
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_screen, container, false)

        // Initialize Variables
        userProfileImage = view.findViewById(R.id.profile_image)
        postDatabase = PostDatabase.getInstance(requireContext())
        settingsButton = view.findViewById(R.id.settingsButton)

        // Now that the view is fully created, we can safely access and modify it
        calendar = view.findViewById<MaterialCalendarView>(R.id.materialCalendarView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the current date and set it as the selected date
        val currentDate = Calendar.getInstance()
        calendar?.selectedDate = CalendarDay.from(currentDate.time)

        // Set the selection color
        val selectionColor = ContextCompat.getColor(requireContext(), com.google.android.material.R.color.material_dynamic_neutral90)
        calendar?.setSelectionColor(selectionColor)

        highlightPostsOnCalendar()

        // setup settings button
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_settingsFragment)
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
                        ContextCompat.getDrawable(requireContext(), com.google.android.material.R.color.material_dynamic_neutral60)
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

    /**
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileScreen.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileScreen().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    */
}