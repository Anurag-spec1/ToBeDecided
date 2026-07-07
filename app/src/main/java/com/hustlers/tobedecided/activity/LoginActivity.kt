package com.hustlers.tobedecided.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.hustlers.tobedecided.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPhoneValidation()

    }

    private fun setupPhoneValidation() {

        binding.etPhone.doOnTextChanged { text, _, _, _ ->

            val phone = text.toString().trim()
            val isValid = phone.matches(Regex("^[6-9]\\d{9}$"))

            binding.sendOtp.isEnabled = isValid

            if (phone.isEmpty()) {
                binding.etPhone.error = null
            } else if (!isValid) {
                binding.etPhone.error = "Please enter a valid Indian mobile number"
            } else {
                binding.etPhone.error = null
            }
        }

        binding.sendOtp.setOnClickListener {

            val phone = binding.etPhone.text.toString().trim()

            if (!phone.matches(Regex("^[6-9]\\d{9}$"))) {
                binding.etPhone.error = "Please enter a valid Indian mobile number"
                return@setOnClickListener
            }

            val intent = Intent(this, VerifyOtp::class.java)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }
    }
}