package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["classId"]),
        Index(value = ["date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val classId: Long,
    val date: String,             // Format "yyyy-MM-dd"
    val status: String,           // "PRESENT" or "ABSENT"
    val notes: String? = null
)
