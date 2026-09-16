package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.StudentAttendanceStat
import com.example.data.entity.AcademicClass
import com.example.data.entity.TeacherProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight
import com.example.util.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceReportsScreen(
    viewModel: MainViewModel,
    classes: List<AcademicClass>,
    teacher: TeacherProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedClass by remember { mutableStateOf(classes.firstOrNull()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var classDropdownOpen by remember { mutableStateOf(false) }

    val currentMonthPrefix = viewModel.currentMonthPrefix

    val statsFlow = remember(selectedClass, selectedTab) {
        val cls = selectedClass
        if (cls != null) {
            when (selectedTab) {
                0 -> viewModel.getDateRangeReport(cls.id, viewModel.todayDateString, viewModel.todayDateString)
                1 -> viewModel.getMonthlyReport(cls.id, currentMonthPrefix)
                else -> viewModel.getAllTimeReport(cls.id)
            }
        } else null
    }

    val statsList by (statsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val tabs = listOf("آج کی حاضری", "ماہانہ رپورٹ", "سیشن / سالانہ")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "حاضری ریکارڈز و PDF رپورٹ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "واپس")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CollegeNavyPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                if (selectedClass != null && statsList.isNotEmpty()) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = {
                                    val periodTitle = when (selectedTab) {
                                        0 -> "روزانہ رپورٹ (تاریخ: ${viewModel.todayDateString})"
                                        1 -> "ماہانہ رپورٹ ($currentMonthPrefix)"
                                        else -> "مکمل سیشن رپورٹ (${selectedClass?.session ?: ""})"
                                    }

                                    val meta = PdfReportGenerator.ReportMeta(
                                        title = "حاضری رپورٹ — $periodTitle",
                                        teacherName = teacher?.formattedDisplayName ?: "استاد محترم",
                                        department = teacher?.department ?: "کالج",
                                        className = "${selectedClass?.name} (${selectedClass?.section})",
                                        subject = selectedClass?.subject ?: "",
                                        period = periodTitle,
                                        totalSessions = statsList.firstOrNull()?.totalDays ?: 0
                                    )
                                    viewModel.generateAndOpenPdf(context, meta, statsList)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CollegeNavyPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PDF رپورٹ ڈاؤن لوڈ / شیئر کریں",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Class Selector Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "کلاس منتخب کریں:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CollegeNavyPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { classDropdownOpen = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedClass?.let { "${it.name} - ${it.subject} (${it.section})" }
                                            ?: "کلاس منتخب کریں",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedClass != null) CollegeNavyDark else Color.Gray
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = classDropdownOpen,
                                onDismissRequest = { classDropdownOpen = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                classes.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text("${cls.name} (${cls.section}) - ${cls.subject}") },
                                        onClick = {
                                            selectedClass = cls
                                            classDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = CollegeNavyPrimary
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                // Table Header / Stats Summary
                if (selectedClass != null && statsList.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("رول / طالب علم", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CollegeNavyPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("حاضر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PresentGreen)
                                Text("غیر حاضر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AbsentRed)
                                Text("فیصد %", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CollegeNavyDark)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(statsList, key = { it.studentId }) { stat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = CollegeNavyPrimary,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = stat.rollNo.toString(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = stat.name,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "ولدیت: ${stat.fatherName}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PresentGreenLight
                                        ) {
                                            Text(
                                                text = "${stat.presentCount}",
                                                color = PresentGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AbsentRedLight
                                        ) {
                                            Text(
                                                text = "${stat.absentCount}",
                                                color = AbsentRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = "${stat.attendancePercentage}%",
                                                color = CollegeNavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (classes.isEmpty()) "کوئی کلاس موجود نہیں ہے۔ پہلے کلاس بنائیں۔" else "اس مدت کا کوئی حاضری ریکارڈ دستیاب نہیں ہے۔",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
