package com.hustlers.tobedecided.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hustlers.tobedecided.activity.LoginActivity
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.data.SessionRouter
import com.hustlers.tobedecided.data.SupabaseProvider
import com.hustlers.tobedecided.databinding.FragmentSettingsScreenBinding
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SettingsScreen : Fragment() {

    private var _binding: FragmentSettingsScreenBinding? = null
    private val binding get() = _binding!!
    private var signingOut = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = AppPrefs(requireContext())

        binding.nameText.text = prefs.name ?: "Unknown"
        binding.roleText.text = if (prefs.role == AppPrefs.ROLE_ADMIN) "Admin" else "Member"

        binding.signOut.setOnClickListener { signOut(prefs) }
    }

    private fun signOut(prefs: AppPrefs) {
        if (signingOut) return
        signingOut = true
        binding.signOut.text = "Signing out…"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                SupabaseProvider.client.auth.signOut()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
            prefs.clear()
            SessionRouter.open(requireActivity(), LoginActivity::class.java)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}