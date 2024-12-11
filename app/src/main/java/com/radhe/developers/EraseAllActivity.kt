package com.radhe.developers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.radhe.developers.addModule.InquiryModel
import com.radhe.developers.databinding.ActivityEraseAllBinding
import java.io.IOException

class EraseAllActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEraseAllBinding
    private lateinit var inquiries: ArrayList<InquiryModel>
    private val CREATE_FILE_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEraseAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inquiries = ArrayList()
        getAllInquires()
        binding.btnEraseAll.setOnClickListener {
            openFilePicker()
        }
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
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "Radhe_Developers_Inquiry.csv")
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
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

                } else {
                    progressAlertDialog.dismiss()
                    Toast.makeText(this@EraseAllActivity, "Currently No Inquiry Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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
                openWarningDialog()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save CSV file", Toast.LENGTH_SHORT).show()
        }
    }
    private fun openWarningDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage("Are you sure you want to erase all data?")
            .setPositiveButton("Yes") { _, _ ->
                deleteAllDataFromFirebase()
            }
            .setNegativeButton("No") { dialog, _ ->

                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun deleteAllDataFromFirebase() {
        val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
        val inquiryRef = firebaseDatabase.getReference("inquiry")
        inquiryRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "All data deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete data", Toast.LENGTH_SHORT).show()
            }
    }

}