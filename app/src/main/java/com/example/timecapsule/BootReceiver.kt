package com.example.timecapsule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Alarms are cleared on reboot, so schedule them all again
        val db = DatabaseHelper(context)
        db.getAllCapsules().filter { it.isLocked }.forEach {
            CapsuleReceiver.scheduleUnlockAlarm(context, it.id, it.title, it.unlockDate)
        }
    }
}