package com.radhe.developers.viewmodule


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.radhe.developers.addModule.InquiryModel
import com.radhe.developers.databinding.InquirylistItemBinding

class InquiryAdapter(
    private var inquiries: MutableList<InquiryModel>,
    private var context: Context,
    private val clickListener: InquiryItemClickListener
) : RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder>() {
    private var counter: Int = 1
    fun updateData(newData: List<InquiryModel>) {
        inquiries = newData.toMutableList()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InquiryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = InquirylistItemBinding.inflate(inflater, parent, false)
        return InquiryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InquiryViewHolder, position: Int) {
        val inquiry = inquiries[position]
        holder.bind(inquiry,position)

    }

    override fun getItemCount(): Int {
        return inquiries.size
    }

    inner class InquiryViewHolder(private val binding: InquirylistItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }



        fun bind(inquiry: InquiryModel,position: Int) {
            binding.srNo.text = "Sr No : ${position + 1}"
            binding.name.text = (inquiry.clientName ?: "N/A")
            binding.number.text = (inquiry.clientNumber ?: "N/A")
            binding.email.text =(inquiry.clientEmail ?: "N/A")
            binding.cast.text = (inquiry.cast ?: "N/A")
            binding.referenceName.text = (inquiry.clientReference ?: "N/A")
            binding.referenceMedia.text = (inquiry.clientRMedia ?: "N/A")
            binding.category.text = (inquiry.categories?.joinToString(separator = " | ") ?: "N/A")

            binding.address.text = (inquiry.address ?: "N/A")
            if (inquiry.followupDetails.isEmpty()) {
                binding.visited.text = "NO VISITS"
            }else {
                binding.visited.text = "Visited : " + (inquiry.followupDetails.size.toString() + " times")

            }

            binding.whatsapp.setOnClickListener {
                openWhatsAppChat(inquiry.clientNumber)
            }
            binding.call.setOnClickListener {
                callPhoneNumber(inquiry.clientNumber)
            }
            binding.delete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val inquiry = inquiries[position]
                    clickListener.onInquiryItemDelete(inquiry)
                }
            }
            binding.update.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val inquiry = inquiries[position]
                    clickListener.onInquiryItemClick(inquiry)
                }
            }
            val followupAdapter = FollowupAdapter(inquiry.followupDetails)
            binding.followupList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = followupAdapter
            }
        }

        private fun openWhatsAppChat(phoneNumber: String) {
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        private fun callPhoneNumber(phoneNumber: String) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            context.startActivity(intent)
        }

        override fun onClick(p0: View?) {

        }
    }

    interface InquiryItemClickListener {
        fun onInquiryItemClick(inquiry: InquiryModel)
        fun onInquiryItemDelete(inquiry: InquiryModel)

    }
}
