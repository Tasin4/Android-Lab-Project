package com.example.timecapsule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class CreateCapsuleActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var selectedUnlockDate: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_capsule)

        databaseHelper = DatabaseHelper(this)

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etMessage = findViewById<TextInputEditText>(R.id.etMessage)
        val btnPickDate = findViewById<Button>(R.id.btnPickDate)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDate)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnPickDate.setOnClickListener { pickUnlockDate(tvSelectedDate) }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()

            when {
                title.isEmpty() -> etTitle.error = "Give your capsule a title"
                message.isEmpty() -> etMessage.error = "Write a message for your future self"
                selectedUnlockDate == -1L ->
                    Toast.makeText(this, "Please pick an unlock date first 📅", Toast.LENGTH_SHORT).show()
                else -> {
                    val capsuleId = databaseHelper.insertCapsule(title, message, selectedUnlockDate)
                    CapsuleReceiver.scheduleUnlockAlarm(this, capsuleId, title, selectedUnlockDate)
                    Toast.makeText(this, "Capsule sealed! See you in the future 🔒", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun pickUnlockDate(tvSelectedDate: TextView) {
        val now = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val chosen = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (chosen.timeInMillis <= System.currentTimeMillis()) {
                            Toast.makeText(this, "The unlock date must be in the future!", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }

                        selectedUnlockDate = chosen.timeInMillis
                        tvSelectedDate.text = "Opens on: ${DateUtils.formatDateTime(selectedUnlockDate)}"
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
    }
}