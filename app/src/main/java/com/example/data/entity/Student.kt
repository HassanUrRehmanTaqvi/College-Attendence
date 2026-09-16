package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["rollNo", "classKey"], unique = true),
        Index(value = ["name"]),
        Index(value = ["contactNo"])
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rollNo: Int,
    val name: String,
    val fatherName: String,
    val contactNo: String,
    val className: String, // e.g. "گیارہویں (1st Year)"
    val classKey: String,  // e.g. "11th_class" or "12th_class"
    val session: String,   // e.g. "2026-2028"
    val isOriginal: Boolean = true,
    val phoneUpdatedAt: Long? = null
)
