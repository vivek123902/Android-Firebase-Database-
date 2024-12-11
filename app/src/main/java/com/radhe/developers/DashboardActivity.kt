package com.radhe.developers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.radhe.developers.addModule.AddInquiryActivity
import com.radhe.developers.databinding.ActivityDashboardBinding
import com.radhe.developers.viewmodule.FutureFollowupActivity
import com.radhe.developers.viewmodule.TodayFollowupActivity
import com.radhe.developers.viewmodule.ViewInquiryActivity

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addForm.setOnClickListener {
            startActivity(Intent(this,AddInquiryActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        binding.viewInquiry.setOnClickListener {
            startActivity(Intent(this, ViewInquiryActivity::class.java))
                   overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        binding.todays.setOnClickListener {
            startActivity(Intent(this, TodayFollowupActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        binding.future.setOnClickListener {
            startActivity(Intent(this, FutureFollowupActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        binding.erase.setOnClickListener {
            startActivity(Intent(this, EraseAllActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Are you sure you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, which ->
                finishAffinity()
                finish()
                System.exit(0)
                dialog.dismiss()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
    }
}