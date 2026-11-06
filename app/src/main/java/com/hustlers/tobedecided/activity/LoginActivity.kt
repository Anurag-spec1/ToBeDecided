package com.hustlers.tobedecided.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.hustlers.tobedecided.data.SupabaseProvider
import com.hustlers.tobedecided.databinding.ActivityLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class
LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var passwordVisible = false
    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.visibility = View.INVISIBLE

        lifecycleScope.launch {
            val status = SupabaseProvider.client.auth.sessionStatus
                .first { it !is SessionStatus.Initializing }

            if (status is SessionStatus.Authenticated || status is SessionStatus.RefreshFailure) {
                openMain()
            } else {
                binding.root.visibility = View.VISIBLE
                setupForm()
            }
        }
    }

    private fun setupForm() {
        updateButtonState()

        binding.etEmail.doOnTextChanged { _, _, _, _ -> onInputChanged() }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> onInputChanged() }

        binding.togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etPassword.transformationMethod =
                if (passwordVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            binding.togglePassword.text = if (passwordVisible) "Hide" else "Show"
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignIn()
                true
            } else {
                false
            }
        }

        binding.signIn.setOnClickListener { attemptSignIn() }
    }

    private fun onInputChanged() {
        binding.errorText.visibility = View.GONE
        updateButtonState()
    }

    private fun updateButtonState() {
        val valid = isEmailValid(email()) && password().length >= 6
        binding.signIn.isEnabled = valid && !loading
        binding.signIn.alpha = if (valid && !loading) 1f else 0.5f
    }

    private fun attemptSignIn() {
        if (loading) return

        val email = email()
        val password = password()

        if (!isEmailValid(email)) {
            showError("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        hideKeyboard()
        setLoading(true)

        lifecycleScope.launch {
            try {
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                openMain()
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                setLoading(false)
                showError(messageFor(e))
            } catch (e: HttpRequestException) {
                setLoading(false)
                showError("No internet connection. Check your network and try again.")
            } catch (e: Exception) {
                setLoading(false)
                showError("Something went wrong. Please try again.")
            }
        }
    }

    private fun messageFor(e: RestException): String {
        val text = "${e.error} ${e.description.orEmpty()}".lowercase()
        return when {
            "invalid" in text -> "Incorrect credentials"
            "not confirmed" in text || "email_not_confirmed" in text ->
                "This account is not confirmed yet. Ask your admin."
            "rate" in text || "too many" in text ->
                "Too many attempts. Try again in a few minutes."
            else -> "Sign in failed. Please try again."
        }
    }

    private fun setLoading(value: Boolean) {
        loading = value
        binding.signIn.text = if (value) "Signing in…" else "Sign in"
        binding.etEmail.isEnabled = !value
        binding.etPassword.isEnabled = !value
        binding.togglePassword.isEnabled = !value
        updateButtonState()
        if (value) binding.signIn.alpha = 0.7f
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun email() = binding.etEmail.text.toString().trim()

    private fun password() = binding.etPassword.text.toString()

    private fun isEmailValid(value: String) = Patterns.EMAIL_ADDRESS.matcher(value).matches()

    private fun openMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}