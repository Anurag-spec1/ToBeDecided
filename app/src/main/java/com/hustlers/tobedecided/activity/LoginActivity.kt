package com.hustlers.tobedecided.activity

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
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.data.Route
import com.hustlers.tobedecided.data.SessionRouter
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

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: AppPrefs
    private var passwordVisible = false
    private var loading = false
    private var formReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPrefs(this)
        binding.root.visibility = View.INVISIBLE

        lifecycleScope.launch {
            val status = SupabaseProvider.client.auth.sessionStatus
                .first { it !is SessionStatus.Initializing }

            if (status is SessionStatus.Authenticated || status is SessionStatus.RefreshFailure) {
                routeAfterAuth()
            } else {
                showForm()
            }
        }
    }

    private suspend fun routeAfterAuth() {
        when (val route = SessionRouter.resolve(this)) {
            is Route.Go -> SessionRouter.open(this, route.target)
            Route.NoProfile -> {
                signOutQuietly()
                prefs.clear()
                showForm()
                setLoading(false)
                showError("Your account is not set up yet. Ask your admin.")
            }
            Route.NeedsInternet -> {
                showForm()
                setLoading(false)
                showError("Connect to the internet once to finish signing in.")
            }
            is Route.Error -> {
                showForm()
                setLoading(false)
                showError("Could not load your profile: ${route.message}")
            }
        }
    }

    private suspend fun signOutQuietly() {
        try {
            SupabaseProvider.client.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
        }
    }

    private fun showForm() {
        binding.root.visibility = View.VISIBLE
        if (!formReady) {
            formReady = true
            setupForm()
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
            val signedIn = try {
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: RestException) {
                setLoading(false)
                showError(messageFor(e))
                false
            } catch (e: HttpRequestException) {
                setLoading(false)
                showError("No internet connection. Check your network and try again.")
                false
            } catch (e: Exception) {
                setLoading(false)
                showError("Something went wrong. Please try again.")
                false
            }

            if (signedIn) routeAfterAuth()
        }
    }

    private fun messageFor(e: RestException): String {
        val text = "${e.error} ${e.description.orEmpty()}".lowercase()
        return when {
            "invalid" in text -> "Incorrect email or password"
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
}