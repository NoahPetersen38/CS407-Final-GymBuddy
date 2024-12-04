package com.cs407.gymsocialapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.gymsocialapp.data.PostDatabase
import com.cs407.gymsocialapp.data.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LoginScreen() : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var appDB: PostDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login_screen, container, false)

        // Initialize variables
        usernameEditText = view.findViewById<EditText>(R.id.et_username)
        passwordEditText = view.findViewById<EditText>(R.id.et_password)
        loginButton = view.findViewById<Button>(R.id.login_button)
        signUpButton = view.findViewById<Button>(R.id.to_sign_up_button)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        appDB = PostDatabase.getInstance(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Switch to sign-up fragment if the sign up button is clicked
        signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpScreen)
        }

        loginButton.setOnClickListener {
            // Get username and password from fields
            val user = usernameEditText.text.toString().trim()
            val passwd = passwordEditText.text.toString().trim()


            // check if username or password is empty
            if (user.isEmpty() || passwd.isEmpty()) {
            // show an error toast message when username or password field is empty
                Toast.makeText(
                    this.context, "Username or Password field is empty", Toast.LENGTH_LONG)
                    .show()
            } else {
                lifecycleScope.launch {
                    // error catching
                    try{

                        // log-in the user
                        val success: Boolean = getUserPasswd(user, passwd)
                        Log.d("Login Result", success.toString())

                        // navigate with successful login
                        if(success) {
                            // set the newly logged-in user to the viewModel
                            userViewModel.setUser(UserState(
                                0,
                                user,
                                passwd))
                            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                            findNavController().navigate(R.id.action_mainFragment_to_navigation_home)
                        }

                    } catch (e: Exception) {
                        Log.e("login Error", "Error during login: ${e.message}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                parentFragment?.context, "Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserPasswd(
        user: String,
        passwdPlain: String
    ): Boolean {

        // Hash the plain password
        val hashedPasswd: String = hash(passwdPlain)

        // Check if the user has created an account, then compare given PW to the stored PW
        val storedPasswd = userPasswdKV.getString(user, null)
        if (storedPasswd != null) { // Username corresponds to a previously-created account

            if (storedPasswd == hashedPasswd) { // Passwords match
                return true
            } else { // Passwords don't match - tell user to retry
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        parentFragment?.context,
                        "wrong password, please try again",
                        Toast.LENGTH_LONG)
                        .show()
                }
            }

        }  else { // username DNE in SharedPreferences - point user towards sign-up fragment
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    parentFragment?.context,
                    "username does not exist, please sign up",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }

        // user gave the wrong password or does not have an account
        return false
    }

    // Hash used to safely store password in SharedPreferences
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}