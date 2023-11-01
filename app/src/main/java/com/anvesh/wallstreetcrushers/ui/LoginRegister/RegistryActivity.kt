package com.anvesh.wallstreetcrushers.ui.LoginRegister

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.User
import com.anvesh.wallstreetcrushers.ui.Helper
import com.anvesh.wallstreetcrushers.ui.MainActivity.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistryActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    lateinit var etName: TextInputEditText
    lateinit var etEmail: TextInputEditText
    lateinit var etPhone: TextInputEditText
    lateinit var etPassword: TextInputEditText
    lateinit var btnRegister: Button
    lateinit var progressLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        initiate()

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhone.text.toString()
            val password = etPassword.text.toString()
            if(name.length < 3){
                showError(etName, "Please Input Full Name")
                return@setOnClickListener
            }
            if(!Helper.isValidEmail(email)){
                showError(etEmail, "Incorrect Email format")
                return@setOnClickListener
            }
            if(!Helper.isPhoneNumber(phone)){
                showError(etPhone, "Incorrect phone format")
                return@setOnClickListener
            }
            if(!Helper.checkPasswordStrength(password)){
                showError(etPassword, "Please keep a stronger password")
                return@setOnClickListener
            }
            progressLayout.visibility = View.VISIBLE
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                registerUser(it.user!!.uid, name, email, phone)
            }.addOnFailureListener {
                progressLayout.visibility = View.GONE
                Toast.makeText(this, "Something went wrong. Try again later.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerUser(uid: String, name: String, email: String, phone: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/Users/${uid}")
        val user = User(uid, name, email, phone, "0", "0")
        ref.setValue(user).addOnSuccessListener {
            saveUser(user)
            val intent = Intent(this@RegistryActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            progressLayout.visibility = View.GONE
            Toast.makeText(this, "Something went wrong. Try again later.", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUser(user: User) {
        val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE).edit()
        sharedPrefs.putString("userId", user.userId)
        sharedPrefs.putString("Name", user.name)
        sharedPrefs.putString("Email", user.email)
        sharedPrefs.putString("Phone", user.phone)
        sharedPrefs.putString("Money", user.money)
        sharedPrefs.putString("TotalMoney", user.money)
        sharedPrefs.apply()
    }

    private fun initiate() {
        toolbar = findViewById(R.id.toolbar)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressLayout = findViewById(R.id.progressLayout)
    }

    private fun showError(input: TextInputEditText, error: String) {
        input.error = error
        input.requestFocus()
    }
}