package com.cs407.gymsocialapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets.Side.all
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.gymsocialapp.data.PostDatabase
import com.cs407.gymsocialapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest


class SignUpScreen : Fragment() {

    private lateinit var firstNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var emailET: EditText
    private lateinit var email_confirmET: EditText
    private lateinit var userET: EditText
    private lateinit var passwdET: EditText
    private lateinit var passwd_confirmET: EditText
    private lateinit var signUpButton: Button
    private lateinit var toLogInButton: Button
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var appDB: PostDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_sign_up_screen, container, false)

        // Initialize variables
        firstNameET = view.findViewById<EditText>(R.id.firstName_editText)
        lastNameET = view.findViewById<EditText>(R.id.lastName_editText)
        emailET = view.findViewById<EditText>(R.id.email_editText)
        email_confirmET = view.findViewById<EditText>(R.id.email_confirm_editText)
        userET = view.findViewById<EditText>(R.id.username_editText)
        passwdET = view.findViewById<EditText>(R.id.password_editText)
        passwd_confirmET = view.findViewById<EditText>(R.id.password_confirm_editText)
        signUpButton = view.findViewById<Button>(R.id.create_account_button)
        toLogInButton = view.findViewById<Button>(R.id.to_log_in_button)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)

        appDB = PostDatabase.getInstance(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sends the user back to the log in screen
        toLogInButton.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreen_to_loginFragment)
        }

        // checks user-provided information and creates a new account if information is valid
        signUpButton.setOnClickListener {

            // get information from fields
            val firstName = firstNameET.text.toString().trim()
            val lastName = lastNameET.text.toString().trim()
            val email = emailET.text.toString().trim()
            val email_confirm = email_confirmET.text.toString().trim()
            val user = userET.text.toString().trim()
            val passwd = passwdET.text.toString().trim()
            val passwd_confirm = passwd_confirmET.text.toString().trim()

            // Step 1. Check if any text field is empty
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                email_confirm.isEmpty() || user.isEmpty() || passwd.isEmpty() ||
                passwd_confirm.isEmpty()) {
                Toast.makeText(
                    this.context, "Please fill out all fields", Toast.LENGTH_LONG
                ).show()
            } else { // All fields have been entered

                // Step 2. Check if any fields have invalid inputs
                if (!firstName.onlyLetters() || !lastName.onlyLetters()) { // check for valid name
                    Toast.makeText(
                        this.context,
                        "Please only enter letters in name fields",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (!email.contains('@')) { // check for valid email (contains an @)
                    Toast.makeText(
                        this.context, "Please enter a valid email address", Toast.LENGTH_LONG
                    ).show()
                } else if (email != email_confirm) { // check for matching email addresses
                    Toast.makeText(
                        this.context,
                        "Email addresses entered must be the same",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (passwd != passwd_confirm) { // check for matching passwords
                    Toast.makeText(
                        this.context, "Passwords entered must be the same", Toast.LENGTH_LONG
                    ).show()
                } else {

                    // Step 3: check to make sure user doesn't already have an account
                    lifecycleScope.launch {

                        try {

                            val alreadyHasAccount = checkForAccount(user)

                            // User already has created an account / username has already been used
                            if (alreadyHasAccount) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        parentFragment?.context,
                                        "Username already in use," +
                                                " please change username or log in",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else { // new user

                                // Insert new user into the Room Database
                                val newUser = User(username = user, email = email)
                                appDB.userDao().insertUser(newUser)

                                val newUserTest: User? = appDB.userDao().login(user)
                                if (newUserTest != null) {
                                    Log.d("New user id", newUserTest.id.toString())
                                } else {
                                    Log.d("New user issue", "you're fucked")
                                }


                                // Hash password and store in SharedPreferences for future logins
                                val hashedPasswd = hash(passwd)
                                val editor = userPasswdKV.edit()
                                editor.putString(user, hashedPasswd).apply()

                                // Set the newly created user in ViewModel
                                userViewModel.setUser(
                                    UserState(0, user, hashedPasswd)
                                )

                                // account created - go to home screen
                                findNavController().navigate(R.id.action_signUpScreen_to_mainFragment)
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
    }

    // String helper function for checking if a String only contains letters
    fun String.onlyLetters() = all { it.isLetter() }

    // function to check if the user already has an account
    private suspend fun checkForAccount (provided_username: String): Boolean {

        val storedPasswd = userPasswdKV.getString(provided_username, null)
        return if (storedPasswd == null) { // user doesn't have an account
            false
        } else { // user has an account
            true
        }
    }

    // Hash used to safely store password in SharedPreferences
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}