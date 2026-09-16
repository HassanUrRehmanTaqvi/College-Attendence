package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class TeacherProfile(
    @PrimaryKey
    val id: Long = 1,
    val name: String,             // e.g. "حسن الرحمٰن تقوی"
    val department: String,       // e.g. "اسلامیات"
    val pinCode: String? = null,
    val isAppLockEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDisplayName: String
        get() = if (name.startsWith("پروفیسر")) name else "پروفیسر $name صاحب"
}
