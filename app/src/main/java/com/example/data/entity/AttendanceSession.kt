package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = AcademicClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["classId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["classId"])
    ]
)
data class AttendanceSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val date: String,             // Format "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val teacherName: String,
    val department: String,
    val subject: String,
    val className: String,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int
)
