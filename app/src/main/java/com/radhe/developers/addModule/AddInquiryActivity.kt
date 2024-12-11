package com.radhe.developers.addModule

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.radhe.developers.DashboardActivity
import com.radhe.developers.R
import com.radhe.developers.databinding.ActivityAddInquiryBinding
import java.util.Calendar

class AddInquiryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddInquiryBinding
    private lateinit var  inquiryRef: DatabaseReference
    private var followupStatus: String = ""
    var rate = ""
    private var cast: String = ""
    private var referenceMedia: String = ""
    private val calendar = Calendar.getInstance()
    private var selectedCategories = ""
    private var selectedCategorieList: List<String> = emptyList()


    private val categories = listOf("2 BHK", "3 BHK", "SHOP","Other")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inquiryRef = FirebaseDatabase.getInstance().getReference("inquiry")
        val castList = arrayOf("Select Cast","Patel","Suthar","Soni","Brahman","Vaniya","Lohana","Other")
        val castAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, castList)
        castAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCast.adapter = castAdapter
        binding.spinnerCast.setSelection(0)
        binding.spinnerCast.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                cast = castList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        val referenceMediaList = arrayOf("Select Reference Media","Mouth publicity","Newspaper","Holding banner","Social media","Radio","Other")
        val rMediaAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, referenceMediaList)
        rMediaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRMedia.adapter = rMediaAdapter
        binding.spinnerRMedia.setSelection(0)
        binding.spinnerRMedia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                referenceMedia = referenceMediaList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        val followUpOptions = arrayOf( "No further follow up required","Follow up created")
        val adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, followUpOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFollowupStatus.adapter = adapter
        binding.spinnerFollowupStatus.setSelection(0)
        binding.spinnerFollowupStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        binding.btnSubmit.setOnClickListener {
            if (validate()) {
                saveInquiry()
            }else
            {
                Toast.makeText(this,"Please fill all the fields",Toast.LENGTH_LONG).show()
            }
        }
        binding.followupDate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.category.setOnClickListener {
            showCategoryDialog()
        }
        binding.brosure.setOnRatingBarChangeListener { _, rating, _ ->
            rate = rating.toString()
            binding.brosure.rating = rating
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Set the selected date in the EditText
                binding.followupDate.setText(String.format("%d-%02d-%02d", year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }
    private fun saveInquiry() {
        val inquiryId =  inquiryRef.push().key
        val updatedFollowupData = FollowupData(
            details = "-",
            followUpStatus = followupStatus,
            followUpDate = binding.followupDate.text.toString(),
            inquiryBy = "N/O",
            followupNote = "Follow-up Crated while inquiry",
            ratting = "0.0"
        )

        val inquiry = InquiryModel(
            inquiryId!!,
            binding.userName.text.toString(),
            binding.etPhone.text.toString(),
            if (binding.etEmail.text.toString().isEmpty()) "-" else binding.etEmail.text.toString(),
            cast,
            if (binding.etRname.text.toString().isEmpty()) "-" else binding.etRname.text.toString(),
            referenceMedia,
            selectedCategorieList,
            selectedCategories,
            if (binding.etAddress.text.toString().isEmpty()) "-" else binding.etAddress.text.toString(),
            listOf(updatedFollowupData)
        )


        inquiryRef.child(inquiryId).setValue(inquiry)
            .addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inquiry Added", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error $it", Toast.LENGTH_LONG).show()
            }
    }

    private fun validate(): Boolean {
        if (binding.userName.text.toString().isEmpty()) {
            return false
        }
        else if(cast.equals("Select Cast")){
            Toast.makeText(this, "Please Select Cast", Toast.LENGTH_LONG).show()
            return false
        }

        else if (binding.etPhone.text.toString().isEmpty()) {
            return false
        }

        else if(referenceMedia.equals("Select Reference Media")){
            Toast.makeText(this, "Please Reference Media", Toast.LENGTH_LONG).show()
            return false
        }
        else if(binding.followupDate.text.equals("Select Follow-up Date")){
            Toast.makeText(this, "Please Select Follow-up Date", Toast.LENGTH_LONG).show()
            return false
        }
        else{
            return true
        }
    }
    private fun showCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selection, null)
        val listView = dialogView.findViewById<ListView>(R.id.list_view)
        val adapter = CategoryAdapter(this, categories)
        listView.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                selectedCategories = adapter.getCheckedItems().joinToString(",")
                selectedCategorieList = adapter.getCheckedItems()
                binding.category.setText(selectedCategories)
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }
}