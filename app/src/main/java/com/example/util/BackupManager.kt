package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.AttendanceSession
import com.example.data.entity.ClassStudentCrossRef
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPreviewInfo(
    val isValid: Boolean,
    val version: Int = 1,
    val timestamp: Long = 0L,
    val formattedDate: String = "",
    val teacherName: String? = null,
    val classesCount: Int = 0,
    val studentsCount: Int = 0,
    val sessionsCount: Int = 0,
    val recordsCount: Int = 0,
    val sourceDescription: String = "",
    val errorMessage: String? = null,
    val rawJson: String? = null,
    val dbFileUri: Uri? = null,
    val isDbFile: Boolean = false
)

object BackupManager {

    private fun getBackupsDir(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates an offline JSON backup file containing all tables and metadata.
     */
    suspend fun createJsonBackupFile(
        context: Context,
        repository: AttendanceRepository
    ): File = withContext(Dispatchers.IO) {
        val jsonContent = repository.createFullBackupJson()
        val backupsDir = getBackupsDir(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(backupsDir, "GACMR_Attendance_Backup_$timestamp.json")
        FileOutputStream(file).use { out ->
            out.write(jsonContent.toByteArray(Charsets.UTF_8))
            out.flush()
        }
        file
    }

    /**
     * Creates an offline SQLite Database (.db) backup file.
     */
    suspend fun createDatabaseBackupFile(
        context: Context
    ): File = withContext(Dispatchers.IO) {
        // Run a full checkpoint so WAL changes are flushed into the main DB file
        try {
            val db = AppDatabase.getDatabase(context)
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dbFile = context.getDatabasePath("gacmr_attendance.db")
        val backupsDir = getBackupsDir(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupsDir, "GACMR_Database_Backup_$timestamp.db")

        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
                output.flush()
            }
        }
        backupFile
    }

    /**
     * Inspects and validates a JSON string to provide a preview before confirmation.
     */
    fun parseAndPreviewJson(jsonString: String): BackupPreviewInfo {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("version", 1)
            val timestamp = root.optLong("timestamp", System.currentTimeMillis())
            val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

            var teacherName: String? = null
            if (root.has("teacher")) {
                val tObj = root.optJSONObject("teacher")
                teacherName = tObj?.optString("name")
            }

            val classesCount = root.optJSONArray("classes")?.length() ?: 0
            val studentsCount = root.optJSONArray("students")?.length() ?: 0
            val sessionsCount = root.optJSONArray("sessions")?.length() ?: 0
            val recordsCount = root.optJSONArray("attendanceRecords")?.length() ?: 0

            BackupPreviewInfo(
                isValid = true,
                version = version,
                timestamp = timestamp,
                formattedDate = dateStr,
                teacherName = teacherName,
                classesCount = classesCount,
                studentsCount = studentsCount,
                sessionsCount = sessionsCount,
                recordsCount = recordsCount,
                sourceDescription = "JSON بیک اپ فائل",
                rawJson = jsonString,
                isDbFile = false
            )
        } catch (e: Exception) {
            BackupPreviewInfo(
                isValid = false,
                errorMessage = "غلط یا نامکمل JSON فائل: ${e.localizedMessage}",
                sourceDescription = "نامعلوم فائل"
            )
        }
    }

    /**
     * Inspects a selected database (.db) file URI.
     */
    fun previewDbFile(context: Context, uri: Uri): BackupPreviewInfo {
        return try {
            val fileSize = context.contentResolver.openInputStream(uri)?.use { it.available() } ?: 0
            BackupPreviewInfo(
                isValid = true,
                formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()),
                sourceDescription = "SQLite ڈیٹا بیس فائل (.db) • سائز: ${fileSize / 1024} KB",
                dbFileUri = uri,
                isDbFile = true
            )
        } catch (e: Exception) {
            BackupPreviewInfo(
                isValid = false,
                errorMessage = "ڈیٹا بیس فائل پڑھنے میں ناکامی: ${e.localizedMessage}",
                sourceDescription = "خراب ڈیٹا بیس فائل"
            )
        }
    }

    /**
     * Restores database from a selected SQLite .db file.
     */
    suspend fun restoreDatabaseFromFile(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("gacmr_attendance.db")
            // Also handle wal/shm files if present
            val walFile = File(dbFile.parentFile, "gacmr_attendance.db-wal")
            val shmFile = File(dbFile.parentFile, "gacmr_attendance.db-shm")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Opens share/export dialog for any generated backup file.
     */
    fun shareBackupFile(context: Context, file: File, mimeType: String = "application/json") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "حاضری سسٹم بیک اپ — گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید")
                putExtra(Intent.EXTRA_TEXT, "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان کے حاضری سسٹم کا آف لائن بیک اپ۔")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "بیک اپ محفوظ یا شیئر کریں"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reads text content from a content URI (e.g. from File Picker).
     */
    suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: ""
    }
}
