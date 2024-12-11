package com.cs407.gymsocialapp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var logoutButton: Button

    private lateinit var changeNameButton: Button
    private lateinit var firstET: EditText
    private lateinit var lastET: EditText
    private lateinit var changeEmailButton: Button
    private lateinit var emailET: EditText
    private lateinit var confirmEmailET: EditText
    private lateinit var changeUserButton: Button
    private lateinit var usernameET: EditText
    private lateinit var confirmUsernameET: EditText
    private lateinit var changePasswdButton: Button
    private lateinit var currentPasswdET: EditText
    private lateinit var passwdET: EditText
    private lateinit var confirmPasswdET: EditText
    private lateinit var submitButton: Button

    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var appDB: PostDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize variables
        backButton = view.findViewById<ImageButton>(R.id.backButton)
        logoutButton = view.findViewById<Button>(R.id.logoutButton)

        changeNameButton = view.findViewById<Button>(R.id.changeNameButton)
        firstET = view.findViewById<EditText>(R.id.editTextFirst)
        lastET = view.findViewById<EditText>(R.id.editTextLast)
        changeEmailButton = view.findViewById<Button>(R.id.changeEmailButton)
        emailET = view.findViewById<EditText>(R.id.editTextEmail)
        confirmEmailET = view.findViewById<EditText>(R.id.editTextConfirmEmail)
        changeUserButton = view.findViewById(R.id.changeUserButton)
        usernameET = view.findViewById<EditText>(R.id.editTextUsername)
        confirmUsernameET = view.findViewById<EditText>(R.id.editTextConfirmUsername)
        changePasswdButton = view.findViewById<Button>(R.id.changePasswordButton)
        currentPasswdET = view.findViewById<EditText>(R.id.editTextOldPassword)
        passwdET = view.findViewById<EditText>(R.id.editTextPassword)
        confirmPasswdET = view.findViewById<EditText>(R.id.editTextConfirmPassword)
        submitButton = view.findViewById<Button>(R.id.submitButton)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userPasswdKV = requireActivity().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE
        )
        appDB = PostDatabase.getInstance(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // logic for back and logout buttons
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_navigation_profile)
        }
        logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)

        }

        // make EditTexts visible/invisible on the press of each button
        changeNameButton.setOnClickListener {
            if (firstET.visibility == View.GONE) {
                changeNameButton.text = getText(R.string.cancelButton)
                firstET.visibility = View.VISIBLE
                lastET.visibility = View.VISIBLE
            } else {
                changeNameButton.text = getText(R.string.ChangeNameButton)
                firstET.visibility = View.GONE
                lastET.visibility = View.GONE
                firstET.text = null
                lastET.text = null
            }
        }

        changeEmailButton.setOnClickListener {
            if (emailET.visibility == View.GONE) {
                changeEmailButton.text = getText(R.string.cancelButton)
                emailET.visibility = View.VISIBLE
                confirmEmailET.visibility = View.VISIBLE
            } else {
                changeEmailButton.text = getText(R.string.changeEmailButton)
                emailET.visibility = View.GONE
                confirmEmailET.visibility = View.GONE
                emailET.text = null
                confirmEmailET.text = null
            }
        }

        changeUserButton.setOnClickListener {
            if (usernameET.visibility == View.GONE) {
                changeUserButton.text = getText(R.string.cancelButton)
                usernameET.visibility = View.VISIBLE
                confirmUsernameET.visibility = View.VISIBLE
            } else {
                changeUserButton.text = getText(R.string.changeUsernameButton)
                usernameET.visibility = View.GONE
                confirmUsernameET.visibility = View.GONE
                usernameET.text = null
                confirmUsernameET.text = null
            }
        }

        changePasswdButton.setOnClickListener {
            if (passwdET.visibility == View.GONE) {
                changePasswdButton.text = getText(R.string.cancelButton)
                currentPasswdET.visibility = View.VISIBLE
                passwdET.visibility = View.VISIBLE
                confirmPasswdET.visibility = View.VISIBLE
            } else {
                changePasswdButton.text = getText(R.string.ChangePasswordButton)
                currentPasswdET.visibility = View.GONE
                passwdET.visibility = View.GONE
                confirmPasswdET.visibility = View.GONE
                currentPasswdET.text = null
                passwdET.text = null
                confirmPasswdET.text = null
            }
        }

        // implement logic for submit button - update values given they have valid inputs
        submitButton.setOnClickListener {
            // check the visibility on all fields, visible = change this setting
            if (firstET.visibility == View.VISIBLE) {
                val newFirst = firstET.text.toString().trim()
                val newLast = lastET.text.toString().trim()

                // check for valid inputs
                if (newFirst.isEmpty() || newLast.isEmpty()) {
                    Toast.makeText(
                        this.context,
                        "Please fill name fields or press \'cancel\'",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!newFirst.onlyLetters() || !newLast.onlyLetters()) { // check for valid name
                    Toast.makeText(
                        this.context,
                        "Please only enter letters in name fields",
                        Toast.LENGTH_SHORT
                    ).show()
                } else { // Name fields are valid - update account info
                    Toast.makeText(
                        this.context,
                        "Fields Updated!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (emailET.visibility == View.VISIBLE) {
                val newEmail = emailET.text.toString().trim()
                val newConfirmEmail = confirmEmailET.text.toString().trim()

                // Check for valid inputs
                if (newEmail.isEmpty() || newConfirmEmail.isEmpty()) {
                    Toast.makeText(
                        this.context,
                        "Please fill email fields or press \'cancel\'",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!newEmail.contains('@')) {
                    Toast.makeText(
                        this.context,
                        "Please enter a valid email",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (newEmail != newConfirmEmail) {
                    Toast.makeText(
                        this.context,
                        "Email addresses must match",
                        Toast.LENGTH_SHORT
                    ).show()
                } else { // valid inputs - update email

                    lifecycleScope.launch {

                        // update Database
                        val username = userViewModel.userState.value.name
                        val id = userViewModel.userState.value.id
                        val updatedUser = User(id = id, username = username, email = newEmail)
                        appDB.userDao().updateUser(updatedUser)

                    }
                    Toast.makeText(
                        this.context,
                        "Fields Updated!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (usernameET.visibility == View.VISIBLE) {
                val newUser = usernameET.text.toString().trim()
                val newUserConfirm = confirmUsernameET.text.toString().trim()

                // Check for valid inputs
                if (newUser.isEmpty() || newUserConfirm.isEmpty()) {
                    Toast.makeText(
                        this.context,
                        "Please fill username fields or press \'cancel\'",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (newUser != newUserConfirm) {
                    Toast.makeText(
                        this.context,
                        "Usernames must match",
                        Toast.LENGTH_SHORT
                    ).show()
                } else { // Valid inputs - update username
                    lifecycleScope.launch {

                        // Update Database
                        val username = userViewModel.userState.value.name
                        val id = userViewModel.userState.value.id
                        val email = appDB.userDao().getUserById(id)?.email
                        // Necessary if statement due to nullable variable
                        if (email != null) {

                            // check that username isn't already being used
                            val alreadyHasAccount = checkForAccount(newUser)

                            // Username has already been used
                            if (alreadyHasAccount) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        parentFragment?.context,
                                        "Username already in use",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else { // Username isn't in use: update username

                                val updatedUser = User(id = id, username = newUser, email = email)
                                appDB.userDao().updateUser(updatedUser)

                                // update SharedPreferences
                                val hashedPW = userViewModel.userState.value.passwd
                                val editor = userPasswdKV.edit()
                                editor.remove(username)
                                editor.putString(newUser, hashedPW).apply()

                                // update ViewModel
                                userViewModel.setUser(
                                    UserState(id, newUser, hashedPW)
                                )

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        parentFragment?.context,
                                        "Fields Updated!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    parentFragment?.context,
                                    "no email found",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }

            if (passwdET.visibility == View.VISIBLE) {

                val oldPasswd = hash(currentPasswdET.text.toString().trim())
                val newPasswd = passwdET.text.toString().trim()
                val confirmNewPasswd = confirmPasswdET.text.toString().trim()
                val oldStoredPasswd = userViewModel.userState.value.passwd

                // Check for valid inputs
                if (oldPasswd != oldStoredPasswd) {
                    Toast.makeText(
                        this.context,
                        "Given Password does not match stored Password",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (newPasswd.isEmpty() || confirmNewPasswd.isEmpty()) {
                    Toast.makeText(
                        this.context,
                        "Please fill Password fields or press \'cancel\'",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (newPasswd != confirmNewPasswd) {
                    Toast.makeText(
                        this.context,
                        "Password must match",
                        Toast.LENGTH_SHORT
                    ).show()
                } else { // Valid inputs - update username

                    lifecycleScope.launch {

                        // Update SharedPreferences
                        val username = userViewModel.userState.value.name
                        val id = userViewModel.userState.value.id
                        val newHashedPasswd = hash(newPasswd)
                        val editor = userPasswdKV.edit()

                        editor.remove(username)
                        editor.putString(username, newHashedPasswd).apply()

                        // Update ViewModel
                        userViewModel.setUser(
                            UserState(id, username, newHashedPasswd)
                        )

                        // Tell user field was updated
                        Toast.makeText(
                            parentFragment?.context,
                            "Fields Updated!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // String helper function for checking if a String only contains letters
    fun String.onlyLetters() = all { it.isLetter() }

    // Hash used to safely store password in SharedPreferences
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    // function to check if the user already has an account
    private suspend fun checkForAccount (provided_username: String): Boolean {

        val storedPasswd = userPasswdKV.getString(provided_username, null)
        return if (storedPasswd == null) { // user doesn't have an account
            false
        } else { // user has an account
            true
        }
    }

}