package com.example.android.myproject.Login

import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.myproject.Chat_Main.MainActivity
import com.example.android.myproject.R
import com.example.android.myproject.SignUp.SignUpActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var btn1: Button
    private lateinit var btn2: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgetPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.signin_email)
        passwordEditText = findViewById(R.id.signin_password)
        btn1 = findViewById(R.id.buttonLogin)
        btn2 = findViewById(R.id.buttonSignUpPage)
        forgetPassword = findViewById(R.id.forgetPassword)

        btn1.setOnClickListener(){
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(!isInternetAvailable()){
                Toast.makeText(this, "Please turn on your internet connection.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmail(email, password)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        btn2.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        forgetPassword.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                        } else {
                            val message = task.exception?.message ?: "Unknown error"
                            Toast.makeText(this, "Failed to send reset email: $message", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        btn1.isEnabled=false
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btn1.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Login Failed: No user found with this email.", Toast.LENGTH_LONG).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Login Failed: Incorrect password.", Toast.LENGTH_LONG).show()
                        passwordEditText.error = "Incorrect password"
                        passwordEditText.requestFocus()
                        passwordEditText.text.clear()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Login Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

}