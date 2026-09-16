package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.TeacherProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    @Query("SELECT * FROM teachers WHERE id = 1 LIMIT 1")
    fun getTeacherProfile(): Flow<TeacherProfile?>

    @Query("SELECT * FROM teachers WHERE id = 1 LIMIT 1")
    suspend fun getTeacherProfileSync(): TeacherProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTeacherProfile(teacher: TeacherProfile)

    @Query("UPDATE teachers SET name = :name, department = :department WHERE id = 1")
    suspend fun updateNameAndDepartment(name: String, department: String)

    @Query("UPDATE teachers SET pinCode = :pin, isAppLockEnabled = :enabled WHERE id = 1")
    suspend fun updatePinLock(pin: String?, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM teachers")
    suspend fun countTeachers(): Int
}
