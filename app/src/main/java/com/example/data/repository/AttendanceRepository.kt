package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.ClassDao
import com.example.data.dao.MonthlyStudentStat
import com.example.data.dao.SimpleAttendanceSummary
import com.example.data.dao.SimpleCount
import com.example.data.dao.StudentAttendanceStat
import com.example.data.dao.StudentDao
import com.example.data.dao.TeacherDao
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.AttendanceSession
import com.example.data.entity.ClassStudentCrossRef
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

data class ImportValidationReport(
    val totalStudents: Int,
    val successfullyImported: Int,
    val incompleteRecords: Int,
    val invalidPhones: Int,
    val duplicateRolls: Int,
    val message: String
)

class AttendanceRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context)
) {
    private val studentDao: StudentDao = database.studentDao()
    private val teacherDao: TeacherDao = database.teacherDao()
    private val classDao: ClassDao = database.classDao()
    private val attendanceDao: AttendanceDao = database.attendanceDao()

    // Ensure database is populated
    suspend fun ensureInitialized() {
        if (studentDao.getStudentCount() == 0) {
            AppDatabase.seedInitialStudents(context, studentDao)
        }
    }

    // Teacher Profile
    val teacherProfile: Flow<TeacherProfile?> = teacherDao.getTeacherProfile()

    suspend fun getTeacherProfileSync(): TeacherProfile? = teacherDao.getTeacherProfileSync()

    suspend fun saveTeacherProfile(teacher: TeacherProfile) {
        teacherDao.saveTeacherProfile(teacher)
    }

    suspend fun updateTeacherNameAndDept(name: String, department: String) {
        teacherDao.updateNameAndDepartment(name, department)
    }

    suspend fun updatePinLock(pin: String?, enabled: Boolean) {
        teacherDao.updatePinLock(pin, enabled)
    }

    // Students
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()

    fun getStudentsByClassKey(classKey: String): Flow<List<Student>> =
        studentDao.getStudentsByClassKey(classKey)

    fun searchStudents(query: String): Flow<List<Student>> =
        studentDao.searchStudents(query)

    fun searchStudentsByNameRollFather(query: String): Flow<List<Student>> =
        studentDao.searchStudentsByNameRollFather(query)

    fun searchStudentsInClasses(query: String, classIds: List<Long>): Flow<List<Student>> =
        if (classIds.isEmpty()) studentDao.searchStudentsByNameRollFather(query)
        else studentDao.searchStudentsInClasses(query, classIds)

    fun getStudentById(id: Long): Flow<Student?> =
        studentDao.getStudentById(id)

    suspend fun updateStudentPhone(id: Long, newPhone: String) {
        studentDao.updatePhoneNumber(id, newPhone.trim(), System.currentTimeMillis())
    }

    suspend fun getStudentCount(): Int = studentDao.getStudentCount()

    // Classes
    val allClasses: Flow<List<AcademicClass>> = classDao.getAllClasses()

    fun getClassById(id: Long): Flow<AcademicClass?> = classDao.getClassById(id)

    suspend fun getClassByIdSync(id: Long): AcademicClass? = classDao.getClassByIdSync(id)

    suspend fun createClass(academicClass: AcademicClass, selectedStudentIds: List<Long>): Long {
        val classId = classDao.insertClass(academicClass)
        val refs = selectedStudentIds.map { ClassStudentCrossRef(classId = classId, studentId = it) }
        classDao.addStudentsToClass(refs)
        return classId
    }

    suspend fun updateClass(academicClass: AcademicClass) {
        classDao.updateClass(academicClass)
    }

    suspend fun deleteClass(academicClass: AcademicClass) {
        classDao.deleteClass(academicClass)
    }

    fun getStudentsForClass(classId: Long): Flow<List<Student>> =
        classDao.getStudentsForClass(classId)

    suspend fun getStudentsForClassSync(classId: Long): List<Student> =
        classDao.getStudentsForClassSync(classId)

    suspend fun addStudentToClass(classId: Long, studentId: Long) {
        classDao.addStudentToClass(ClassStudentCrossRef(classId, studentId))
    }

    suspend fun addStudentsToClass(classId: Long, studentIds: List<Long>) {
        val refs = studentIds.map { ClassStudentCrossRef(classId, it) }
        classDao.addStudentsToClass(refs)
    }

    suspend fun removeStudentFromClass(classId: Long, studentId: Long) {
        // IMPORTANT: Only removes cross reference, does NOT delete Student!
        classDao.removeStudentFromClass(classId, studentId)
    }

    // Attendance
    fun getSessionsForClass(classId: Long): Flow<List<AttendanceSession>> =
        attendanceDao.getSessionsForClass(classId)

    val allSessions: Flow<List<AttendanceSession>> = attendanceDao.getAllSessions()

    suspend fun checkExistingSession(classId: Long, date: String): AttendanceSession? =
        attendanceDao.getSessionByClassAndDate(classId, date)

    suspend fun getSessionById(sessionId: Long): AttendanceSession? =
        attendanceDao.getSessionById(sessionId)

    fun getRecordsForSession(sessionId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForSession(sessionId)

    suspend fun getRecordsForSessionSync(sessionId: Long): List<AttendanceRecord> =
        attendanceDao.getRecordsForSessionSync(sessionId)

    suspend fun saveAttendance(
        session: AttendanceSession,
        records: List<AttendanceRecord>
    ): Long {
        return attendanceDao.saveAttendanceSessionWithRecords(session, records)
    }

    suspend fun deleteSession(sessionId: Long) {
        attendanceDao.deleteSession(sessionId)
    }

    fun getMonthlyAttendance(classId: Long, monthPrefix: String): Flow<List<StudentAttendanceStat>> =
        attendanceDao.getMonthlyClassAttendanceSummary(classId, monthPrefix)

    fun getAllTimeAttendance(classId: Long): Flow<List<StudentAttendanceStat>> =
        attendanceDao.getAllTimeClassAttendanceSummary(classId)

    fun getDateRangeAttendance(classId: Long, startDate: String, endDate: String): Flow<List<StudentAttendanceStat>> =
        attendanceDao.getDateRangeClassAttendanceSummary(classId, startDate, endDate)

    suspend fun getStudentMonthlyCounts(studentId: Long, classId: Long, monthPrefix: String): SimpleCount {
        return attendanceDao.getStudentMonthlyCounts(studentId, classId, monthPrefix)
            ?: SimpleCount(presentCount = 0, absentCount = 0)
    }

    fun getStudentMonthlyBreakdown(studentId: Long, classId: Long = 0): Flow<List<MonthlyStudentStat>> =
        attendanceDao.getStudentMonthlyBreakdown(studentId, classId)

    fun getStudentOverallStats(studentId: Long, classId: Long = 0): Flow<SimpleAttendanceSummary?> =
        attendanceDao.getStudentOverallStats(studentId, classId)

    fun getStudentAttendanceHistory(studentId: Long, classId: Long = 0): Flow<List<AttendanceRecord>> =
        attendanceDao.getStudentAttendanceHistory(studentId, classId)

    // JSON Import and Validation
    suspend fun validateAndImportJson(jsonString: String): ImportValidationReport {
        return try {
            val root = JSONObject(jsonString)
            val studentsObj = root.optJSONObject("students")
                ?: return ImportValidationReport(0, 0, 0, 0, 0, "JSON میں students آبجیکٹ نہیں ملا۔")

            var total = 0
            var imported = 0
            var incomplete = 0
            var invalidPhones = 0
            var duplicateRolls = 0

            val studentsToInsert = mutableListOf<Student>()
            val seenRolls = mutableSetOf<String>()

            val keys = studentsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val classObj = studentsObj.getJSONObject(key)
                val className = classObj.optString("class_name", "کلاس")
                val session = classObj.optString("session", "")
                val records = classObj.optJSONArray("records") ?: continue

                for (i in 0 until records.length()) {
                    total++
                    val rec = records.getJSONObject(i)
                    val rollNo = rec.optInt("roll_no", 0)
                    val name = rec.optString("name", "").trim()
                    val fatherName = rec.optString("father_name", "").trim()
                    val contactNo = rec.optString("contact_no", "").trim()

                    if (rollNo == 0 || name.isEmpty()) {
                        incomplete++
                        continue
                    }

                    val compositeKey = "$key-$rollNo"
                    if (seenRolls.contains(compositeKey)) {
                        duplicateRolls++
                    } else {
                        seenRolls.add(compositeKey)
                    }

                    if (contactNo.isEmpty() || !contactNo.matches(Regex("^(03|\\+923)[0-9]{9}$"))) {
                        invalidPhones++
                    }

                    studentsToInsert.add(
                        Student(
                            rollNo = rollNo,
                            name = name,
                            fatherName = fatherName,
                            contactNo = contactNo,
                            className = className,
                            classKey = key,
                            session = session
                        )
                    )
                }
            }

            if (studentsToInsert.isNotEmpty()) {
                studentDao.insertStudents(studentsToInsert)
                imported = studentsToInsert.size
            }

            ImportValidationReport(
                totalStudents = total,
                successfullyImported = imported,
                incompleteRecords = incomplete,
                invalidPhones = invalidPhones,
                duplicateRolls = duplicateRolls,
                message = "ڈیٹا کامیابی سے جانچ کر شامل کر دیا گیا۔"
            )
        } catch (e: Exception) {
            ImportValidationReport(
                totalStudents = 0,
                successfullyImported = 0,
                incompleteRecords = 0,
                invalidPhones = 0,
                duplicateRolls = 0,
                message = "JSON پارس کرتے وقت خرابی: ${e.localizedMessage}"
            )
        }
    }

    // Full Backup Generation & Restoration
    suspend fun createFullBackupJson(): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("institution", "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان")

        // Teacher
        val teacher = teacherDao.getTeacherProfileSync()
        if (teacher != null) {
            val teacherObj = JSONObject().apply {
                put("id", teacher.id)
                put("name", teacher.name)
                put("department", teacher.department)
                put("pinCode", teacher.pinCode ?: "")
                put("isAppLockEnabled", teacher.isAppLockEnabled)
                put("createdAt", teacher.createdAt)
            }
            root.put("teacher", teacherObj)
        }

        // Classes
        val classes = classDao.getAllClassesSync()
        val classesArray = JSONArray()
        for (c in classes) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("subject", c.subject)
                put("session", c.session)
                put("academicYear", c.academicYear)
                put("room", c.room)
                put("scheduleDays", c.scheduleDays)
                put("colorHex", c.colorHex)
                put("createdAt", c.createdAt)
            }
            classesArray.put(obj)
        }
        root.put("classes", classesArray)

        // Class-Student Cross References
        val crossRefs = classDao.getAllClassStudentCrossRefsSync()
        val refsArray = JSONArray()
        for (r in crossRefs) {
            val obj = JSONObject().apply {
                put("classId", r.classId)
                put("studentId", r.studentId)
            }
            refsArray.put(obj)
        }
        root.put("classStudents", refsArray)

        // Students
        val students = studentDao.getAllStudentsSync()
        val studentsArray = JSONArray()
        for (s in students) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("rollNo", s.rollNo)
                put("name", s.name)
                put("fatherName", s.fatherName)
                put("contactNo", s.contactNo)
                put("className", s.className)
                put("classKey", s.classKey)
                put("session", s.session)
                put("phoneUpdatedAt", s.phoneUpdatedAt)
            }
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        // Attendance Sessions
        val sessions = attendanceDao.getAllSessionsSync()
        val sessionsArray = JSONArray()
        for (sess in sessions) {
            val obj = JSONObject().apply {
                put("id", sess.id)
                put("classId", sess.classId)
                put("date", sess.date)
                put("presentCount", sess.presentCount)
                put("absentCount", sess.absentCount)
                put("topicNotes", sess.topicNotes)
                put("createdAt", sess.createdAt)
            }
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        // Attendance Records
        val records = attendanceDao.getAllAttendanceRecordsSync()
        val recordsArray = JSONArray()
        for (rec in records) {
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("sessionId", rec.sessionId)
                put("studentId", rec.studentId)
                put("classId", rec.classId)
                put("date", rec.date)
                put("status", rec.status.name)
                put("timestamp", rec.timestamp)
            }
            recordsArray.put(obj)
        }
        root.put("attendanceRecords", recordsArray)

        root.toString(2)
    }

    suspend fun createBackupJson(): String = createFullBackupJson()

    // Restoration from JSON
    suspend fun restoreFromBackupJson(jsonString: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // 1. Teacher
            if (root.has("teacher")) {
                val tObj = root.getJSONObject("teacher")
                val profile = TeacherProfile(
                    id = tObj.optLong("id", 1L),
                    name = tObj.getString("name"),
                    department = tObj.getString("department"),
                    pinCode = tObj.optString("pinCode").takeIf { it.isNotBlank() },
                    isAppLockEnabled = tObj.optBoolean("isAppLockEnabled", false),
                    createdAt = tObj.optLong("createdAt", System.currentTimeMillis())
                )
                teacherDao.saveTeacherProfile(profile)
            }

            // 2. Students
            if (root.has("students")) {
                val sArray = root.getJSONArray("students")
                val studentList = mutableListOf<Student>()
                for (i in 0 until sArray.length()) {
                    val sObj = sArray.getJSONObject(i)
                    studentList.add(
                        Student(
                            id = sObj.optLong("id", 0L),
                            rollNo = sObj.getInt("rollNo"),
                            name = sObj.getString("name"),
                            fatherName = sObj.optString("fatherName", ""),
                            contactNo = sObj.optString("contactNo", ""),
                            className = sObj.optString("className", ""),
                            classKey = sObj.optString("classKey", ""),
                            session = sObj.optString("session", ""),
                            phoneUpdatedAt = sObj.optLong("phoneUpdatedAt", 0L)
                        )
                    )
                }
                if (studentList.isNotEmpty()) {
                    studentDao.upsertStudents(studentList)
                }
            }

            // 3. Classes
            if (root.has("classes")) {
                val cArray = root.getJSONArray("classes")
                val classList = mutableListOf<AcademicClass>()
                for (i in 0 until cArray.length()) {
                    val cObj = cArray.getJSONObject(i)
                    classList.add(
                        AcademicClass(
                            id = cObj.optLong("id", 0L),
                            name = cObj.getString("name"),
                            subject = cObj.getString("subject"),
                            session = cObj.getString("session"),
                            academicYear = cObj.optString("academicYear", "2024-2025"),
                            room = cObj.optString("room", ""),
                            scheduleDays = cObj.optString("scheduleDays", ""),
                            colorHex = cObj.optString("colorHex", "#1E3A8A"),
                            createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                classDao.deleteAllClasses()
                classDao.insertClasses(classList)
            }

            // 4. Class-Student Cross References
            if (root.has("classStudents")) {
                val csArray = root.getJSONArray("classStudents")
                val csList = mutableListOf<ClassStudentCrossRef>()
                for (i in 0 until csArray.length()) {
                    val csObj = csArray.getJSONObject(i)
                    csList.add(
                        ClassStudentCrossRef(
                            classId = csObj.getLong("classId"),
                            studentId = csObj.getLong("studentId")
                        )
                    )
                }
                classDao.addStudentsToClass(csList)
            }

            // 5. Attendance Sessions & Records
            if (root.has("sessions")) {
                val sessArray = root.getJSONArray("sessions")
                val sessList = mutableListOf<AttendanceSession>()
                for (i in 0 until sessArray.length()) {
                    val sObj = sessArray.getJSONObject(i)
                    sessList.add(
                        AttendanceSession(
                            id = sObj.optLong("id", 0L),
                            classId = sObj.getLong("classId"),
                            date = sObj.getString("date"),
                            presentCount = sObj.optInt("presentCount", 0),
                            absentCount = sObj.optInt("absentCount", 0),
                            topicNotes = sObj.optString("topicNotes", ""),
                            createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                attendanceDao.deleteAllSessions()
                attendanceDao.insertSessions(sessList)
            }

            if (root.has("attendanceRecords")) {
                val recArray = root.getJSONArray("attendanceRecords")
                val recList = mutableListOf<AttendanceRecord>()
                for (i in 0 until recArray.length()) {
                    val rObj = recArray.getJSONObject(i)
                    recList.add(
                        AttendanceRecord(
                            id = rObj.optLong("id", 0L),
                            sessionId = rObj.getLong("sessionId"),
                            studentId = rObj.getLong("studentId"),
                            classId = rObj.getLong("classId"),
                            date = rObj.getString("date"),
                            status = com.example.data.entity.AttendanceStatus.valueOf(rObj.getString("status")),
                            timestamp = rObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                attendanceDao.deleteAllAttendanceRecords()
                attendanceDao.insertAttendanceRecords(recList)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
