package com.example.timecapsule

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat

class CapsuleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val capsuleId = intent.getLongExtra(EXTRA_ID, -1L)
        if (capsuleId == -1L) return

        val db = DatabaseHelper(context)
        val capsule = db.getCapsuleById(capsuleId) ?: return
        if (!capsule.isLocked) return
        db.unlockCapsule(capsuleId)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Capsule Unlocks", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications when your time capsules open" }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, CapsuleDetailActivity::class.java).apply {
            putExtra("capsule_id", capsuleId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context, capsuleId.toInt(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📦 A Time Capsule just opened!")
            .setContentText("\"${capsule.title}\" is ready to be read.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("\"${capsule.title}\" is ready to be read."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapPendingIntent)
            .build()

        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            notificationManager.notify(capsuleId.toInt(), notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "capsule_unlock_channel"
        private const val EXTRA_ID = "capsule_id"
        private const val EXTRA_TITLE = "title"

        fun scheduleUnlockAlarm(context: Context, capsuleId: Long, title: String, unlockDate: Long) {
            val intent = Intent(context, CapsuleReceiver::class.java).apply {
                putExtra(EXTRA_ID, capsuleId)
                putExtra(EXTRA_TITLE, title)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, capsuleId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms()

            if (canBeExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, unlockDate, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, unlockDate, pendingIntent)
            }
        }

        fun cancelUnlockAlarm(context: Context, capsuleId: Long) {
            val intent = Intent(context, CapsuleReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, capsuleId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}