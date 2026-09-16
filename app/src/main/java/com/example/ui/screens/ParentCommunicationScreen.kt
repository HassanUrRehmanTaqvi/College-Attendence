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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.entity.AcademicClass
import com.example.data.entity.Student
import com.example.data.entity.TeacherProfile
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen
import com.example.util.CommunicationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCommunicationScreen(
    absentStudents: List<Student>,
    selectedClass: AcademicClass?,
    selectedDate: String,
    teacher: TeacherProfile?,
    onUpdatePhone: (Long, String) -> Unit,
    getStudentStats: suspend (Long, Long) -> Pair<Int, Int>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var studentToEditPhone by remember { mutableStateOf<Student?>(null) }
    var editedPhoneNumber by remember { mutableStateOf("") }
    var previewMessageStudent by remember { mutableStateOf<Student?>(null) }
    var previewMessageContent by remember { mutableStateOf("") }

    // Map to cache stats (studentId -> Pair(presentDays, absentDays))
    var statsMap by remember { mutableStateOf<Map<Long, Pair<Int, Int>>>(emptyMap()) }

    LaunchedEffect(absentStudents, selectedClass) {
        if (selectedClass != null) {
            val newMap = mutableMapOf<Long, Pair<Int, Int>>()
            absentStudents.forEach { st ->
                val counts = getStudentStats(st.id, selectedClass.id)
                newMap[st.id] = counts
            }
            statsMap = newMap
        }
    }

    val teacherDisplayName = teacher?.formattedDisplayName ?: "پروفیسر صاحب"
    val teacherDept = teacher?.department ?: "کالج"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "والدین سے رابطہ — غیر حاضر طلبہ",
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Info Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedClass?.let { "${it.name} (${it.section})" } ?: "کلاس",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavyPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تاریخ: $selectedDate • مضمون: ${selectedClass?.subject ?: ""}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (absentStudents.isEmpty()) Color(0xFFDCFCE7) else AbsentRedLight
                        ) {
                            Text(
                                text = "${absentStudents.size} غیر حاضر",
                                color = if (absentStudents.isEmpty()) PresentGreen else AbsentRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (absentStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PresentGreen,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "ماشاءاللہ! تمام طلبہ حاضر ہیں۔",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اس تاریخ کے لیے کسی طالب علم کے والدین کو اطلاع بھیجنے کی ضرورت نہیں ہے۔",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(bottom = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(absentStudents, key = { it.id }) { student ->
                            val stats = statsMap[student.id] ?: Pair(0, 0)
                            val studentClassTitle = selectedClass?.name ?: student.className

                            val formattedMsg = CommunicationHelper.formatParentMessage(
                                studentName = student.name,
                                rollNo = student.rollNo,
                                className = studentClassTitle,
                                date = selectedDate,
                                presentDays = stats.first,
                                absentDays = stats.second + 1, // Including today
                                teacherDisplayName = teacherDisplayName,
                                department = teacherDept
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Student Identity Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = AbsentRed,
                                                modifier = Modifier.size(36.dp)
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

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = student.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    text = "والد کا نام: ${student.fatherName}",
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }

                                        // Preview message button
                                        IconButton(onClick = {
                                            previewMessageStudent = student
                                            previewMessageContent = formattedMsg
                                        }) {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = "پیغام دیکھیں",
                                                tint = CollegeNavyPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Phone and Monthly Stats Row
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (student.contactNo.isNotBlank()) "فون: ${student.contactNo}" else "فون نمبر موجود نہیں",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (student.contactNo.isNotBlank()) CollegeNavyDark else AbsentRed
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        studentToEditPhone = student
                                                        editedPhoneNumber = student.contactNo
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "نمبر تبدیل کریں",
                                                        tint = CollegeSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "اس ماہ: ${stats.first} حاضر | ${stats.second + 1} غیر حاضر",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action Buttons: Call, SMS, WhatsApp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // 1. Call Button
                                        Button(
                                            onClick = {
                                                CommunicationHelper.callParent(context, student.contactNo)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CollegeNavyPrimary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("کال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // 2. SMS Button
                                        Button(
                                            onClick = {
                                                CommunicationHelper.sendSms(context, student.contactNo, formattedMsg)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CollegeSecondary,
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("SMS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // 3. WhatsApp Button
                                        Button(
                                            onClick = {
                                                CommunicationHelper.openWhatsApp(context, student.contactNo, formattedMsg)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF25D366),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Edit Phone Dialog
            if (studentToEditPhone != null) {
                AlertDialog(
                    onDismissRequest = { studentToEditPhone = null },
                    title = {
                        Text(
                            text = "موبائل نمبر درست کریں",
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyPrimary
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "${studentToEditPhone?.name} (رول نمبر: ${studentToEditPhone?.rollNo})",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editedPhoneNumber,
                                onValueChange = { editedPhoneNumber = it },
                                label = { Text("والد کا موبائل نمبر") },
                                placeholder = { Text("03001234567") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "نیا نمبر محفوظ کرنے سے مرکزی ڈیٹا بیس میں ہمیشہ کے لیے اپ ڈیٹ ہو جائے گا۔",
                                fontSize = 11.sp,
                                color = CollegeSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                studentToEditPhone?.let { st ->
                                    onUpdatePhone(st.id, editedPhoneNumber)
                                }
                                studentToEditPhone = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                        ) {
                            Text("محفوظ کریں")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { studentToEditPhone = null }) {
                            Text("منسوخ")
                        }
                    }
                )
            }

            // Message Preview Dialog
            if (previewMessageStudent != null) {
                AlertDialog(
                    onDismissRequest = { previewMessageStudent = null },
                    title = {
                        Text(
                            text = "اردو پیغام کا پیش منظر",
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyPrimary
                        )
                    },
                    text = {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = previewMessageContent,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { previewMessageStudent = null },
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                        ) {
                            Text("ٹھیک ہے")
                        }
                    }
                )
            }
        }
    }
}
