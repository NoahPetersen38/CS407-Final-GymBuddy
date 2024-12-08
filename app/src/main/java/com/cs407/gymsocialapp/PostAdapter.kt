package com.cs407.gymsocialapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.gymsocialapp.data.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Adapter class for RecyclerView
class PostAdapter(private var postsWithUsernames: List<Pair<Post, String>>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.post_title)
        val timestamp: TextView = view.findViewById(R.id.post_timestamp)
        val content: TextView = view.findViewById(R.id.post_content)
        val username: TextView = view.findViewById(R.id.post_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, username) = postsWithUsernames[position]
        holder.title.text = post.title
        holder.content.text = post.content
        holder.username.text = "Posted by: $username"

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.timestamp.text = dateFormat.format(Date(post.timestamp))
    }

    override fun getItemCount(): Int {
        return postsWithUsernames.size
    }

    fun updateData(newPostsWithUsernames: List<Pair<Post, String>>) {
        postsWithUsernames = newPostsWithUsernames
        notifyDataSetChanged()
    }
}