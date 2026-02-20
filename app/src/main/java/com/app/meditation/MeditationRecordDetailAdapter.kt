package com.app.meditation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.ItemMeditationRecordDetailBinding

class MeditationRecordDetailAdapter : ListAdapter<MeditationRecord, MeditationRecordDetailAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeditationRecordDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMeditationRecordDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: MeditationRecord) {
            binding.durationText.text = "${record.duration}분"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MeditationRecord>() {
            override fun areItemsTheSame(oldItem: MeditationRecord, newItem: MeditationRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MeditationRecord, newItem: MeditationRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}