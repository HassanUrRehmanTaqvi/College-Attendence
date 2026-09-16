package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.MonthlyStudentStat
import com.example.data.dao.SimpleAttendanceSummary
import com.example.data.dao.StudentAttendanceStat
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceRecord
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen
import com.example.util.CommunicationHelper
import com.example.util.PdfReportGenerator

private fun formatUrduMonth(monthPrefix: String): String {
    val parts = monthPrefix.split("-")
    if (parts.size >= 2) {
        val y = parts[0]
        val m = parts[1].toIntOrNull() ?: 1
        val monthNames = arrayOf(
            "", "جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون",
            "جولائی", "اگست", "ستمبر", "اکتوبر", "نومبر", "دسمبر"
        )
        val mName = if (m in 1..12) monthNames[m] else parts[1]
        return "$mName $y"
    }
    return monthPrefix
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualAttendanceScreen(
    viewModel: MainViewModel,
    classes: List<AcademicClass>,
    teacher: TeacherProfile?,
    initialSelectedStudent: Student? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTabIndex by remember {
        mutableIntStateOf(if (initialSelectedStudent != null) 1 else 0)
    }

    // Selected class state
    var selectedClass by remember(classes) {
        mutableStateOf(classes.firstOrNull())
    }

    // Selected student state for student-wise view
    var selectedStudent by remember {
        mutableStateOf<Student?>(initialSelectedStudent)
    }

    // Academic Year display
    val academicYear = selectedClass?.session?.ifBlank { "2024-2025" } ?: "2024-2025"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "پوری تدریسی سال کی حاضری کا تفصیلی ریکارڈ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
                            fontSize = 11.sp,
                            color = CollegeGoldTertiary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "واپس",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CollegeNavyDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Tabs: 1. کلاس کے لحاظ سے | 2. طالب علم کے لحاظ سے
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = CollegeNavyPrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    modifier = Modifier.testTag("tab_class_wise"),
                    text = {
                        Text(
                            text = "کلاس کے لحاظ سے ریکارڈ",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    modifier = Modifier.testTag("tab_student_wise"),
                    text = {
                        Text(
                            text = "طالب علم کے لحاظ سے ریکارڈ",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Class-wise Annual Attendance
                ClassWiseAnnualView(
                    viewModel = viewModel,
                    classes = classes,
                    selectedClass = selectedClass,
                    academicYear = academicYear,
                    teacher = teacher,
                    onSelectClass = { selectedClass = it },
                    onStudentClick = { studentStat ->
                        // Find the student object and switch to student tab
                        viewModel.allStudents.value.find { it.id == studentStat.studentId }?.let { st ->
                            selectedStudent = st
                            selectedTabIndex = 1
                        }
                    }
                )
            } else {
                // Student-wise Annual Attendance
                StudentWiseAnnualView(
                    viewModel = viewModel,
                    classes = classes,
                    selectedClass = selectedClass,
                    academicYear = academicYear,
                    selectedStudent = selectedStudent,
                    teacher = teacher,
                    onSelectClass = { selectedClass = it },
                    onSelectStudent = { selectedStudent = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassWiseAnnualView(
    viewModel: MainViewModel,
    classes: List<AcademicClass>,
    selectedClass: AcademicClass?,
    academicYear: String,
    teacher: TeacherProfile?,
    onSelectClass: (AcademicClass) -> Unit,
    onStudentClick: (StudentAttendanceStat) -> Unit
) {
    val context = LocalContext.current
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }

    if (classes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ابھی تک کوئی کلاس موجود نہیں ہے۔ پہلے کلاس بنائیں!",
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )
        }
        return
    }

    val currentClass = selectedClass ?: classes.first()

    // Collect all-time / session stats for this class
    val classStats by viewModel.getAllTimeReport(currentClass.id).collectAsState(initial = emptyList())

    val filteredStats = if (filterQuery.isBlank()) {
        classStats
    } else {
        classStats.filter {
            it.name.contains(filterQuery, ignoreCase = true) ||
            it.rollNo.toString().contains(filterQuery) ||
            it.fatherName.contains(filterQuery, ignoreCase = true)
        }
    }

    // High level metrics
    val totalStudents = classStats.size
    val totalLectures = classStats.maxOfOrNull { it.totalDays } ?: 0
    val avgPercentage = if (classStats.isNotEmpty()) {
        classStats.map { it.attendancePercentage }.average().toInt()
    } else 0
    val defaultersCount = classStats.count { it.totalDays > 0 && it.attendancePercentage < 75 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Class & Academic Session Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "منتخب کلاس و تدریسی سیشن",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyDark
                        )
                        Text(
                            text = "سیشن: $academicYear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CollegeGoldTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${currentClass.name} — ${currentClass.grade} (${currentClass.subject})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("dropdown_class_selector"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            classes.forEach { cls ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${cls.name} — ${cls.grade} (${cls.subject})")
                                    },
                                    onClick = {
                                        onSelectClass(cls)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Total Enrolled
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "کل طلبہ",
                    value = "$totalStudents",
                    bgColor = Color(0xFFEFF6FF),
                    textColor = CollegeNavyPrimary
                )
                // Metric 2: Total Lectures
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "کل سالانہ لیکچرز",
                    value = "$totalLectures",
                    bgColor = Color(0xFFF1F5F9),
                    textColor = CollegeNavyDark
                )
                // Metric 3: Avg Attendance Ratio
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "اوسط تناسب",
                    value = "$avgPercentage%",
                    bgColor = Color(0xFFEFFDF5),
                    textColor = PresentGreen
                )
                // Metric 4: Defaulters (<75%)
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "شارٹ لسٹ (<75%)",
                    value = "$defaultersCount",
                    bgColor = Color(0xFFFEF2F2),
                    textColor = AbsentRed
                )
            }
        }

        // Search within class
        item {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_class_filter"),
                placeholder = { Text("کلاس میں رول نمبر، نام، یا والد کا نام تلاش کریں...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CollegeNavyPrimary
                    )
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        // Export Full Academic Year Register PDF
        item {
            Button(
                onClick = {
                    val meta = PdfReportGenerator.ReportMeta(
                        title = "پوری تدریسی سال کا حاضری رجسٹر — سالانہ سیشن $academicYear",
                        teacherName = teacher?.name ?: "استاد محترم",
                        department = teacher?.department ?: "کالج",
                        className = "${currentClass.name} (${currentClass.grade})",
                        subject = currentClass.subject,
                        period = "تدریسی سال $academicYear (مکمل سیشن)",
                        totalSessions = totalLectures
                    )
                    viewModel.generateAndOpenPdf(context, meta, classStats)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_export_annual_class_pdf"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("پورے تدریسی سال کا کلاس رجسٹر PDF ڈاؤن لوڈ کریں", fontSize = 13.sp, color = Color.White)
            }
        }

        // List Header
        item {
            Text(
                text = "طلبہ کی سالانہ حاضری کی تفصیلات (حاضری، غیر حاضری اور تناسب):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CollegeNavyDark
            )
        }

        // Student Stats Items
        if (filteredStats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "کوئی ریکارڈ نہیں ملا۔",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredStats, key = { it.studentId }) { stat ->
                AnnualStudentRowCard(
                    stat = stat,
                    onClick = { onStudentClick(stat) }
                )
            }
        }
    }
}

