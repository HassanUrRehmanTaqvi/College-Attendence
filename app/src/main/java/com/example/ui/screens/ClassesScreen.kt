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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AcademicClass
import com.example.data.entity.Student
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    classes: List<AcademicClass>,
    allStudents: List<Student>,
    onCreateClass: (String, String, String, String, String, List<Long>) -> Unit,
    onDeleteClass: (AcademicClass) -> Unit,
    onSelectClassDetails: (AcademicClass) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<AcademicClass?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "میری کلاسز",
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
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CollegeNavyPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "نئی کلاس شامل کریں")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (classes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                tint = CollegeSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "فی الحال کوئی کلاس درج نہیں ہے۔",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavyPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "نیچے + والے بٹن پر کلک کر کے نئی کلاس شامل کریں اور طلبہ کو منتخب کریں۔",
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
                            .padding(14.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(classes, key = { it.id }) { cls ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectClassDetails(cls) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFEFF6FF),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = CollegeNavyPrimary,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "${cls.name} (${cls.section})",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CollegeNavyPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "مضمون: ${cls.subject} • ${cls.grade}",
                                                fontSize = 12.sp,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = "سیشن: ${cls.session}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { classToDelete = cls }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "کلاس حذف کریں",
                                                tint = AbsentRed.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Class Dialog
            if (showAddDialog) {
                CreateClassDialog(
                    allStudents = allStudents,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, grade, section, subject, session, selectedStudentIds ->
                        onCreateClass(name, grade, section, subject, session, selectedStudentIds)
                        showAddDialog = false
                    }
                )
            }

            // Delete Class Confirmation
            if (classToDelete != null) {
                AlertDialog(
                    onDismissRequest = { classToDelete = null },
                    title = {
                        Text("کلاس حذف کریں", fontWeight = FontWeight.Bold, color = AbsentRed)
                    },
                    text = {
                        Text(
                            "کیا آپ واقعی کلاس \"${classToDelete?.name}\" کو حذف کرنا چاہتے ہیں؟\n\nنوٹ: طلبہ کا مرکزی ریکارڈ محفوظ رہے گا، صرف یہ کلاس اور اس کی حاضری شیٹ حذف ہو گی۔"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                classToDelete?.let { onDeleteClass(it) }
                                classToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                        ) {
                            Text("حذف کریں")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { classToDelete = null }) {
                            Text("منسوخ")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateClassDialog(
    allStudents: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, List<Long>) -> Unit
) {
    var subject by remember { mutableStateOf("اسلامیات") }
    var grade by remember { mutableStateOf("گیارہویں (1st Year)") }
    var section by remember { mutableStateOf("سیکشن A") }
    var session by remember { mutableStateOf("2026-2028") }
    var className by remember { mutableStateOf("11th اسلامیات A") }

    // Student selection mode: 11th, 12th, or custom
    var selectedGradeKey by remember { mutableStateOf("11th_class") }
    var selectedStudentIds by remember {
        mutableStateOf(
            allStudents.filter { it.classKey == "11th_class" }.map { it.id }.toSet()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("نئی کلاس بنائیں", fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("کلاس کا نام") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        className = "$grade $it $section"
                    },
                    label = { Text("مضمون") },
                    placeholder = { Text("مثال: اسلامیات") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = section,
                        onValueChange = {
                            section = it
                            className = "$grade $subject $it"
                        },
                        label = { Text("سیکشن") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = session,
                        onValueChange = { session = it },
                        label = { Text("سیشن") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "طلبہ کا انتخاب:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavyPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Fast grade selector chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedGradeKey == "11th_class") CollegeNavyPrimary else Color(0xFFE2E8F0),
                        modifier = Modifier.clickable {
                            selectedGradeKey = "11th_class"
                            grade = "گیارہویں (1st Year)"
                            className = "11th $subject $section"
                            selectedStudentIds = allStudents.filter { it.classKey == "11th_class" }.map { it.id }.toSet()
                        }
                    ) {
                        Text(
                            text = "گیارہویں کے تمام (${allStudents.count { it.classKey == "11th_class" }})",
                            fontSize = 11.sp,
                            color = if (selectedGradeKey == "11th_class") Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedGradeKey == "12th_class") CollegeNavyPrimary else Color(0xFFE2E8F0),
                        modifier = Modifier.clickable {
                            selectedGradeKey = "12th_class"
                            grade = "بارہویں (2nd Year)"
                            className = "12th $subject $section"
                            selectedStudentIds = allStudents.filter { it.classKey == "12th_class" }.map { it.id }.toSet()
                        }
                    ) {
                        Text(
                            text = "بارہویں کے تمام (${allStudents.count { it.classKey == "12th_class" }})",
                            fontSize = 11.sp,
                            color = if (selectedGradeKey == "12th_class") Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${selectedStudentIds.size} طلبہ شامل ہوں گے",
                    fontSize = 12.sp,
                    color = CollegeSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        className.trim(),
                        grade.trim(),
                        section.trim(),
                        subject.trim(),
                        session.trim(),
                        selectedStudentIds.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
            ) {
                Text("کلاس بنائیں")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("منسوخ")
            }
        }
    )
}
