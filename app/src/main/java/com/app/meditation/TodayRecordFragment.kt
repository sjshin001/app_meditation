package com.app.meditation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.meditation.databinding.DialogDailyRecordDetailBinding
import com.app.meditation.databinding.FragmentTodayRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TodayRecordFragment : Fragment() {

    private var _binding: FragmentTodayRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BowViewModel by activityViewModels()
    private lateinit var recordAdapter: DailyRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        recordAdapter = DailyRecordAdapter { summary ->
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
        
        viewModel.selectedDailyRecords.observe(viewLifecycleOwner) { records ->
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

    private fun showDetailDialog(records: List<DailyRecord>) {
        val dialogBinding = DialogDailyRecordDetailBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dialogBinding.detailDateText.text = "운동일자: ${dateFormat.format(records[0].date)}"

        val detailAdapter = DailyRecordDetailAdapter()
        dialogBinding.detailRecyclerView.apply {
            adapter = detailAdapter
            layoutManager = LinearLayoutManager(context)
        }
        detailAdapter.submitList(records)

        dialogBinding.detailCloseButton.setOnClickListener { 
            dialog.dismiss()
            viewModel.onDetailDialogDismissed()
        }

        dialog.setOnDismissListener { viewModel.onDetailDialogDismissed() }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}