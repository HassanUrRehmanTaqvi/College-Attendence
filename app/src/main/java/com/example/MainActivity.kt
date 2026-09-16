package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.entity.AcademicClass
import com.example.data.entity.Student
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AnnualAttendanceScreen
import com.example.ui.screens.AttendanceReportsScreen
import com.example.ui.screens.AuthorizedStudentSearchScreen
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.ClassDetailScreen
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.ParentCommunicationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StudentDatabaseScreen
import com.example.ui.screens.TakeAttendanceScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppRoot()
                    }
                }
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: MainViewModel = viewModel()) {
    val teacherProfile by viewModel.teacherProfile.collectAsState()
    val allClasses by viewModel.allClasses.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val classStudents by viewModel.classStudents.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()
    val existingWarning by viewModel.existingSessionWarning.collectAsState()
    val lastAbsentStudents by viewModel.lastAbsentStudents.collectAsState()
    val validationReport by viewModel.validationReport.collectAsState()

    val navController = rememberNavController()
    var activeDetailClass by remember { mutableStateOf<AcademicClass?>(null) }
    var studentForAnnualRecord by remember { mutableStateOf<Student?>(null) }

    var isAppLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1400)
        isAppLoading = false
    }

    if (isAppLoading) {
        LoadingScreen()
    } else if (teacherProfile == null) {
        WelcomeScreen(
            onProfileSaved = { name, department, pinCode ->
                viewModel.saveTeacherProfile(name, department, pinCode)
            }
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = Screen.DASHBOARD
        ) {
            composable(Screen.DASHBOARD) {
                DashboardScreen(
                    teacher = teacherProfile,
                    classes = allClasses,
                    studentCount = allStudents.size,
                    todayUrduDate = viewModel.todayUrduDisplayDate,
                    onNavigate = { route ->
                        if (route == Screen.TAKE_ATTENDANCE && allClasses.isNotEmpty() && selectedClass == null) {
                            viewModel.selectClassForAttendance(allClasses.first())
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(Screen.TAKE_ATTENDANCE) {
                TakeAttendanceScreen(
                    classes = allClasses,
                    selectedClass = selectedClass,
                    selectedDate = selectedDate,
                    students = classStudents,
                    attendanceMap = attendanceMap,
                    existingWarning = existingWarning,
                    onSelectClass = { cls ->
                        viewModel.selectClassForAttendance(cls)
                    },
                    onSelectDate = { date ->
                        viewModel.setAttendanceDate(date)
                    },
                    onToggleAttendance = { studentId ->
                        viewModel.toggleStudentAttendance(studentId)
                    },
                    onMarkAllPresent = {
                        viewModel.markAllPresent()
                    },
                    onMarkAllAbsent = {
                        viewModel.markAllAbsent()
                    },
                    onSaveAttendance = {
                        viewModel.saveAttendanceSession {
                            if (viewModel.lastAbsentStudents.value.isNotEmpty()) {
                                navController.navigate(Screen.PARENT_COMMUNICATION)
                            } else {
                                navController.popBackStack()
                            }
                        }
                    },
                    onDismissWarning = {
                        viewModel.dismissExistingWarning()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.PARENT_COMMUNICATION) {
                ParentCommunicationScreen(
                    absentStudents = if (lastAbsentStudents.isNotEmpty()) lastAbsentStudents else classStudents.filter { attendanceMap[it.id] == false },
                    selectedClass = selectedClass,
                    selectedDate = selectedDate,
                    teacher = teacherProfile,
                    onUpdatePhone = { studentId, newPhone ->
                        viewModel.updateStudentPhone(studentId, newPhone)
                    },
                    getStudentStats = { studentId, classId ->
                        viewModel.getStudentMonthlyStats(studentId, classId)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.CLASSES) {
                ClassesScreen(
                    classes = allClasses,
                    allStudents = allStudents,
                    onCreateClass = { name, grade, section, subject, session, studentIds ->
                        viewModel.createClass(name, grade, section, subject, session, studentIds) {
                            // Class created
                        }
                    },
                    onDeleteClass = { cls ->
                        viewModel.deleteClass(cls)
                    },
                    onSelectClassDetails = { cls ->
                        activeDetailClass = cls
                        navController.navigate(Screen.CLASS_DETAIL)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.CLASS_DETAIL) {
                val currentClass = activeDetailClass
                if (currentClass != null) {
                    val enrolledInDetail by viewModel.getMonthlyReport(currentClass.id, viewModel.currentMonthPrefix)
                        .collectAsState(initial = emptyList())

                    // Map stats to enrolled students
                    val enrolledStudents = allStudents.filter { st ->
                        enrolledInDetail.any { it.studentId == st.id }
                    }

                    ClassDetailScreen(
                        academicClass = currentClass,
                        enrolledStudents = enrolledStudents,
                        allStudents = allStudents,
                        onRemoveStudent = { studentId ->
                            viewModel.removeStudentFromClass(currentClass.id, studentId)
                        },
                        onAddStudents = { studentIds ->
                            viewModel.addStudentsToClass(currentClass.id, studentIds)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(Screen.REPORTS) {
                AttendanceReportsScreen(
                    viewModel = viewModel,
                    classes = allClasses,
                    teacher = teacherProfile,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ANNUAL_ATTENDANCE) {
                AnnualAttendanceScreen(
                    viewModel = viewModel,
                    classes = allClasses,
                    teacher = teacherProfile,
                    initialSelectedStudent = studentForAnnualRecord,
                    onBack = {
                        studentForAnnualRecord = null
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AUTHORIZED_STUDENT_SEARCH) {
                AuthorizedStudentSearchScreen(
                    viewModel = viewModel,
                    onSelectStudentForAnnualRecord = { student ->
                        studentForAnnualRecord = student
                        navController.navigate(Screen.ANNUAL_ATTENDANCE)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.STUDENT_DATABASE) {
                StudentDatabaseScreen(
                    students = allStudents,
                    onUpdatePhone = { id, phone ->
                        viewModel.updateStudentPhone(id, phone)
                    },
                    onViewAnnualRecord = { student ->
                        studentForAnnualRecord = student
                        navController.navigate(Screen.ANNUAL_ATTENDANCE)
                    },
                    onOpenAuthorizedSearch = {
                        navController.navigate(Screen.AUTHORIZED_STUDENT_SEARCH)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.BACKUP) {
                BackupScreen(
                    validationReport = validationReport,
                    onValidateJson = { jsonStr ->
                        viewModel.validateAndImportJson(jsonStr)
                    },
                    onDismissReport = {
                        viewModel.dismissValidationReport()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.PROFILE) {
                ProfileScreen(
                    teacher = teacherProfile,
                    onSaveProfile = { name, department, pinCode ->
                        viewModel.saveTeacherProfile(name, department, pinCode)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
