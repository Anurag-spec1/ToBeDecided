package com.hustlers.tobedecided.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.hustlers.tobedecided.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupValidation()

        binding.signinText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupValidation() {
        binding.sendOtp.isEnabled = false

        binding.etPhone.doOnTextChanged { text, _, _, _ ->

            val phone = text.toString().trim()

            if (phone.isEmpty()) {
                binding.etPhone.error = null
            } else if (!phone.matches(Regex("^[6-9]\\d{9}$"))) {
                binding.etPhone.error = "Please enter a valid Indian mobile number"
            } else {
                binding.etPhone.error = null
            }

            validateInputs()
        }

        binding.etFullName.doOnTextChanged { text, _, _, _ ->

            val name = text.toString().trim()

            val isValidName =
                name.matches(Regex("^[A-Za-z]+(?:\\s[A-Za-z]+)*$"))

            if (name.isEmpty()) {
                binding.etFullName.error = null
            } else if (!isValidName) {
                binding.etFullName.error = "Please enter a valid name"
            } else {
                binding.etFullName.error = null
            }

            validateInputs()
        }

        binding.sendOtp.setOnClickListener {

            val phone = binding.etPhone.text.toString().trim()
            val name = binding.etFullName.text.toString().trim()

            val isPhoneValid = phone.matches(Regex("^[6-9]\\d{9}$"))
            val isNameValid = name.matches(Regex("^[A-Za-z]+(?:\\s[A-Za-z]+)*$"))

            if (!isPhoneValid) {
                binding.etPhone.error = "Please enter a valid Indian mobile number"
                return@setOnClickListener
            }

            if (!isNameValid) {
                binding.etFullName.error = "Please enter a valid name"
                return@setOnClickListener
            }

            val intent = Intent(this, VerifyOtp::class.java)
            intent.putExtra("phone", phone)
            intent.putExtra("name", name)
            intent.putExtra("fromSignupActivity", true)
            startActivity(intent)
        }
    }

    private fun validateInputs() {

        val phone = binding.etPhone.text.toString().trim()
        val name = binding.etFullName.text.toString().trim()

        val isPhoneValid = phone.matches(Regex("^[6-9]\\d{9}$"))
        val isNameValid = name.matches(Regex("^[A-Za-z]+(?:\\s[A-Za-z]+)*$"))

        binding.sendOtp.isEnabled = isPhoneValid && isNameValid
    }
}