package com.radhe.developers.viewmodule

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.radhe.developers.R
import com.radhe.developers.addModule.FollowupData
import com.radhe.developers.addModule.InquiryModel
import com.radhe.developers.databinding.ActivityTodayFollowupBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodayFollowupActivity : AppCompatActivity(), InquiryAdapter.InquiryItemClickListener  {
    private lateinit var inquiries: ArrayList<InquiryModel>
    lateinit var binding: ActivityTodayFollowupBinding
    private lateinit var adapter: InquiryAdapter
    private var selectedCategoriesForEdit = ""
    private val categories = listOf("2 BHK", "3 BHK", "SHOP","Other")
    private var selectedCategorieListForEdit: List<String> = emptyList()
    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayFollowupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        inquiries = ArrayList()

        binding.todaysFollowupList.setHasFixedSize(true)
        binding.todaysFollowupList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = InquiryAdapter(inquiries, this,this)
        binding.todaysFollowupList.adapter = adapter

        getAllTodaysInquires()
    }
    private fun getAllTodaysInquires() {
        val progressAlertDialog: AlertDialog = AlertDialog.Builder(this).create()
        progressAlertDialog.show()
        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val inquiryRef = firebaseDatabase.getReference("inquiry")
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.currentDate.text = "Date : $currentDate"
        inquiryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.reversed().forEach { data ->
                    val inquiryModel = data.getValue(InquiryModel::class.java)
                    if (inquiryModel != null) {
                        // Iterate over the followup details of the inquiry
                        val followupDetails = inquiryModel.followupDetails
                        var hasMatchingFollowupDate = false
                        for (followupData in followupDetails) {
                            if (followupData.followUpDate == currentDate) {
                                hasMatchingFollowupDate = true
                                break
                            }
                        }
                        if (hasMatchingFollowupDate) {
                            inquiries.add(inquiryModel)
                        }
                    }
                }
                progressAlertDialog.dismiss()
                if (inquiries.isNotEmpty()) {
                   adapter.notifyDataSetChanged()
                    binding.noData.visibility = View.GONE
                } else {
                    adapter.notifyDataSetChanged()
                    binding.noData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    override fun onInquiryItemClick(inquiry: InquiryModel) {
        openDetailsDialog(inquiry)
    }
    override fun onInquiryItemDelete(inquiry: InquiryModel) {
        deleteCategoryDialog(inquiry)
    }
    private fun checkIfListEmpty() {
        if (inquiries.isEmpty()) {
            binding.noData.visibility = View.VISIBLE
        } else {
            binding.noData.visibility = View.GONE
        }
    }
    private fun deleteCategoryDialog(inquiry: InquiryModel) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Confirmation")
        alertDialogBuilder.setMessage("Are you sure you want to delete ${inquiry.clientName}?")

        alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            // Delete the inquiry
            val inquiryRef = FirebaseDatabase.getInstance().getReference("inquiry").child(inquiry.inquiryId)
            inquiryRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Inquiry deleted successfully", Toast.LENGTH_SHORT).show()
                    getAllTodaysInquires()
                    checkIfListEmpty()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete inquiry", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun openDetailsDialog(inquiry: InquiryModel) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.update_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        var selectedCast = ""
        var selectedRMedia = ""
        var followupStatus = ""
        var followupDate = ""
        var rattingValue = ""
        val textName = dialogView.findViewById<EditText>(R.id.et_update_name)
        val textNumber = dialogView.findViewById<EditText>(R.id.et_update_number)
        val textEmail = dialogView.findViewById<EditText>(R.id.et_update_email)
        val textRName = dialogView.findViewById<EditText>(R.id.et_update_rname)
        val textAddress = dialogView.findViewById<EditText>(R.id.et_update_address)
        val textCategory = dialogView.findViewById<TextView>(R.id.update_category)
        val textInquiryBy = dialogView.findViewById<EditText>(R.id.et_update_inquiryBy)
        val textFollowupDate = dialogView.findViewById<TextView>(R.id.update_followup_date)
        textFollowupDate.setText("Select Follow-up Date")
        val textFollowupNote = dialogView.findViewById<EditText>(R.id.et_update_followup_note)
        val textFollowupStatus = dialogView.findViewById<Spinner>(R.id.update_spinner_followup_status)
        val updateSpinnerCast = dialogView.findViewById<Spinner>(R.id.update_spinnerCast)
        val updateSpinnerRMedia = dialogView.findViewById<Spinner>(R.id.spinnerRMedia)
        val textDetails = dialogView.findViewById<EditText>(R.id.et_update_details)
        var ratting = dialogView.findViewById<RatingBar>(R.id.brosure)

        val followUpOptions = arrayOf( "No further follow up required","Follow up created")
        val followUpAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, followUpOptions)
        followUpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        textFollowupStatus.adapter = followUpAdapter
        textFollowupStatus.setSelection(0)
        textFollowupStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                followupStatus = followUpOptions[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        val castList =arrayOf("Select Cast","Patel","Suthar","Soni","Brahman","Vaniya","Lohana","Other")
        val castAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, castList)
        castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        updateSpinnerCast.adapter = castAdapter
        val statusIndex = castList.indexOf(inquiry.cast)
        if (statusIndex != -1) {
            updateSpinnerCast.setSelection(statusIndex)
        }
        updateSpinnerCast.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCast = castList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        ratting.setOnRatingBarChangeListener { ratingBar, fl, b ->
            rattingValue = fl.toString()
            ratting.rating  = fl
        }
        textName.setText(inquiry.clientName)
        textNumber.setText(inquiry.clientNumber)
        textEmail.setText(inquiry.clientEmail)
        textRName.setText(inquiry.clientReference)
        textAddress.setText(inquiry.address)
        textCategory.text = inquiry.categoryName
        selectedCategorieListForEdit = inquiry.categories
        selectedCategoriesForEdit = inquiry.categoryName
        val rMediaList =arrayOf("Select Reference Media","Mouth publicity","Newspaper","Holding banner","Social media","Radio","Other")
        val rMediaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rMediaList)
        rMediaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        updateSpinnerRMedia.adapter = rMediaAdapter
        val statusIndexForMedia = rMediaList.indexOf(inquiry.clientRMedia)
        if (statusIndexForMedia != -1) {
            updateSpinnerRMedia.setSelection(statusIndexForMedia)
        }
        updateSpinnerRMedia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRMedia = rMediaList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        textCategory.setOnClickListener {
            val dialogViewForCategory = LayoutInflater.from(this).inflate(R.layout.dialog_category_selection, null)
            val listView = dialogViewForCategory.findViewById<ListView>(R.id.list_view)
            val adapter = UpdatedCategoryAdapter(this, categories,inquiry.categories)
            listView.adapter = adapter
            val alertDialog = android.app.AlertDialog.Builder(this)
                .setView(dialogViewForCategory)
                .setPositiveButton("OK") { _, _ ->
                    selectedCategoriesForEdit = adapter.getCheckedItems().joinToString(",")
                    selectedCategorieListForEdit = adapter.getCheckedItems()
                    textCategory.setText(selectedCategoriesForEdit)

                }
                .setNegativeButton("Cancel", null)
                .create()

            alertDialog.show()
        }
        textFollowupDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Set the selected date in the EditText
                    textFollowupDate.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
        dialogView.findViewById<Button>(R.id.btn_update).setOnClickListener {

            if (textName.text.isEmpty() ||
                textNumber.text.isEmpty() ||
                textEmail.text.isEmpty() ||
                textRName.text.isEmpty() ||
                textAddress.text.isEmpty() ||
                textFollowupDate.text.equals("Select Follow-up Date") ||
                textFollowupNote.text.isEmpty() ||
                textDetails.text.isEmpty() ||
                updateSpinnerCast.selectedItem.toString().equals("Select Cast") ||
                updateSpinnerRMedia.selectedItem.toString().equals("Select Reference Media") ||
                rattingValue.equals("") ||
                textInquiryBy.text.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {

                val updatedFollowupData = FollowupData(
                    details = textDetails.text.toString(),
                    followUpStatus = followupStatus,
                    followUpDate = textFollowupDate.text.toString(),
                    inquiryBy = textInquiryBy.text.toString(),
                    followupNote = textFollowupNote.text.toString(),
                    ratting = rattingValue
                )


                val inquiryRef = FirebaseDatabase.getInstance().getReference("inquiry").child(inquiry.inquiryId)
                inquiryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingInquiry = snapshot.getValue(InquiryModel::class.java)
                        existingInquiry?.apply {
                            clientName = textName.text.toString()
                            clientNumber = textNumber.text.toString()
                            clientEmail = textEmail.text.toString()
                            cast = updateSpinnerCast.selectedItem.toString()
                            clientReference = textRName.text.toString()
                            clientRMedia = updateSpinnerRMedia.selectedItem.toString()
                            categories = selectedCategorieListForEdit
                            categoryName = selectedCategoriesForEdit
                            address = textAddress.text.toString()

                            followupDetails = (followupDetails + updatedFollowupData).toMutableList()
                        }

                        // Save the updated InquiryModel back to Firebase
                        inquiryRef.setValue(existingInquiry)
                            .addOnSuccessListener {
                                Toast.makeText(this@TodayFollowupActivity, "Record updated successfully", Toast.LENGTH_SHORT).show()
                                getAllTodaysInquires()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@TodayFollowupActivity, "Failed to update record", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle onCancelled
                    }
                })
            }
        }
        dialog.show()
    }
}