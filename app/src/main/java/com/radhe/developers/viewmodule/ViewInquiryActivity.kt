package com.radhe.developers.viewmodule

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.radhe.developers.R
import com.radhe.developers.addModule.FollowupData
import com.radhe.developers.addModule.InquiryModel
import com.radhe.developers.databinding.ActivityViewInquiryBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewInquiryActivity : AppCompatActivity(),InquiryAdapter.InquiryItemClickListener {
    private lateinit var binding: ActivityViewInquiryBinding
    private lateinit var inquiries: ArrayList<InquiryModel>
    private lateinit var adapter: InquiryAdapter
    private val CREATE_FILE_REQUEST_CODE = 123
    private var selectedCategories = ""
    private var selectedCategorieList: List<String> = emptyList()
    private val categories = listOf("2 BHK", "3 BHK", "SHOP","Other")
    private val category = listOf("Select Category","2 BHK", "3 BHK", "SHOP","Other")
    val castList = arrayOf("Select Cast","Patel","Suthar","Soni","Brahman","Vaniya","Lohana","Other")
    val referenceMediaList = arrayOf("Select Reference Media","Mouth publicity","Newspaper","Holding banner","Social media","Radio","Other")
    private var selectedCategoriesForEdit = ""
    private var selectedCategorieListForEdit: List<String> = emptyList()


    private var searchText: String = ""
    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inquiries =ArrayList()
        binding.inquiryList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = InquiryAdapter(inquiries, this,this)
        binding.inquiryList.adapter = adapter
        binding.inquiryList.setHasFixedSize(true)
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s.toString().trim()
                getInquires(searchText)
            }
            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
        binding.download.setOnClickListener {
            openFilePicker()
        }
        binding.filter.setOnClickListener {
            openFilterPicker()
        }
        getAllInquires()
    }


    private var filteredInquiries: MutableList<InquiryModel> = mutableListOf()

    private fun getInquires(searchText: String = "") {
        val tempFilteredInquiries = if (searchText.isNotEmpty()) {
            inquiries.filter { inquiry ->
                inquiry.clientName.contains(searchText, ignoreCase = true) ||
                        inquiry.clientNumber.contains(searchText, ignoreCase = true)
            }
        } else {
            inquiries.toList()
        }

        adapter.updateData(tempFilteredInquiries)
        checkIfListEmpty()
    }



    /*private fun getInquires(searchText: String = "") {
        val progressAlertDialog: AlertDialog = AlertDialog.Builder(this).create()
        progressAlertDialog.show()
        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val inquiryRef = firebaseDatabase.getReference("inquiry")

        val query = if (searchText.isNotEmpty()) {
            inquiryRef.orderByChild("clientName").startAt(searchText).endAt(searchText + "\uf8ff")
        } else {
            inquiryRef
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                inquiries.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val inquiryModel = data.getValue(InquiryModel::class.java)
                        inquiryModel?.let {
                            inquiries.add(it)
                        }
                    }
                    progressAlertDialog.dismiss()
                    binding.inquiryList.adapter!!.notifyDataSetChanged()
                    checkIfListEmpty()

                } else {
                    binding.inquiryList.adapter!!.notifyDataSetChanged()
                    progressAlertDialog.dismiss()
                    checkIfListEmpty()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressAlertDialog.dismiss()
            }
        })
    }*/

    private fun openFilterPicker() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.filter_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        var categorySpinner = dialogView.findViewById<Spinner>(R.id.filter_spinner)
        var castSpinner = dialogView.findViewById<Spinner>(R.id.filter_cast)
        var referenceSpinner = dialogView.findViewById<Spinner>(R.id.filter_reference_media)
        var startDate = dialogView.findViewById<TextView>(R.id.start_date)
        var endDate = dialogView.findViewById<TextView>(R.id.end_date)
        var filterButton = dialogView.findViewById<Button>(R.id.btn_filter)
        var cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        var resetButton = dialogView.findViewById<Button>(R.id.btn_reset)

        // Set up category spinner
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            category
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Set up reference media spinner
        val referenceMediaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, referenceMediaList)
        referenceMediaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        referenceSpinner.adapter = referenceMediaAdapter

        // Set up cast spinner
        val castAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, castList)
        castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        castSpinner.adapter = castAdapter

        // Set up start date picker
        startDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Set the selected date in the EditText
                   startDate.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        endDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Set the selected date in the EditText
                   endDate.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        filterButton.setOnClickListener {
            val selectedCategory = categorySpinner.selectedItem.toString()
            val selectedCast = castSpinner.selectedItem.toString()
            val selectedReferenceMedia = referenceSpinner.selectedItem.toString()
            var selectedStartDate = startDate.text.toString()
            var  selectedEndDate = endDate.text.toString()

            applyFilter(selectedCategory, selectedCast, selectedReferenceMedia, selectedStartDate, selectedEndDate)

            dialog.dismiss()
        }

        // Reset button click listener
        resetButton.setOnClickListener {
            categorySpinner.setSelection(0)
            castSpinner.setSelection(0)
            referenceSpinner.setSelection(0)
            startDate.text = ""
            endDate.text = ""
        }

        // Cancel button click listener
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }




    private fun applyFilter(
        selectedCategory: String,
        selectedCast: String,
        selectedReferenceMedia: String,
        selectedStartDate: String,
        selectedEndDate: String
    ) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("inquiry")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                inquiries.clear()

                for (snapshot in dataSnapshot.children.reversed()) {
                    val inquiry = snapshot.getValue(InquiryModel::class.java)
                    inquiry?.let {
                        val categoryMatch = selectedCategory.isEmpty() || it.categoryName.contains(selectedCategory)
                        val castMatch = selectedCast.isEmpty() || it.cast.contains(selectedCast)
                        val referenceMediaMatch = selectedReferenceMedia.isEmpty() || it.clientRMedia.contains(selectedReferenceMedia)
                        if (selectedStartDate.equals("Tap and Select") || selectedEndDate.equals("Tap and Select")) {
                            if (categoryMatch || castMatch || referenceMediaMatch) {
                                inquiries.add(it)
                            }
                        }else{
                            if (selectedStartDate.equals("Tap and Select")){
                                val dateMatch = isDateInRange(it.followupDetails, selectedEndDate, selectedEndDate)
                                if (categoryMatch || castMatch || referenceMediaMatch || dateMatch) {
                                    inquiries.add(it)
                                }
                            }else if (selectedEndDate.equals("Tap and Select")){
                                val dateMatch = isDateInRange(it.followupDetails, selectedStartDate, selectedStartDate)
                                if (categoryMatch || castMatch || referenceMediaMatch || dateMatch) {
                                    inquiries.add(it)
                                }
                            }else{
                                val dateMatch = isDateInRange(it.followupDetails, selectedStartDate, selectedEndDate)
                                if (categoryMatch || castMatch || referenceMediaMatch || dateMatch) {
                                    inquiries.add(it)
                                }
                            }

                        }
                    }
                }
                adapter.notifyDataSetChanged()
                checkIfListEmpty()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ViewInquiryActivity, "Error retrieving data $databaseError", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isDateInRange(followupDetails: List<FollowupData>, startDate: String, endDate: String): Boolean {
        // If no date range is specified, return true
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return true
        }
        // Convert date strings to Date objects for comparison
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDateObj = dateFormat.parse(startDate)
        val endDateObj = dateFormat.parse(endDate)

        // Iterate through followupDetails to check if any followUpDate falls within the specified date range
        for (followupData in followupDetails) {
            val followUpDateObj = dateFormat.parse(followupData.followUpDate)
            if (followUpDateObj in startDateObj..endDateObj) {
                return true
            }
        }
        return false
    }



    /* val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selection, null)
     val listView = dialogView.findViewById<ListView>(R.id.list_view)
     val adapter = CategoryAdapter(this, categories)
     listView.adapter = adapter

     val alertDialog = android.app.AlertDialog.Builder(this)
         .setView(dialogView)
         .setPositiveButton("OK") { _, _ ->
             selectedCategories = adapter.getCheckedItems().joinToString(",")
             selectedCategorieList = adapter.getCheckedItems()
             filterDataByCategories(selectedCategorieList)
         }
         .setNegativeButton("Cancel", null)
         .create()

     alertDialog.show()*/


    private fun filterDataByCategories(selectedCategories: List<String>) {
        val progressAlertDialog: AlertDialog = AlertDialog.Builder(this).create()
        progressAlertDialog.show()

        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val inquiryRef = firebaseDatabase.getReference("inquiry")

        inquiryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                inquiries.clear()
                for (data in snapshot.children.reversed()) {
                    val inquiryModel = data.getValue(InquiryModel::class.java)

                    // Check if inquiryModel exists and contains all selected categories
                    if (inquiryModel != null && inquiryModel.categories.intersect(selectedCategories).isNotEmpty()) {
                        inquiries.add(inquiryModel)
                    }
                }

                if (inquiries.isNotEmpty()) {
                    progressAlertDialog.dismiss()
                    adapter.notifyDataSetChanged()
                    binding.noData.visibility = View.GONE
                    binding.inquiryList.visibility = View.VISIBLE
                } else {
                    progressAlertDialog.dismiss()
                    binding.inquiryList.visibility = View.GONE
                    binding.noData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Error fetching inquiries", error.toException())
            }
        })
    }

    private fun getAllInquires() {
        val progressAlertDialog: AlertDialog = AlertDialog.Builder(this).create()
        progressAlertDialog.show()
        var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        var inquiryRef = firebaseDatabase.getReference("inquiry")
        inquiryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                inquiries.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children.reversed()) {
                        var inquiryModel = data.getValue(InquiryModel::class.java)
                        inquiries.add(inquiryModel!!)
                    }
                    progressAlertDialog.dismiss()
                    binding.totalRecords.setText(inquiries.size.toString())
                    adapter.notifyDataSetChanged()
                    binding.noData.visibility = View.GONE
                } else {
                    progressAlertDialog.dismiss()
                    adapter.notifyDataSetChanged()
                    binding.noData.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkIfListEmpty() {
        if (inquiries.isEmpty()) {
            binding.inquiryList.visibility = View.GONE
            binding.noData.visibility = View.VISIBLE
        } else {
            binding.inquiryList.visibility = View.VISIBLE
            binding.noData.visibility = View.GONE
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "Radhe_Developers_Inquiry.csv")
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                saveCSVToFile(uri)
            }
        }
    }

    private fun saveCSVToFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->

                outputStream.write("Name,Number,Address,Email,Cast,Reference Name,Reference Media,Category,Details,Inquiry By,Follow-up Note,Follow-up Date,Follow-up Status,Rate\n \n".toByteArray())

                // Write CSV data
                for (userData in inquiries) {
                    val lastFollowupDetails = userData.followupDetails.lastOrNull()
                    val csvLine = if (lastFollowupDetails != null) {
                        "${userData.clientName},${userData.clientNumber},${userData.address},${userData.clientEmail},${userData.cast},${userData.clientReference},${userData.clientRMedia},${userData.categoryName}," +
                                "${lastFollowupDetails.details},${lastFollowupDetails.inquiryBy},${lastFollowupDetails.followupNote},${lastFollowupDetails.followUpDate}," +
                                "${lastFollowupDetails.followUpStatus},${lastFollowupDetails.ratting}\n"
                    } else {
                        "${userData.clientName},${userData.clientNumber},${userData.address},${userData.clientEmail},${userData.cast},${userData.clientReference},${userData.clientRMedia},${userData.categoryName},,,,,,,,,\n"
                    }

                    outputStream.write(csvLine.toByteArray())
                }
                Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save CSV file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInquiryItemClick(inquiry: InquiryModel) {
        openDetailsDialog(inquiry)
    }

    override fun onInquiryItemDelete(inquiry: InquiryModel) {
        deleteCategoryDialog(inquiry)
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
                    getAllInquires()
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
        var rattingValue = "0.0"
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

            val email = if(textEmail.text.isEmpty()) "-" else textEmail.text.toString()
            val rName = if(textRName.text.isEmpty()) "-" else textRName.text.toString()
            val addressFor  = if(textAddress.text.isEmpty()) "-" else textAddress.text.toString()
            val details = if(textDetails.text.isEmpty()) "-" else textDetails.text.toString()


            if (textName.text.isEmpty() ||
                textNumber.text.isEmpty() ||
                textFollowupDate.text.equals("Select Follow-up Date") ||
                textFollowupNote.text.isEmpty() ||
                updateSpinnerCast.selectedItem.toString().equals("Select Cast") ||
                updateSpinnerRMedia.selectedItem.toString().equals("Select Reference Media") ||
                textInquiryBy.text.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
                val updatedFollowupData = FollowupData(
                    details = details,
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
                            clientEmail = email
                            cast = updateSpinnerCast.selectedItem.toString()
                            clientReference = rName
                            clientRMedia = updateSpinnerRMedia.selectedItem.toString()
                            categories = selectedCategorieListForEdit
                            categoryName = selectedCategoriesForEdit
                            address = addressFor

                            followupDetails = (followupDetails + updatedFollowupData).toMutableList()
                        }

                        // Save the updated InquiryModel back to Firebase
                        inquiryRef.setValue(existingInquiry)
                            .addOnSuccessListener {
                                Toast.makeText(this@ViewInquiryActivity, "Record updated successfully", Toast.LENGTH_SHORT).show()
                                getAllInquires()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ViewInquiryActivity, "Failed to update record", Toast.LENGTH_SHORT).show()
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