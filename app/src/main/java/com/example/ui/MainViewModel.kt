package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.dao.MonthlyStudentStat
import com.example.data.dao.SimpleAttendanceSummary
import com.example.data.dao.StudentAttendanceStat
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.AttendanceSession
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.ImportValidationReport
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)
    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

    val todayUrduDisplayDate: String
        get() = SimpleDateFormat("dd MMMM yyyy", Locale("ur", "PK")).format(Date())

    val currentMonthPrefix: String
        get() = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())

    // Database Initialization
    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    // Teacher Profile
    val teacherProfile: StateFlow<TeacherProfile?> = repository.teacherProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveTeacherProfile(name: String, department: String, pinCode: String? = null) {
        viewModelScope.launch {
            val profile = TeacherProfile(
                name = name.trim(),
                department = department.trim(),
                pinCode = pinCode?.trim()?.ifEmpty { null },
                isAppLockEnabled = !pinCode.isNullOrBlank()
            )
            repository.saveTeacherProfile(profile)
        }
    }

    // Students
    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    // Teacher authorization for searching student records
    private val _isTeacherAuthorized = MutableStateFlow(false)
    val isTeacherAuthorized: StateFlow<Boolean> = _isTeacherAuthorized.asStateFlow()

    // Filter scope: only classes taught by this teacher vs all college records
    private val _searchScopeOnlyMyClasses = MutableStateFlow(true)
    val searchScopeOnlyMyClasses: StateFlow<Boolean> = _searchScopeOnlyMyClasses.asStateFlow()

    fun verifyAndAuthorizeTeacherPin(pin: String): Boolean {
        val currentProfile = teacherProfile.value
        val correctPin = currentProfile?.pinCode
        val isMatch = if (correctPin.isNullOrBlank()) {
            if (pin.length >= 4) {
                viewModelScope.launch {
                    repository.updatePinLock(pin, true)
                }
                true
            } else false
        } else {
            correctPin == pin
        }

        if (isMatch) {
            _isTeacherAuthorized.value = true
        }
        return isMatch
    }

    fun setTeacherAuthorized(authorized: Boolean) {
        _isTeacherAuthorized.value = authorized
    }

    fun setSearchScopeOnlyMyClasses(onlyMine: Boolean) {
        _searchScopeOnlyMyClasses.value = onlyMine
    }

    fun getAuthorizedSearchResults(query: String): Flow<List<Student>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return flowOf(emptyList())
        if (!_isTeacherAuthorized.value) return flowOf(emptyList())

        return if (_searchScopeOnlyMyClasses.value) {
            val classIds = allClasses.value.map { it.id }
            repository.searchStudentsInClasses(trimmed, classIds)
        } else {
            repository.searchStudentsByNameRollFather(trimmed)
        }
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun updateStudentPhone(studentId: Long, newPhone: String) {
        viewModelScope.launch {
            repository.updateStudentPhone(studentId, newPhone)
        }
    }

    // Classes
    val allClasses: StateFlow<List<AcademicClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createClass(
        name: String,
        grade: String,
        section: String,
        subject: String,
        session: String,
        studentIds: List<Long>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val academicClass = AcademicClass(
                name = name.trim(),
                grade = grade.trim(),
                section = section.trim(),
                subject = subject.trim(),
                session = session.trim()
            )
            val classId = repository.createClass(academicClass, studentIds)
            onSuccess(classId)
        }
    }

    fun removeStudentFromClass(classId: Long, studentId: Long) {
        viewModelScope.launch {
            // Note: only removes from class cross-reference, never deletes student
            repository.removeStudentFromClass(classId, studentId)
        }
    }

    fun addStudentsToClass(classId: Long, studentIds: List<Long>) {
        viewModelScope.launch {
            repository.addStudentsToClass(classId, studentIds)
        }
    }

    fun deleteClass(academicClass: AcademicClass) {
        viewModelScope.launch {
            repository.deleteClass(academicClass)
        }
    }

    // Active Attendance Session State
    private val _selectedClass = MutableStateFlow<AcademicClass?>(null)
    val selectedClass = _selectedClass.asStateFlow()

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate = _selectedDate.asStateFlow()

    private val _classStudents = MutableStateFlow<List<Student>>(emptyList())
    val classStudents = _classStudents.asStateFlow()

    // studentId -> isPresent (true = حاضر, false = غیر حاضر)
    private val _attendanceMap = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val attendanceMap = _attendanceMap.asStateFlow()

    private val _existingSessionWarning = MutableStateFlow<AttendanceSession?>(null)
    val existingSessionWarning = _existingSessionWarning.asStateFlow()

    private val _lastSavedSessionId = MutableStateFlow<Long?>(null)
    val lastSavedSessionId = _lastSavedSessionId.asStateFlow()

    private val _lastAbsentStudents = MutableStateFlow<List<Student>>(emptyList())
    val lastAbsentStudents = _lastAbsentStudents.asStateFlow()

    fun selectClassForAttendance(academicClass: AcademicClass) {
        _selectedClass.value = academicClass
        viewModelScope.launch {
            val students = repository.getStudentsForClassSync(academicClass.id)
            _classStudents.value = students
            // By default, mark all present
            _attendanceMap.value = students.associate { it.id to true }
            checkExistingAttendance(academicClass.id, _selectedDate.value)
        }
    }

    fun setAttendanceDate(date: String) {
        _selectedDate.value = date
        val currentClass = _selectedClass.value
        if (currentClass != null) {
            checkExistingAttendance(currentClass.id, date)
        }
    }

    private fun checkExistingAttendance(classId: Long, date: String) {
        viewModelScope.launch {
            val existing = repository.checkExistingSession(classId, date)
            _existingSessionWarning.value = existing
            if (existing != null) {
                // Load existing marks
                val records = repository.getRecordsForSessionSync(existing.id)
                val newMap = records.associate { it.studentId to (it.status == "PRESENT") }
                if (newMap.isNotEmpty()) {
                    _attendanceMap.value = newMap
                }
            }
        }
    }

    fun toggleStudentAttendance(studentId: Long) {
        val current = _attendanceMap.value[studentId] ?: true
        _attendanceMap.value = _attendanceMap.value.toMutableMap().apply {
            put(studentId, !current)
        }
    }

    fun markAllPresent() {
        _attendanceMap.value = _classStudents.value.associate { it.id to true }
    }

    fun markAllAbsent() {
        _attendanceMap.value = _classStudents.value.associate { it.id to false }
    }

    fun saveAttendanceSession(onSaved: (Long) -> Unit) {
        val currentClass = _selectedClass.value ?: return
        val teacher = teacherProfile.value
        val students = _classStudents.value
        val map = _attendanceMap.value
        val date = _selectedDate.value

        viewModelScope.launch {
            val presentCount = students.count { map[it.id] != false }
            val absentCount = students.size - presentCount

            val session = AttendanceSession(
                classId = currentClass.id,
                date = date,
                teacherName = teacher?.name ?: "استاد محترم",
                department = teacher?.department ?: "شعبہ",
                subject = currentClass.subject,
                className = "${currentClass.name} (${currentClass.section})",
                totalStudents = students.size,
                presentCount = presentCount,
                absentCount = absentCount
            )

            val records = students.map { student ->
                val isPresent = map[student.id] != false
                AttendanceRecord(
                    sessionId = 0, // Assigned inside DAO transaction
                    studentId = student.id,
                    classId = currentClass.id,
                    date = date,
                    status = if (isPresent) "PRESENT" else "ABSENT"
                )
            }

            val sessionId = repository.saveAttendance(session, records)
            _lastSavedSessionId.value = sessionId
            _lastAbsentStudents.value = students.filter { map[it.id] == false }
            _existingSessionWarning.value = null
            onSaved(sessionId)
        }
    }

    fun dismissExistingWarning() {
        _existingSessionWarning.value = null
    }

    // Parent Communication Helper: monthly counts
    suspend fun getStudentMonthlyStats(studentId: Long, classId: Long): Pair<Int, Int> {
        val counts = repository.getStudentMonthlyCounts(studentId, classId, currentMonthPrefix)
        return Pair(counts.presentCount, counts.absentCount)
    }

    // Reports & Statistics
    val allSessions: StateFlow<List<AttendanceSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMonthlyReport(classId: Long, monthPrefix: String) =
        repository.getMonthlyAttendance(classId, monthPrefix)

    fun getAllTimeReport(classId: Long) =
        repository.getAllTimeAttendance(classId)

    fun getDateRangeReport(classId: Long, start: String, end: String) =
        repository.getDateRangeAttendance(classId, start, end)

    fun generateAndOpenPdf(
        context: Context,
        meta: PdfReportGenerator.ReportMeta,
        stats: List<StudentAttendanceStat>
    ) {
        viewModelScope.launch {
            val file = PdfReportGenerator.generateAttendancePdf(context, meta, stats)
            if (file != null) {
                PdfReportGenerator.openOrSharePdf(context, file)
            }
        }
    }

    // Academic Year / Student-wise Attendance
    fun getStudentMonthlyBreakdown(studentId: Long, classId: Long = 0L) =
        repository.getStudentMonthlyBreakdown(studentId, classId)

    fun getStudentOverallStats(studentId: Long, classId: Long = 0L) =
        repository.getStudentOverallStats(studentId, classId)

    fun getStudentAttendanceHistory(studentId: Long, classId: Long = 0L) =
        repository.getStudentAttendanceHistory(studentId, classId)

    fun generateAndOpenStudentAnnualPdf(
        context: Context,
        student: Student,
        className: String,
        subject: String,
        academicYear: String,
        summary: SimpleAttendanceSummary?,
        monthlyStats: List<MonthlyStudentStat>
    ) {
        viewModelScope.launch {
            val teacher = teacherProfile.value
            val file = PdfReportGenerator.generateStudentAnnualPdf(
                context = context,
                student = student,
                teacherName = teacher?.name ?: "استاد محترم",
                department = teacher?.department ?: "کالج",
                className = className,
                subject = subject,
                academicYear = academicYear,
                summary = summary,
                monthlyStats = monthlyStats
            )
            if (file != null) {
                PdfReportGenerator.openOrSharePdf(context, file)
            }
        }
    }

    // JSON Validation & Backup
    private val _validationReport = MutableStateFlow<ImportValidationReport?>(null)
    val validationReport = _validationReport.asStateFlow()

    val attendanceRepository: AttendanceRepository get() = repository

    fun validateAndImportJson(jsonString: String) {
        viewModelScope.launch {
            val report = repository.validateAndImportJson(jsonString)
            _validationReport.value = report
        }
    }

    fun dismissValidationReport() {
        _validationReport.value = null
    }

    fun exportJsonBackup(context: Context, onComplete: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            try {
                val file = com.example.util.BackupManager.createJsonBackupFile(context, repository)
                onComplete(file)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    fun exportDatabaseBackup(context: Context, onComplete: (java.io.File?) -> Unit) {
        viewModelScope.launch {
            try {
                val file = com.example.util.BackupManager.createDatabaseBackupFile(context)
                onComplete(file)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    fun restoreBackupJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreFromBackupJson(jsonString)
            onResult(success)
        }
    }

    fun restoreDatabaseFile(context: Context, uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = com.example.util.BackupManager.restoreDatabaseFromFile(context, uri)
            onResult(success)
        }
    }
}
