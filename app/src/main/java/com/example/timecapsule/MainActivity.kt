package com.example.timecapsule

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: CapsuleAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var tvSummary: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val countdownRunnable = object : Runnable {
        override fun run() {
            adapter.refreshCountdowns()
            handler.postDelayed(this, 1000)
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvSummary = findViewById(R.id.tvSummary)

        adapter = CapsuleAdapter(
            onItemClick = { capsule ->
                val intent = Intent(this, CapsuleDetailActivity::class.java)
                intent.putExtra("capsule_id", capsule.id)
                startActivity(intent)
            },
            onDeleteClick = { capsule -> confirmDelete(capsule) }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, CreateCapsuleActivity::class.java))
        }

        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        databaseHelper.unlockExpiredCapsules()   // unlock anything overdue
        loadCapsules()
        handler.post(countdownRunnable)          // start live countdowns
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(countdownRunnable)
    }

    private fun loadCapsules() {
        val capsules = databaseHelper.getAllCapsules()
        adapter.updateCapsules(capsules)

        val sealed = capsules.count { it.isLocked }
        val opened = capsules.size - sealed
        tvSummary.text = "$sealed sealed  •  $opened opened"

        tvEmpty.visibility = if (capsules.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDelete(capsule: Capsule) {
        AlertDialog.Builder(this)
            .setTitle("Delete capsule")
            .setMessage("Delete \"${capsule.title}\" forever? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                CapsuleReceiver.cancelUnlockAlarm(this, capsule.id)
                databaseHelper.deleteCapsule(capsule.id)
                loadCapsules()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}