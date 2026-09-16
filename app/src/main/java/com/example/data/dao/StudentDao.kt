package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classKey = :classKey ORDER BY rollNo ASC")
    fun getStudentsByClassKey(classKey: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdSync(id: Long): Student?

    @Query("SELECT * FROM students WHERE rollNo = :rollNo AND classKey = :classKey LIMIT 1")
    suspend fun getStudentByRollAndClassKey(rollNo: Int, classKey: String): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR CAST(rollNo AS TEXT) LIKE '%' || :query || '%' OR fatherName LIKE '%' || :query || '%' OR contactNo LIKE '%' || :query || '%' ORDER BY rollNo ASC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR CAST(rollNo AS TEXT) LIKE '%' || :query || '%' OR fatherName LIKE '%' || :query || '%' ORDER BY rollNo ASC")
    fun searchStudentsByNameRollFather(query: String): Flow<List<Student>>

    @Query("""
        SELECT DISTINCT s.* FROM students s
        INNER JOIN class_students cs ON s.id = cs.studentId
        WHERE cs.classId IN (:classIds)
          AND (s.name LIKE '%' || :query || '%' OR CAST(s.rollNo AS TEXT) LIKE '%' || :query || '%' OR s.fatherName LIKE '%' || :query || '%')
        ORDER BY s.rollNo ASC
    """)
    fun searchStudentsInClasses(query: String, classIds: List<Long>): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET contactNo = :newPhone, phoneUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePhoneNumber(id: Long, newPhone: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    @Query("SELECT COUNT(*) FROM students WHERE contactNo = '' OR contactNo IS NULL")
    suspend fun getIncompletePhoneCount(): Int

    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    suspend fun getAllStudentsSync(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudents(students: List<Student>)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}
