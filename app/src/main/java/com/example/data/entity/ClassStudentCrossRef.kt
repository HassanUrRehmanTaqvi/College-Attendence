package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "class_students",
    primaryKeys = ["classId", "studentId"],
    foreignKeys = [
        ForeignKey(
            entity = AcademicClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
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
        Index(value = ["classId"]),
        Index(value = ["studentId"])
    ]
)
data class ClassStudentCrossRef(
    val classId: Long,
    val studentId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
