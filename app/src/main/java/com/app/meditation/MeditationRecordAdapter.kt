package com.app.meditation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.ItemMeditationRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MeditationRecordAdapter(private val onItemClicked: (MeditationRecordSummary) -> Unit) :
    ListAdapter<MeditationRecordSummary, MeditationRecordAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeditationRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = getItem(position)
        holder.bind(summary)
        holder.itemView.setOnClickListener { onItemClicked(summary) }
    }

    class ViewHolder(private val binding: ItemMeditationRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(summary: MeditationRecordSummary) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.recordDateText.text = dateFormat.format(summary.date)
            binding.recordDurationText.text = "${summary.total_duration}분"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MeditationRecordSummary>() {
            override fun areItemsTheSame(oldItem: MeditationRecordSummary, newItem: MeditationRecordSummary): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: MeditationRecordSummary, newItem: MeditationRecordSummary): Boolean {
                return oldItem == newItem
            }
        }
    }
}