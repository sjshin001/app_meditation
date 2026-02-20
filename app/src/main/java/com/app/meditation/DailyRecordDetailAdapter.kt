package com.app.meditation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.ItemDailyRecordDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailyRecordDetailAdapter : ListAdapter<DailyRecord, DailyRecordDetailAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyRecordDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemDailyRecordDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: DailyRecord) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            binding.startTimeText.text = timeFormat.format(record.date)
            
            val h = TimeUnit.MILLISECONDS.toHours(record.duration)
            val m = TimeUnit.MILLISECONDS.toMinutes(record.duration) - TimeUnit.HOURS.toMinutes(h)
            val s = TimeUnit.MILLISECONDS.toSeconds(record.duration) - TimeUnit.MINUTES.toSeconds(m) - TimeUnit.HOURS.toSeconds(h)
            val formattedDuration = String.format("%02d:%02d:%02d", h, m, s)
            binding.durationText.text = formattedDuration

            binding.countText.text = record.count.toString()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DailyRecord>() {
            override fun areItemsTheSame(oldItem: DailyRecord, newItem: DailyRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DailyRecord, newItem: DailyRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}