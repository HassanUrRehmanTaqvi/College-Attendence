package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.AttendanceSession
import kotlinx.coroutines.flow.Flow

data class StudentAttendanceStat(
    val studentId: Long,
    val rollNo: Int,
    val name: String,
    val fatherName: String,
    val contactNo: String,
    val totalDays: Int,
    val presentCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Int
        get() = if (totalDays > 0) ((presentCount.toDouble() / totalDays) * 100).toInt() else 0
}

data class SimpleCount(
    val presentCount: Int,
    val absentCount: Int
)

data class MonthlyStudentStat(
    val monthPrefix: String,
    val totalDays: Int,
    val presentCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Int
        get() = if (totalDays > 0) ((presentCount.toDouble() / totalDays) * 100).toInt() else 0
}

data class SimpleAttendanceSummary(
    val totalDays: Int,
    val presentCount: Int,
    val absentCount: Int
) {
    val attendancePercentage: Int
        get() = if (totalDays > 0) ((presentCount.toDouble() / totalDays) * 100).toInt() else 0
}

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC")
    fun getSessionsForClass(classId: Long): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<AttendanceSession>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId AND date = :date LIMIT 1")
    suspend fun getSessionByClassAndDate(classId: Long, date: String): AttendanceSession?

    @Query("SELECT * FROM attendance_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): AttendanceSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSession): Long

    @Update
    suspend fun updateSession(session: AttendanceSession)

    @Query("DELETE FROM attendance_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsForSession(sessionId: Long)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSessionSync(sessionId: Long): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Transaction
    suspend fun saveAttendanceSessionWithRecords(
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): Long {
        val existing = getSessionByClassAndDate(session.classId, session.date)
        val sessionId = if (existing != null) {
            val updatedSession = session.copy(id = existing.id)
            updateSession(updatedSession)
            deleteRecordsForSession(existing.id)
            existing.id
        } else {
            insertSession(session)
        }
        val recordsWithSession = records.map { it.copy(sessionId = sessionId) }
        insertAttendanceRecords(recordsWithSession)
        return sessionId
    }

    // Monthly stats for a class
    @Query("""
        SELECT 
            s.id AS studentId,
            s.rollNo AS rollNo,
            s.name AS name,
            s.fatherName AS fatherName,
            s.contactNo AS contactNo,
            COUNT(ar.id) AS totalDays,
            SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN ar.status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM students s
        INNER JOIN class_students cs ON s.id = cs.studentId
        LEFT JOIN attendance_records ar ON s.id = ar.studentId AND ar.classId = :classId AND ar.date LIKE :monthPrefix || '%'
        WHERE cs.classId = :classId
        GROUP BY s.id
        ORDER BY s.rollNo ASC
    """)
    fun getMonthlyClassAttendanceSummary(classId: Long, monthPrefix: String): Flow<List<StudentAttendanceStat>>

    // All-time / Session stats for a class
    @Query("""
        SELECT 
            s.id AS studentId,
            s.rollNo AS rollNo,
            s.name AS name,
            s.fatherName AS fatherName,
            s.contactNo AS contactNo,
            COUNT(ar.id) AS totalDays,
            SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN ar.status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM students s
        INNER JOIN class_students cs ON s.id = cs.studentId
        LEFT JOIN attendance_records ar ON s.id = ar.studentId AND ar.classId = :classId
        WHERE cs.classId = :classId
        GROUP BY s.id
        ORDER BY s.rollNo ASC
    """)
    fun getAllTimeClassAttendanceSummary(classId: Long): Flow<List<StudentAttendanceStat>>

    // Custom date range stats for a class
    @Query("""
        SELECT 
            s.id AS studentId,
            s.rollNo AS rollNo,
            s.name AS name,
            s.fatherName AS fatherName,
            s.contactNo AS contactNo,
            COUNT(ar.id) AS totalDays,
            SUM(CASE WHEN ar.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN ar.status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM students s
        INNER JOIN class_students cs ON s.id = cs.studentId
        LEFT JOIN attendance_records ar ON s.id = ar.studentId AND ar.classId = :classId AND ar.date >= :startDate AND ar.date <= :endDate
        WHERE cs.classId = :classId
        GROUP BY s.id
        ORDER BY s.rollNo ASC
    """)
    fun getDateRangeClassAttendanceSummary(classId: Long, startDate: String, endDate: String): Flow<List<StudentAttendanceStat>>

    // Get specific student stats for dynamic parent message
    @Query("""
        SELECT 
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM attendance_records
        WHERE studentId = :studentId AND classId = :classId AND date LIKE :monthPrefix || '%'
    """)
    suspend fun getStudentMonthlyCounts(studentId: Long, classId: Long, monthPrefix: String): SimpleCount?

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsSync(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_sessions")
    suspend fun getAllSessionsSync(): List<AttendanceSession>

    // Student-wise: Monthly breakdown for full academic year
    @Query("""
        SELECT 
            substr(date, 1, 7) AS monthPrefix,
            COUNT(id) AS totalDays,
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM attendance_records
        WHERE studentId = :studentId AND (:classId = 0 OR classId = :classId)
        GROUP BY substr(date, 1, 7)
        ORDER BY monthPrefix ASC
    """)
    fun getStudentMonthlyBreakdown(studentId: Long, classId: Long = 0): Flow<List<MonthlyStudentStat>>

    // Student-wise: Overall stats across academic year
    @Query("""
        SELECT 
            COUNT(id) AS totalDays,
            SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS presentCount,
            SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) AS absentCount
        FROM attendance_records
        WHERE studentId = :studentId AND (:classId = 0 OR classId = :classId)
    """)
    fun getStudentOverallStats(studentId: Long, classId: Long = 0): Flow<SimpleAttendanceSummary?>

    // Student-wise: Full date-wise history
    @Query("""
        SELECT * FROM attendance_records
        WHERE studentId = :studentId AND (:classId = 0 OR classId = :classId)
        ORDER BY date DESC
    """)
    fun getStudentAttendanceHistory(studentId: Long, classId: Long = 0): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC")
    suspend fun getAllSessionsSync(): List<AttendanceSession>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllAttendanceRecordsSync(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<AttendanceSession>)

    @Query("DELETE FROM attendance_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendanceRecords()
}
