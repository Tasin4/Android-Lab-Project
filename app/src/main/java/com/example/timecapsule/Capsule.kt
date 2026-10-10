package com.example.timecapsule

data class Capsule(
    val id: Long,
    val title: String,
    val message: String,
    val createdDate: Long,
    val unlockDate: Long,
    val openedDate: Long,
    val isLocked: Boolean
)