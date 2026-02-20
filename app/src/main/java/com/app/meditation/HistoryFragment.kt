package com.app.meditation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.FragmentHistoryBinding
import com.app.meditation.databinding.ItemProgressDayBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by activityViewModels()
    private lateinit var gridAdapter: ProgressGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        gridAdapter = ProgressGridAdapter { day ->
            val round = viewModel.allRounds.value?.find { it.id == viewModel.selectedRoundId.value }?.round ?: 0
            val action = HistoryFragmentDirections.actionHistoryFragmentToProgressRecordFragment(round, day)
            findNavController().navigate(action)
        }
        binding.progressGridRecycler.adapter = gridAdapter
    }

    private fun observeViewModel() {
        viewModel.allRounds.observe(viewLifecycleOwner) { rounds ->
            val roundNumbers = rounds.map { "${it.round}회차" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roundNumbers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.roundSpinner.adapter = adapter

            if (rounds.isNotEmpty()) {
                val latestRoundId = rounds.first().id
                if (viewModel.selectedRoundId.value != latestRoundId) {
                    viewModel.setSelectedRound(latestRoundId)
                }
                binding.roundSpinner.setSelection(0)
            }
        }

        viewModel.recordsForSelectedRound.observe(viewLifecycleOwner) {
            gridAdapter.submitList(it)
        }
    }

    private fun setupClickListeners() {
        binding.addRoundButton.setOnClickListener {
            showAddRoundConfirmationDialog()
        }

        binding.deleteRoundButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.roundSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.allRounds.value?.get(position)?.id?.let {
                    viewModel.setSelectedRound(it)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showAddRoundConfirmationDialog() {
        val lastRound = viewModel.allRounds.value?.firstOrNull()?.round ?: 0
        val nextRound = lastRound + 1
        AlertDialog.Builder(requireContext())
            .setTitle("회차 추가")
            .setMessage("${nextRound}회차를 추가하시겠습니까?")
            .setPositiveButton("추가") { _, _ ->
                viewModel.addRound()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        val selectedRound = viewModel.allRounds.value?.find { it.id == viewModel.selectedRoundId.value }
        val roundNumber = selectedRound?.round ?: 0

        AlertDialog.Builder(requireContext())
            .setTitle("회차 삭제")
            .setMessage("${roundNumber}회차를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteCurrentRound()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProgressGridAdapter(private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ProgressGridAdapter.DayViewHolder>() {

    private var records: Map<Int, ProgressRecord> = mapOf()
    private val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    fun submitList(list: List<ProgressRecord>) {
        records = list.associateBy { it.day }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemProgressDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = position + 1
        holder.bind(day, records[day])
    }

    override fun getItemCount() = 100 // Always 100 days

    inner class DayViewHolder(private val binding: ItemProgressDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Int, record: ProgressRecord?) {
            binding.dayNumberText.text = day.toString()
            if (record != null && record.date != null) {
                binding.dayDateText.visibility = View.VISIBLE
                binding.dayDateText.text = dateFormat.format(record.date!!)
            } else {
                binding.dayDateText.visibility = View.GONE
            }
            itemView.setOnClickListener { onItemClick(day) }
        }
    }
}