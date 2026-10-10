package com.example.timecapsule

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class CapsuleDetailActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var capsuleId: Long = -1L

    private lateinit var tvIcon: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCountdown: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvDates: TextView
    private lateinit var cardMessage: MaterialCardView

    private var isLocked = false
    private var unlockDate = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val countdownRunnable = object : Runnable {
        override fun run() {
            if (!isLocked) return
            val remaining = unlockDate - System.currentTimeMillis()
            if (remaining <= 0) {
                databaseHelper.unlockCapsule(capsuleId)
                Toast.makeText(this@CapsuleDetailActivity, "🎁 The capsule just unlocked!", Toast.LENGTH_SHORT).show()
                loadCapsule()
            } else {
                tvCountdown.text = "Opens in ${DateUtils.formatDuration(remaining)}"
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capsule_detail)

        databaseHelper = DatabaseHelper(this)
        capsuleId = intent.getLongExtra("capsule_id", -1L)

        tvIcon = findViewById(R.id.tvDetailIcon)
        tvTitle = findViewById(R.id.tvDetailTitle)
        tvStatus = findViewById(R.id.tvDetailStatus)
        tvCountdown = findViewById(R.id.tvDetailCountdown)
        tvMessage = findViewById(R.id.tvDetailMessage)
        tvDates = findViewById(R.id.tvDetailDates)
        cardMessage = findViewById(R.id.cardMessage)

        loadCapsule()
    }

    private fun loadCapsule() {
        val capsule = databaseHelper.getCapsuleById(capsuleId)
        if (capsule == null) {
            finish()
            return
        }

        isLocked = capsule.isLocked
        unlockDate = capsule.unlockDate
        tvTitle.text = capsule.title

        if (capsule.isLocked) {
            tvIcon.text = "🔒"
            tvStatus.text = "This capsule is still sealed.\nIt opens on ${DateUtils.formatDateTime(capsule.unlockDate)}."
            tvCountdown.visibility = View.VISIBLE
            cardMessage.visibility = View.GONE
            tvDates.text = "Sealed on ${DateUtils.formatDateTime(capsule.createdDate)}"
            handler.post(countdownRunnable)
        } else {
            tvIcon.text = "💌"
            tvStatus.text = "This capsule has been opened!"
            tvCountdown.visibility = View.GONE
            cardMessage.visibility = View.VISIBLE
            tvMessage.text = capsule.message

            val openedText =
                if (capsule.openedDate > 0) DateUtils.formatDateTime(capsule.openedDate)
                else DateUtils.formatDateTime(System.currentTimeMillis())
            tvDates.text = "Sealed on ${DateUtils.formatDateTime(capsule.createdDate)}\nOpened on $openedText"

            // Soft fade-in reveal of the letter
            tvMessage.alpha = 0f
            tvMessage.animate().alpha(1f).setDuration(1500).start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(countdownRunnable)
    }
}