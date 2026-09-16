package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AcademicClass
import com.example.data.entity.AttendanceSession
import com.example.data.entity.Student
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    classes: List<AcademicClass>,
    selectedClass: AcademicClass?,
    selectedDate: String,
    students: List<Student>,
    attendanceMap: Map<Long, Boolean>,
    existingWarning: AttendanceSession?,
    onSelectClass: (AcademicClass) -> Unit,
    onSelectDate: (String) -> Unit,
    onToggleAttendance: (Long) -> Unit,
    onMarkAllPresent: () -> Unit,
    onMarkAllAbsent: () -> Unit,
    onSaveAttendance: () -> Unit,
    onDismissWarning: () -> Unit,
    onBack: () -> Unit
) {
    var classDropdownOpen by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val presentCount = students.count { attendanceMap[it.id] != false }
    val absentCount = students.size - presentCount

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "حاضری درج کریں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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
                if (selectedClass != null && students.isNotEmpty()) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onSaveAttendance,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CollegeNavyPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حاضری محفوظ کریں ($presentCount حاضر • $absentCount غیر حاضر)",
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
                // Class & Date Selection Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Class Picker
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
                                            ?: "کلاس منتخب فرمائیں...",
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
                                if (classes.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("کوئی کلاس دستیاب نہیں ہے") },
                                        onClick = { classDropdownOpen = false }
                                    )
                                } else {
                                    classes.forEach { cls ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = "${cls.name} (${cls.section})",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "مضمون: ${cls.subject} • ${cls.grade}",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onSelectClass(cls)
                                                classDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Date display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = CollegeSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تاریخ: $selectedDate",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                // Existing Attendance Warning Banner
                if (existingWarning != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "اس تاریخ کی حاضری پہلے سے محفوظ ہے!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "آپ محفوظ ریکارڈ میں ترمیم کر سکتے ہیں۔ محفوظ کرنے پر نیا ریکارڈ اپ ڈیٹ ہو جائے گا۔",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                            IconButton(onClick = onDismissWarning) {
                                Icon(Icons.Default.Close, contentDescription = "بند کریں", tint = Color.Gray)
                            }
                        }
                    }
                }

                // Stats & Quick Actions
                if (selectedClass != null && students.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stats chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "کل: ${students.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PresentGreenLight
                            ) {
                                Text(
                                    text = "حاضر: $presentCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PresentGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AbsentRedLight
                            ) {
                                Text(
                                    text = "غیر حاضر: $absentCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AbsentRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Bulk toggle
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = onMarkAllPresent) {
                                Text("سب حاضر", fontSize = 12.sp, color = PresentGreen, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = onMarkAllAbsent) {
                                Text("سب غیر حاضر", fontSize = 12.sp, color = AbsentRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Student Attendance List
                if (selectedClass == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حاضری شروع کرنے کے لیے اوپر سے کلاس منتخب فرمائیں۔",
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                } else if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "اس کلاس میں فی الحال کوئی طالب علم شامل نہیں ہے۔",
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "کلاس مینجمنٹ میں جا کر طلبہ کو اس کلاس میں شامل کریں۔",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = CollegeSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students, key = { it.id }) { student ->
                            val isPresent = attendanceMap[student.id] != false

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleAttendance(student.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPresent) Color.White else Color(0xFFFFF1F2)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Roll No & Student Details
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Roll No Badge
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isPresent) CollegeNavyPrimary else AbsentRed,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = student.rollNo.toString(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = student.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = "ولدیت: ${student.fatherName}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            if (student.contactNo.isNotBlank()) {
                                                Text(
                                                    text = "فون: ${student.contactNo}",
                                                    fontSize = 11.sp,
                                                    color = CollegeSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Attendance Toggle Badge Button
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPresent) PresentGreen else AbsentRed,
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(40.dp)
                                            .clickable { onToggleAttendance(student.id) }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    if (isPresent) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isPresent) "حاضر" else "غیر حاضر",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
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
}
