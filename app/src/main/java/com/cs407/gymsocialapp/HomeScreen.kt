package com.cs407.gymsocialapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymsocialapp.data.Post
import com.cs407.gymsocialapp.data.PostDatabase
import com.cs407.gymsocialapp.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeScreen.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeScreen : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var searchInput: EditText
    private lateinit var searchButton: AppCompatImageButton
    private lateinit var followingRecyclerView: RecyclerView
    private lateinit var followingAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)

        recyclerView = view.findViewById(R.id.your_workouts_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchInput = view.findViewById(R.id.search_bar)
        searchButton = view.findViewById(R.id.search_button)

        followingRecyclerView = view.findViewById(R.id.following_recycler_view)
        followingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        followingAdapter = PostAdapter(emptyList()) // Initialize with an empty list
        followingRecyclerView.adapter = followingAdapter


        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUsers(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a username to search", Toast.LENGTH_SHORT).show()
            }
        }



        fetchPosts()

        return view
    }


    private fun searchUsers(username: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = PostDatabase.getInstance(requireContext())

            // Search users matching the username
            val users: List<User> = db.userDao().searchUsersByUsername("%$username%")

            if (users.isNotEmpty()) {
                // Fetch posts by matching users
                val userPosts: List<Pair<Post, String>> = users.flatMap { user ->
                    db.postDao().getPostsByUser(user.id).map { post ->
                        Pair(post, user.username)
                    }
                }

                // Update "Following" RecyclerView with these posts
                followingAdapter.updateData(userPosts)
            } else {
                // Show a message and clear "Following" RecyclerView
                Toast.makeText(requireContext(), "No users found with username: $username", Toast.LENGTH_SHORT).show()
                followingAdapter.updateData(emptyList())
            }
        }
    }




    fun fetchPosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = PostDatabase.getInstance(requireContext())
            val posts = db.postDao().getAllPosts()
            val postsWithUsernames = posts.map { post ->
                val user = db.userDao().getUserById(post.userId)
                Pair(post, user?.username ?: "Unknown User")
            }

            if (!::postAdapter.isInitialized) {
                postAdapter = PostAdapter(postsWithUsernames)
                recyclerView.adapter = postAdapter
            } else {
                postAdapter.updateData(postsWithUsernames)
            }
        }
    }
}