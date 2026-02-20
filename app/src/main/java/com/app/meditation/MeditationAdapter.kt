package com.app.meditation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.meditation.databinding.ItemMeditationBinding

class MeditationAdapter(
    private val onEditClick: (Meditation) -> Unit,
    private val onItemClick: (Meditation) -> Unit
) : RecyclerView.Adapter<MeditationAdapter.MeditationViewHolder>() {

    private var meditations = listOf<Meditation>()

    fun submitList(list: List<Meditation>) {
        meditations = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeditationViewHolder {
        val binding = ItemMeditationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeditationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeditationViewHolder, position: Int) {
        holder.bind(meditations[position])
    }

    override fun getItemCount() = meditations.size

    inner class MeditationViewHolder(private val binding: ItemMeditationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meditation: Meditation) {
            binding.meditationNameText.text = meditation.name
            binding.meditationTimeText.text = "${meditation.time}분"

            binding.editButton.setOnClickListener {
                onEditClick(meditation)
            }
            itemView.setOnClickListener {
                onItemClick(meditation)
            }
        }
    }
}