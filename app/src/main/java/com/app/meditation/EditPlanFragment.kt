package com.app.meditation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.meditation.databinding.FragmentEditPlanBinding

class EditPlanFragment : Fragment() {

    private var _binding: FragmentEditPlanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BowViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiSettings.observe(viewLifecycleOwner) { settings ->
            if(settings != null) {
                binding.planEditText.setText(settings.planCount.toString())
                binding.screenOnCheckbox.isChecked = settings.keepScreenOn
            }
        }

        binding.saveButton.setOnClickListener {
            val newPlanCount = binding.planEditText.text.toString().toIntOrNull() ?: 111
            val newKeepScreenOn = binding.screenOnCheckbox.isChecked
            viewModel.saveSettings(newPlanCount, newKeepScreenOn)
            
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}