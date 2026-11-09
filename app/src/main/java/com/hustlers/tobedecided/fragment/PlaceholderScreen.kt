package com.hustlers.tobedecided.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hustlers.tobedecided.databinding.FragmentPlaceholderScreenBinding

class PlaceholderScreen : Fragment() {

    private var _binding: FragmentPlaceholderScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        binding.title.text = args.getString(ARG_TITLE)
        binding.subtitle.text = args.getString(ARG_SUBTITLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SUBTITLE = "subtitle"

        fun newInstance(title: String, subtitle: String) = PlaceholderScreen().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_SUBTITLE, subtitle)
            }
        }
    }
}