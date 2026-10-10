package com.example.timecapsule

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CapsuleAdapter(
    private val onItemClick: (Capsule) -> Unit,
    private val onDeleteClick: (Capsule) -> Unit
) : RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    private var capsules: List<Capsule> = emptyList()

    fun updateCapsules(newList: List<Capsule>) {
        capsules = newList
        notifyDataSetChanged()
    }

    // Called every second by MainActivity so countdowns stay live
    fun refreshCountdowns() {
        notifyDataSetChanged()
    }

    inner class CapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIcon: TextView = itemView.findViewById(R.id.tvLockIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvCapsuleTitle)
        val tvCountdown: TextView = itemView.findViewById(R.id.tvCountdown)
        val tvCreated: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvCapsuleStatus)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_capsule, parent, false)
        return CapsuleViewHolder(view)
    }

    override fun getItemCount(): Int = capsules.size

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val capsule = capsules[position]

        holder.tvTitle.text = capsule.title
        holder.tvCreated.text = "Sealed on ${DateUtils.formatDate(capsule.createdDate)}"

        val pill = GradientDrawable().apply { cornerRadius = 40f }

        if (capsule.isLocked) {
            holder.tvIcon.text = "🔒"
            holder.tvStatus.text = "SEALED"
            pill.setColor(0x26B7791F)
            holder.tvStatus.setTextColor(0xFFB7791F.toInt())
            holder.tvCountdown.text = DateUtils.countdownText(capsule.unlockDate)
            holder.tvCountdown.setTextColor(0xFFB7791F.toInt())
        } else {
            holder.tvIcon.text = "💌"
            holder.tvStatus.text = "OPENED"
            pill.setColor(0x262E7D32)
            holder.tvStatus.setTextColor(0xFF2E7D32.toInt())
            holder.tvCountdown.text = "Tap to read your letter"
            holder.tvCountdown.setTextColor(0xFF2E7D32.toInt())
        }
        holder.tvStatus.background = pill

        holder.itemView.setOnClickListener { onItemClick(capsule) }
        holder.btnDelete.setOnClickListener { onDeleteClick(capsule) }
    }
}