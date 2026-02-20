package com.app.meditation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.ItemDailyRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DailyRecordAdapter(private val onItemClicked: (DailyRecordSummary) -> Unit) :
    ListAdapter<DailyRecordSummary, DailyRecordAdapter.RecordViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        RecordViewHolder {
        val binding = ItemDailyRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val summary = getItem(position)
        holder.bind(summary)
        holder.itemView.setOnClickListener { onItemClicked(summary) }
    }

    class RecordViewHolder(private val binding: ItemDailyRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(summary: DailyRecordSummary) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.recordNoText.text = (adapterPosition + 1).toString()
            binding.recordDateText.text = dateFormat.format(summary.date)
            binding.recordCountText.text = summary.total_count.toString()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DailyRecordSummary>() {
            override fun areItemsTheSame(oldItem: DailyRecordSummary, newItem: DailyRecordSummary):
                Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: DailyRecordSummary, newItem: DailyRecordSummary):
                Boolean {
                return oldItem == newItem
            }
        }
    }
}