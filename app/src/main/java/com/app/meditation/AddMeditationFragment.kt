package com.app.meditation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.meditation.databinding.FragmentAddMeditationBinding

class AddMeditationFragment : Fragment() {

    private var _binding: FragmentAddMeditationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeditationViewModel by activityViewModels()
    private val args: AddMeditationFragmentArgs by navArgs()

    private var meditationTime = 10
    private var existingMeditation: Meditation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMeditationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        existingMeditation = args.meditationItem
        if (existingMeditation != null) {
            // Edit Mode
            binding.nameEditText.setText(existingMeditation!!.name)
            meditationTime = existingMeditation!!.time
            binding.registerButton.text = "수정"
            binding.deleteButton.visibility = View.VISIBLE
        } else {
            // Add Mode
            viewModel.nextMeditationName.observe(viewLifecycleOwner) { name ->
                if (binding.nameEditText.text.toString().isEmpty()) {
                    binding.nameEditText.setText(name)
                }
            }
            binding.deleteButton.visibility = View.GONE
        }

        updateTimeText()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.minus5Button.setOnClickListener { updateTime(-5) }
        binding.minusButton.setOnClickListener { updateTime(-1) }
        binding.plusButton.setOnClickListener { updateTime(1) }
        binding.plus5Button.setOnClickListener { updateTime(5) }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.registerButton.setOnClickListener {
            var name = binding.nameEditText.text.toString()
            if (name.isBlank()) {
                name = viewModel.nextMeditationName.value ?: "명상"
            }
            
            if (existingMeditation != null) {
                val updatedMeditation = existingMeditation!!.copy(name = name, time = meditationTime)
                viewModel.updateMeditation(updatedMeditation)
            } else {
                viewModel.addMeditation(name, meditationTime)
            }
            findNavController().popBackStack()
        }
    }

    private fun updateTime(delta: Int) {
        meditationTime += delta
        if (meditationTime < 1) meditationTime = 1 // Minimum time is 1 minute
        updateTimeText()
    }

    private fun updateTimeText() {
        binding.timeText.text = "${meditationTime}분"
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("명상 삭제")
            .setMessage("\'${existingMeditation?.name}\' 명상을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                existingMeditation?.let { viewModel.deleteMeditation(it) }
                findNavController().popBackStack()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}