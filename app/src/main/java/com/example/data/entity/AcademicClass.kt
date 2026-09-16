package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_classes")
data class AcademicClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,         // e.g. "11th Islamiat"
    val grade: String,        // e.g. "گیارہویں (1st Year)"
    val section: String,      // e.g. "سیکشن A"
    val subject: String,      // e.g. "اسلامیات"
    val session: String,      // e.g. "2026-2028"
    val createdAt: Long = System.currentTimeMillis()
)
