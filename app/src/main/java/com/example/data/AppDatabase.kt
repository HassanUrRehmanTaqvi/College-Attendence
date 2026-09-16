package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.ClassDao
import com.example.data.dao.StudentDao
import com.example.data.dao.TeacherDao
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.AttendanceSession
import com.example.data.entity.ClassStudentCrossRef
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@Database(
    entities = [
        Student::class,
        TeacherProfile::class,
        AcademicClass::class,
        ClassStudentCrossRef::class,
        AttendanceSession::class,
        AttendanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun classDao(): ClassDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gacmr_attendance.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial students from assets
                            INSTANCE?.let { appDb ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedInitialStudents(context.applicationContext, appDb.studentDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialStudents(context: Context, studentDao: StudentDao) {
            try {
                if (studentDao.getStudentCount() > 0) return
                val jsonString = context.assets.open("initial_students.json")
                    .bufferedReader()
                    .use { it.readText() }
                val root = JSONObject(jsonString)
                val studentsObj = root.optJSONObject("students") ?: return

                val studentList = mutableListOf<Student>()
                val keys = studentsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val classObj = studentsObj.getJSONObject(key)
                    val className = classObj.optString("class_name", "")
                    val session = classObj.optString("session", "")
                    val records = classObj.optJSONArray("records") ?: continue

                    for (i in 0 until records.length()) {
                        val record = records.getJSONObject(i)
                        val rollNo = record.optInt("roll_no", 0)
                        val name = record.optString("name", "").trim()
                        val fatherName = record.optString("father_name", "").trim()
                        val contactNo = record.optString("contact_no", "").trim()

                        if (rollNo > 0 && name.isNotEmpty()) {
                            studentList.add(
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
                }
                if (studentList.isNotEmpty()) {
                    studentDao.insertStudents(studentList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
