package com.example.android.myproject.SignUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.myproject.Entities.User
import com.example.android.myproject.Login.LoginActivity
import com.example.android.myproject.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var btn2: Button
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var nameEditText : TextInputEditText
    private lateinit var dataBase : DatabaseReference
    private lateinit var signin : TextView
    private lateinit var back : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.signup_email)
        passwordEditText = findViewById(R.id.signup_password)
        nameEditText = findViewById(R.id.signup_username)
        signin = findViewById(R.id.buttonSignInPage)
        back = findViewById(R.id.back_signup)

        back.visibility = View.VISIBLE

        back.setOnClickListener {
            finish()
        }

        btn2 = findViewById(R.id.buttonSignUp)

        btn2.setOnClickListener(){
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                checkUsernameAndSignup(name, email, password)
            } else {
                Toast.makeText(this, "Please enter email, name and password", Toast.LENGTH_SHORT).show()
            }
        }

        signin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }

    private fun checkUsernameAndSignup(name: String, email: String, password: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("user")
        userRef.get().addOnSuccessListener { snapshot ->
            var isUnique = true
            for (userSnap in snapshot.children) {
                val existingName = userSnap.child("name").value?.toString()
                if (existingName.equals(name, ignoreCase = true)) {
                    isUnique = false
                    break
                }
            }

            if (isUnique) {
                signupWithEmail(name, email, password)
            } else {
                Toast.makeText(this, "Username already taken. Please choose another.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error checking username: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun signupWithEmail(name : String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                    addUserToDataBase(name,email,mAuth.uid!!)
                    finish()
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Toast.makeText(this, "Signup failed! $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDataBase(name : String, email: String, uid : String){
        dataBase = FirebaseDatabase.getInstance().getReference()
        dataBase.child("user").child(uid).setValue(User(uid, name, email))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}