@Composable
fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun AnnualStudentRowCard(
    stat: StudentAttendanceStat,
    onClick: () -> Unit
) {
    val isEligible = stat.attendancePercentage >= 75

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_annual_student_${stat.rollNo}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Roll Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CollegeNavyPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${stat.rollNo}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavyDark
                    )
                    Text(
                        text = "ولدیت: ${stat.fatherName}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Eligibility Status Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isEligible) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEligible) "اہل (75%+)" else "شارٹ لسٹ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEligible) Color(0xFF166534) else Color(0xFF991B1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance, Absents, Total, and Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "کل ایام: ${stat.totalDays}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = "حاضر: ${stat.presentCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PresentGreen
                    )
                    Text(
                        text = "غیر حاضر: ${stat.absentCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AbsentRed
                    )
                }

                Text(
                    text = "تناسب: ${stat.attendancePercentage}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEligible) PresentGreen else AbsentRed
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (stat.attendancePercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isEligible) PresentGreen else AbsentRed,
                trackColor = Color(0xFFE2E8F0)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentWiseAnnualView(
    viewModel: MainViewModel,
    classes: List<AcademicClass>,
    selectedClass: AcademicClass?,
    academicYear: String,
    selectedStudent: Student?,
    teacher: TeacherProfile?,
    onSelectClass: (AcademicClass) -> Unit,
    onSelectStudent: (Student) -> Unit
) {
    val context = LocalContext.current
    val allStudents by viewModel.allStudents.collectAsState()

    var studentSearchInput by remember { mutableStateOf("") }
    var isSearchDropdownOpen by remember { mutableStateOf(false) }
    var showTimelineHistory by remember { mutableStateOf(false) }

    val currentClass = selectedClass ?: classes.firstOrNull()
    val student = selectedStudent ?: allStudents.firstOrNull()

    if (student == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "کوئی طالب علم منتخب نہیں ہے!",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )
        }
        return
    }

    // Collect Student Overall Stats
    val overallStats by viewModel.getStudentOverallStats(
        studentId = student.id,
        classId = currentClass?.id ?: 0L
    ).collectAsState(initial = null)

    // Collect Student Monthly Breakdown
    val monthlyStats by viewModel.getStudentMonthlyBreakdown(
        studentId = student.id,
        classId = currentClass?.id ?: 0L
    ).collectAsState(initial = emptyList())

    // Collect Student Attendance History Timeline
    val historyRecords by viewModel.getStudentAttendanceHistory(
        studentId = student.id,
        classId = currentClass?.id ?: 0L
    ).collectAsState(initial = emptyList())

    // Filter students for picker
    val matchingStudents = if (studentSearchInput.isBlank()) {
        allStudents.take(10)
    } else {
        allStudents.filter {
            it.name.contains(studentSearchInput, ignoreCase = true) ||
            it.rollNo.toString().contains(studentSearchInput) ||
            it.fatherName.contains(studentSearchInput, ignoreCase = true)
        }.take(10)
    }

    val totDays = overallStats?.totalDays ?: 0
    val presents = overallStats?.presentCount ?: 0
    val absents = overallStats?.absentCount ?: 0
    val ratioPct = overallStats?.attendancePercentage ?: 0
    val isEligible = ratioPct >= 75

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Student Quick Selector Search Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "طالب علم منتخب کریں (تلاش برائے نام، رول نمبر، یا والد کا نام):",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentSearchInput,
                        onValueChange = {
                            studentSearchInput = it
                            isSearchDropdownOpen = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_picker"),
                        placeholder = { Text("مثلاً 101 یا احمد یا محمد...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CollegeNavyPrimary
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (isSearchDropdownOpen && matchingStudents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                matchingStudents.forEach { st ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectStudent(st)
                                                studentSearchInput = ""
                                                isSearchDropdownOpen = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "رول ${st.rollNo} — ${st.name} ولد ${st.fatherName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CollegeNavyDark
                                        )
                                        Text(
                                            text = st.className,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Student Profile & Dossier Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(CollegeNavyPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${student.rollNo}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ولدیت: ${student.fatherName}",
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "کلاس: ${student.className} | تدریسی سال: $academicYear",
                                fontSize = 11.5.sp,
                                color = CollegeSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Annual Attendance Ratio Highlight Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEligible) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isEligible) PresentGreen else AbsentRed)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "پوری تدریسی سال کی حاضری کا تناسب",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$presents حاضری / $totDays کل ایام",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Text(
                                    text = "$ratioPct%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEligible) PresentGreen else AbsentRed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (ratioPct / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isEligible) PresentGreen else AbsentRed,
                                trackColor = Color(0xFFE2E8F0)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Board Eligibility assessment
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isEligible) Icons.Default.Check else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isEligible) PresentGreen else AbsentRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEligible)
                                        "اہل برائے امتحانات: حاضری پنجاب ہائر ایجوکیشن کی مطلوبہ 75 فیصد شرط پر پوری اترتی ہے۔"
                                    else
                                        "تنبیہ (شارٹ اٹینڈنس): حاضری 75 فیصد سے کم ہے۔ داخلہ روکنے کا خطرہ موجود ہے۔",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isEligible) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Badges: کل لیکچرز، حاضر، غیر حاضر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = "کل لیکچرز",
                            value = "$totDays",
                            bgColor = Color(0xFFF8FAFC),
                            textColor = CollegeNavyDark
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = "کل حاضری",
                            value = "$presents",
                            bgColor = Color(0xFFF0FDF4),
                            textColor = PresentGreen
                        )
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = "کل غیر حاضری",
                            value = "$absents",
                            bgColor = Color(0xFFFEF2F2),
                            textColor = AbsentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parent Communication Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                CommunicationHelper.callParent(context, student.contactNo)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = PresentGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کال کریں", fontSize = 12.sp, color = PresentGreen)
                        }

                        OutlinedButton(
                            onClick = {
                                val msg = "السلام علیکم! گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان سے طالب علم ${student.name} ولد ${student.fatherName} (رول نمبر: ${student.rollNo}) کا سالانہ تدریسی ریکارڈ برائے سیشن $academicYear: کل منعقدہ ایام: $totDays، حاضری: $presents، غیر حاضری: $absents، سالانہ حاضری کا تناسب: $ratioPct فیصد۔"
                                CommunicationHelper.openWhatsApp(context, student.contactNo, msg)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واٹس ایپ", fontSize = 12.sp, color = Color(0xFF25D366))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export Individual Annual PDF Slip
                    Button(
                        onClick = {
                            viewModel.generateAndOpenStudentAnnualPdf(
                                context = context,
                                student = student,
                                className = student.className,
                                subject = currentClass?.subject ?: "تمام مضامین",
                                academicYear = academicYear,
                                summary = overallStats,
                                monthlyStats = monthlyStats
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_student_annual_pdf"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طالب علم کا سالانہ حاضری سرٹیفکیٹ / سلپ PDF ڈاؤن لوڈ کریں", fontSize = 12.5.sp, color = Color.White)
                    }
                }
            }
        }

        // Month-by-Month Breakdown Table for Academic Year
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "تدریسی سال کا ماہ بہ ماہ حاضری بریک ڈاؤن:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavyDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (monthlyStats.isEmpty()) {
                        Text(
                            text = "ابھی تک کوئی ماہانہ ریکارڈ موجود نہیں ہے۔",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مہینہ", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = CollegeNavyDark)
                            Text("کل ایام", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = CollegeNavyDark)
                            Text("حاضر", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PresentGreen)
                            Text("غیر حاضر", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AbsentRed)
                            Text("تناسب %", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
                        }

                        monthlyStats.forEachIndexed { idx, mStat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatUrduMonth(mStat.monthPrefix),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CollegeNavyDark
                                )
                                Text("${mStat.totalDays}", fontSize = 12.sp, color = Color(0xFF334155))
                                Text("${mStat.presentCount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PresentGreen)
                                Text("${mStat.absentCount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AbsentRed)
                                Text("${mStat.attendancePercentage}%", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (mStat.attendancePercentage >= 75) PresentGreen else AbsentRed)
                            }
                        }
                    }
                }
            }
        }

        // Expandable Date-wise Timeline Log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimelineHistory = !showTimelineHistory },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تاریخ وار تفصیلی لاگ (${historyRecords.size} لیکچرز)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyDark
                        )
                        IconButton(onClick = { showTimelineHistory = !showTimelineHistory }) {
                            Icon(
                                imageVector = if (showTimelineHistory) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = CollegeNavyPrimary
                            )
                        }
                    }

                    AnimatedVisibility(visible = showTimelineHistory) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (historyRecords.isEmpty()) {
                                Text("کوئی لاگ دستیاب نہیں ہے", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                historyRecords.forEach { rec ->
                                    val isPres = rec.status == "PRESENT"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isPres) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rec.date,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = CollegeNavyDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isPres) PresentGreen else AbsentRed,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isPres) "حاضر" else "غیر حاضر",
                                                fontSize = 10.5.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
