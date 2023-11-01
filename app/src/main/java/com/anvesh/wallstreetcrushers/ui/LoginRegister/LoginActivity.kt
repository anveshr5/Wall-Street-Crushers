package com.anvesh.wallstreetcrushers.ui.LoginRegister

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.constraintlayout.motion.widget.TransitionBuilder.validate
import androidx.constraintlayout.widget.ConstraintLayout
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.User
import com.anvesh.wallstreetcrushers.ui.Helper
import com.anvesh.wallstreetcrushers.ui.MainActivity.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    lateinit var etEmail: TextInputEditText
    lateinit var etPassword: TextInputEditText
    lateinit var btnLogin: Button
    lateinit var btnRegister: Button
    lateinit var progressLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initiate()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(!Helper.isValidEmail(email)) {
                showError(etEmail, "Email address is incorrect")
                return@setOnClickListener
            }
            if(password.length < 6){
                showError(etPassword, "Password should be at least 6 characters")
                return@setOnClickListener
            }
            progressLayout.visibility = View.VISIBLE
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                getUser(it.user!!.uid)
            }.addOnFailureListener {
                Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
                progressLayout.visibility = View.GONE
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun getUser(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/Users/${uid}")
        ref.get().addOnSuccessListener {
            val user = it.getValue<User>()!!
            saveUser(user)
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
            progressLayout.visibility = View.GONE
        }
    }

    private fun initiate() {
        toolbar = findViewById(R.id.toolbar)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        progressLayout = findViewById(R.id.progressLayout)
    }

    private fun saveUser(user: User) {
        val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE).edit()
        sharedPrefs.putString("userId", user.userId)
        sharedPrefs.putString("Name", user.name)
        sharedPrefs.putString("Email", user.email)
        sharedPrefs.putString("Phone", user.phone)
        sharedPrefs.putString("Money", user.money)
        sharedPrefs.putString("TotalMoney", user.totalMoney)
        sharedPrefs.apply()
    }

    private fun showError(input: TextInputEditText, error: String) {
        input.error = error
        input.requestFocus()
    }
}