package com.app.meditation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.meditation.databinding.FragmentProgressRecordBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgressRecordFragment : Fragment() {

    private var _binding: FragmentProgressRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by activityViewModels()
    private val args: ProgressRecordFragmentArgs by navArgs()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recordTitle.text = "${args.round}회차 - ${args.day}일차"

        viewModel.getRecordForDay(args.day).observe(viewLifecycleOwner) { record ->
            if (record != null) {
                binding.contentEditText.setText(record.content)
                record.date?.let { 
                    selectedDate.time = it
                    binding.dateEditText.setText(dateFormat.format(it))
                }
            } else {
                initializeViewsWithDefault()
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.deleteContentButton.setOnClickListener {
            showDeleteContentConfirmationDialog()
        }

        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.saveButton.setOnClickListener {
            val content = binding.contentEditText.text.toString()
            viewModel.saveRecord(args.day, content, selectedDate.time)
            findNavController().popBackStack()
        }
    }

    private fun initializeViewsWithDefault() {
        binding.dateEditText.setText(dateFormat.format(Date()))
        selectedDate.time = Date()
        viewModel.getTodaysProgressContent().observe(viewLifecycleOwner) {
            binding.contentEditText.setText(it)
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.dateEditText.setText(dateFormat.format(selectedDate.time))
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showDeleteContentConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("내용 삭제")
            .setMessage("저장된 내용을 모두 삭제하고, 오늘의 운동 기록으로 초기화하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.clearContentForDay(args.day)
                initializeViewsWithDefault()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}