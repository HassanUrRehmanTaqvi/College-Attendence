package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AcademicClass
import com.example.data.entity.ClassStudentCrossRef
import com.example.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {

    @Query("SELECT * FROM academic_classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<AcademicClass>>

    @Query("SELECT * FROM academic_classes WHERE id = :id LIMIT 1")
    fun getClassById(id: Long): Flow<AcademicClass?>

    @Query("SELECT * FROM academic_classes WHERE id = :id LIMIT 1")
    suspend fun getClassByIdSync(id: Long): AcademicClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(academicClass: AcademicClass): Long

    @Update
    suspend fun updateClass(academicClass: AcademicClass)

    @Delete
    suspend fun deleteClass(academicClass: AcademicClass)

    @Query("SELECT s.* FROM students s INNER JOIN class_students cs ON s.id = cs.studentId WHERE cs.classId = :classId ORDER BY s.rollNo ASC")
    fun getStudentsForClass(classId: Long): Flow<List<Student>>

    @Query("SELECT s.* FROM students s INNER JOIN class_students cs ON s.id = cs.studentId WHERE cs.classId = :classId ORDER BY s.rollNo ASC")
    suspend fun getStudentsForClassSync(classId: Long): List<Student>

    @Query("SELECT studentId FROM class_students WHERE classId = :classId")
    suspend fun getStudentIdsForClassSync(classId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStudentToClass(crossRef: ClassStudentCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStudentsToClass(crossRefs: List<ClassStudentCrossRef>)

    @Query("DELETE FROM class_students WHERE classId = :classId AND studentId = :studentId")
    suspend fun removeStudentFromClass(classId: Long, studentId: Long)

    @Query("DELETE FROM class_students WHERE classId = :classId")
    suspend fun removeAllStudentsFromClass(classId: Long)

    @Query("SELECT * FROM academic_classes ORDER BY createdAt DESC")
    suspend fun getAllClassesSync(): List<AcademicClass>

    @Query("SELECT * FROM class_students")
    suspend fun getAllClassStudentCrossRefsSync(): List<ClassStudentCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<AcademicClass>)

    @Query("DELETE FROM academic_classes")
    suspend fun deleteAllClasses()

    @Query("SELECT COUNT(*) FROM class_students WHERE classId = :classId")
    fun getStudentCountForClass(classId: Long): Flow<Int>
}
