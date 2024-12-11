package com.radhe.developers.viewmodule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radhe.developers.addModule.FollowupData
import com.radhe.developers.databinding.FollowupItemBinding

class FollowupAdapter(private val followupData: List<FollowupData>) : RecyclerView.Adapter<FollowupAdapter.FollowupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FollowupItemBinding.inflate(inflater, parent, false)
        return FollowupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowupViewHolder, position: Int) {
        val followupItem = followupData[position]
        holder.bind(followupItem)
    }

    override fun getItemCount() = followupData.size

    inner class FollowupViewHolder(private val binding: FollowupItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(followupItem: FollowupData) {
            // Bind follow-up data to UI elements in the item layout
            binding.details.text = (followupItem.details ?: "N/A")
            if (followupItem.ratting == "") {
                binding.brosure.visibility = View.GONE
            }else {
                binding.brosure.rating = followupItem.ratting?.toFloat() ?: 0f
            }

            binding.inquiryby.text = (followupItem.inquiryBy ?: "N/A")
            binding.followupDate.text = (followupItem.followUpDate ?: "N/A")
            binding.followupNote.text = (followupItem.followupNote ?: "N/A")
            binding.followupStatus.text = (followupItem.followUpStatus ?: "N/A")
        }
    }
}
