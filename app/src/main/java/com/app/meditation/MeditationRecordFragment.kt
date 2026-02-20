package com.app.meditation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.meditation.databinding.DialogMeditationRecordDetailBinding
import com.app.meditation.databinding.FragmentMeditationRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MeditationRecordFragment : Fragment() {

    private var _binding: FragmentMeditationRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeditationViewModel by activityViewModels()
    private lateinit var recordAdapter: MeditationRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeditationRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        recordAdapter = MeditationRecordAdapter { summary ->
            viewModel.onDateClicked(summary)
        }
        binding.recordRecyclerView.apply {
            adapter = recordAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        viewModel.monthlyRecordSummaries.observe(viewLifecycleOwner) {
            recordAdapter.submitList(it)
        }

        viewModel.currentDisplayCalendar.observe(viewLifecycleOwner) {
            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            binding.monthYearText.text = monthYearFormat.format(it.time)
        }

        viewModel.selectedMeditationRecords.observe(viewLifecycleOwner) { records ->
            records?.let { showDetailDialog(it) }
        }
    }

    private fun setupClickListeners() {
        binding.prevMonthButton.setOnClickListener {
            viewModel.changeMonth(-1)
        }

        binding.nextMonthButton.setOnClickListener {
            viewModel.changeMonth(1)
        }
    }

    private fun showDetailDialog(records: List<MeditationRecord>) {
        val dialogBinding = DialogMeditationRecordDetailBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dialogBinding.detailDateText.text = "명상일자: ${dateFormat.format(records[0].date)}"

        val detailAdapter = MeditationRecordDetailAdapter()
        dialogBinding.detailRecyclerView.apply {
            adapter = detailAdapter
            layoutManager = LinearLayoutManager(context)
        }
        detailAdapter.submitList(records)

        dialogBinding.detailCloseButton.setOnClickListener { 
            dialog.dismiss()
        }

        dialog.setOnDismissListener { viewModel.onDetailDialogDismissed() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}