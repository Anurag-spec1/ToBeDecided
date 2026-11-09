package com.hustlers.tobedecided.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hustlers.tobedecided.activity.ConsentActivity
import com.hustlers.tobedecided.data.AppPrefs
import com.hustlers.tobedecided.databinding.FragmentMemberHomeScreenBinding

class MemberHomeScreen : Fragment() {

    private var _binding: FragmentMemberHomeScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: AppPrefs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = AppPrefs(requireContext())

        binding.greeting.text = "Hi, ${prefs.name ?: "there"}"

        binding.startSharing.setOnClickListener {
            prefs.consentDeclined = false
            startActivity(Intent(requireContext(), ConsentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) render()
    }

    private fun render() {
        val b = _binding ?: return
        if (!prefs.consentDone) {
            b.statusText.text = "Location sharing is off. Your family cannot see where you are."
            b.startSharing.visibility = View.VISIBLE
        } else {
            b.statusText.text = "Setup complete. Live sharing switches on in the next build step."
            b.startSharing.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}