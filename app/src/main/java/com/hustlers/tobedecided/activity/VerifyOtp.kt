package com.hustlers.tobedecided.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import carbon.view.View
import com.hustlers.tobedecided.R
import com.hustlers.tobedecided.databinding.ActivityVerifyOtpBinding

class VerifyOtp : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phoneNumber = intent.getStringExtra("phone") ?: ""
        val isNavigatedFromSignup = intent.getBooleanExtra("fromSignupActivity",false)
        binding.constTextNumber.text = "Code sent to +91 $phoneNumber"

        if (isNavigatedFromSignup){
            binding.verifyOtp.text="Verify & Signup"
        }

        setupOtp()
        startTimer()

        binding.resend.setOnClickListener {
            startTimer()
        }

        binding.verifyOtp.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }

    }

    private fun startTimer() {

        binding.timer.visibility = View.VISIBLE
        binding.resend.text = "\uD83D\uDD65 Resend code in "
        binding.resend.setTextColor(Color.parseColor("#808080"))
        binding.resend.isClickable = false
        binding.resend.isEnabled = false

        object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.timer.text = "${seconds}s"
            }

            override fun onFinish() {

                binding.timer.visibility = View.GONE
                binding.resend.text = "Resend"
                binding.resend.setTextColor(Color.parseColor("#36DA77"))
                binding.resend.isClickable = true
                binding.resend.isEnabled = true
            }

        }.start()
    }

    private fun setupOtp() {

        val boxes = arrayOf(
            binding.et1, binding.et2, binding.et3, binding.et4, binding.et5, binding.et6
        )

        for (i in boxes.indices) {

            boxes[i].addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().isNotEmpty()) {

                        if (i < boxes.size - 1) {
                            boxes[i + 1].requestFocus()
                        }

                    }

                }

                override fun afterTextChanged(s: Editable?) {}
            })

            boxes[i].setOnKeyListener { _, keyCode, event ->

                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && boxes[i].text.isNullOrEmpty()) {

                    if (i > 0) {
                        boxes[i - 1].requestFocus()
                    }

                }

                false
            }

        }

        boxes[0].requestFocus()

    }
}