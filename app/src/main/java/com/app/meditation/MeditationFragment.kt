package com.app.meditation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.meditation.databinding.FragmentMeditationBinding

class MeditationFragment : Fragment() {

    private var _binding: FragmentMeditationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeditationViewModel by activityViewModels()
    private lateinit var meditationAdapter: MeditationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.addMeditationButton.setOnClickListener {
            val action = MeditationFragmentDirections.actionMeditationFragmentToAddMeditationFragment(null)
            findNavController().navigate(action)
        }

        binding.viewRecordButton.setOnClickListener {
            val action = MeditationFragmentDirections.actionMeditationFragmentToMeditationRecordFragment()
            findNavController().navigate(action)
        }

        binding.exitButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupRecyclerView() {
        meditationAdapter = MeditationAdapter(
            onEditClick = { meditation ->
                val action = MeditationFragmentDirections.actionMeditationFragmentToAddMeditationFragment(meditation)
                findNavController().navigate(action)
            },
            onItemClick = { meditation ->
                val action = MeditationFragmentDirections.actionMeditationFragmentToMeditationDetailFragment(meditation)
                findNavController().navigate(action)
            }
        )
        binding.meditationRecyclerView.apply {
            adapter = meditationAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewModel.meditations.observe(viewLifecycleOwner) {
            meditationAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}