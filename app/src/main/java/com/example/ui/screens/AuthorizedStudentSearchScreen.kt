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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Student
import com.example.ui.MainViewModel
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen
import com.example.util.CommunicationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorizedStudentSearchScreen(
    viewModel: MainViewModel,
    onSelectStudentForAnnualRecord: (Student) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val teacher by viewModel.teacherProfile.collectAsState()
    val isAuthorized by viewModel.isTeacherAuthorized.collectAsState()
    val onlyMyClasses by viewModel.searchScopeOnlyMyClasses.collectAsState()

    var query by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showPinDialog by remember { mutableStateOf(false) }

    // Student phone edit dialog
    var studentForPhoneEdit by remember { mutableStateOf<Student?>(null) }
    var newPhoneInput by remember { mutableStateOf("") }

    // Fetch search results if authorized
    val searchResults by if (isAuthorized && query.isNotBlank()) {
        viewModel.getAuthorizedSearchResults(query).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Student>()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "طلبہ سرچ (صرف مجاز اساتذہ)",
                            fontSize = 18.sp,
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
                actions = {
                    if (isAuthorized) {
                        IconButton(
                            onClick = { viewModel.setTeacherAuthorized(false) },
                            modifier = Modifier.testTag("btn_lock_session")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "اختیار لاک کریں",
                                tint = CollegeGoldTertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CollegeNavyDark
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Authorization Status Banner
            if (isAuthorized) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEFFDF5)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PresentGreen.copy(alpha = 0.5f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = PresentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مجاز رسائی تصدیق شدہ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "پروفیسر: ${teacher?.name ?: "استاد محترم"} — شعبہ: ${teacher?.department ?: "کالج"}",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                        TextButton(
                            onClick = { viewModel.setTeacherAuthorized(false) },
                            modifier = Modifier.testTag("btn_relock")
                        ) {
                            Text("لاک کریں", fontSize = 11.sp, color = AbsentRed)
                        }
                    }
                }
            } else {
                // Restricted / Unauthorized Barrier Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF7ED)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF97316).copy(alpha = 0.5f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محدود سرچ — صرف متعلقہ مجاز اساتذہ کے لیے",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9A3412)
                            )
                            Text(
                                text = "طلبہ کے ڈیٹا کی رازداری کے لیے، سرچ نتائج صرف تصدیق شدہ اساتذہ کو دکھائی دیتے ہیں۔",
                                fontSize = 11.sp,
                                color = Color(0xFFC2410C)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                pinInput = ""
                                pinError = null
                                showPinDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CollegeNavyPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_authorize_now")
                        ) {
                            Text("تصدیق کریں", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Search Query Input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (!isAuthorized && it.isNotBlank()) {
                                showPinDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_student_search"),
                        label = { Text("طلبہ کو نام، رول نمبر یا والد کے نام سے تلاش کریں...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = CollegeNavyPrimary
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "صاف کریں",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Relevant Class Scope Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سرچ کا دائرہ کار:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )

                        FilterChip(
                            selected = onlyMyClasses,
                            onClick = { viewModel.setSearchScopeOnlyMyClasses(true) },
                            label = { Text("میری کلاسز کے طلبہ", fontSize = 11.sp) },
                            leadingIcon = {
                                if (onlyMyClasses) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CollegeNavyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_scope_my_classes")
                        )

                        FilterChip(
                            selected = !onlyMyClasses,
                            onClick = { viewModel.setSearchScopeOnlyMyClasses(false) },
                            label = { Text("مکمل کالج (مجاز)", fontSize = 11.sp) },
                            leadingIcon = {
                                if (!onlyMyClasses) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CollegeNavyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_scope_all_classes")
                        )
                    }
                }
            }

            // Search Results or Authorization Lock Area
            if (!isAuthorized) {
                // Blocked View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "سرچ کے نتائج مقفل ہیں",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollegeNavyDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "صرف گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان کے مجاز اساتذہ طلبہ کے نام، رول نمبر اور والد کے نام سے سرچ کر سکتے ہیں۔",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF64748B),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    pinInput = ""
                                    pinError = null
                                    showPinDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CollegeNavyPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_unlock_search")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("اپنا سیکیورٹی پن درج کریں اور ان لاک کریں", fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else if (query.isBlank()) {
                // Empty query placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تلاش شروع کریں",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اوپر سرچ بار میں طالب علم کا نام، رول نمبر، یا والد کا نام درج کریں۔",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                // No results found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "کوئی طالب علم نہیں ملا",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"$query\" سے مطابقت رکھنے والا کوئی ریکارڈ دستیاب نہیں ہے۔",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                // List of search results
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تلاش کے نتائج: ${searchResults.size} طلبہ مل گئے",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyPrimary
                        )
                        Text(
                            text = if (onlyMyClasses) "متعلقہ کلاسز" else "مکمل کالج",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(searchResults, key = { it.id }) { student ->
                            StudentSearchResultCard(
                                student = student,
                                onAnnualAttendanceClick = {
                                    onSelectStudentForAnnualRecord(student)
                                },
                                onCallClick = {
                                    CommunicationHelper.callParent(context, student.contactNo)
                                },
                                onWhatsAppClick = {
                                    val msg = "السلام علیکم! گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان سے طالب علم ${student.name} ولد ${student.fatherName} (رول نمبر: ${student.rollNo}) کے تعلیمی و حاضری ریکارڈ کے سلسلے میں رابطہ کیا جا رہا ہے۔"
                                    CommunicationHelper.openWhatsApp(context, student.contactNo, msg)
                                },
                                onEditPhoneClick = {
                                    studentForPhoneEdit = student
                                    newPhoneInput = student.contactNo
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Teacher PIN Authorization Dialog
    if (showPinDialog) {
        val hasExistingPin = !teacher?.pinCode.isNullOrBlank()
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = {
                Text(
                    text = if (hasExistingPin) "استاد کی سیکیورٹی تصدیق" else "مجاز پن کوڈ کا اندراج",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (hasExistingPin)
                            "طلبہ کے ریکارڈ کی تلاش کے لیے اپنا 4 ہندسی پن کوڈ درج فرمائیں:"
                        else
                            "سرچ کی رازداری کے لیے اپنا 4 ہندسی سیکیورٹی پن سیٹ کریں:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                pinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("4 ہندسی پن کوڈ") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dialog_pin"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pinError!!,
                            color = AbsentRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.verifyAndAuthorizeTeacherPin(pinInput)
                        if (success) {
                            showPinDialog = false
                            pinError = null
                        } else {
                            pinError = "غلط پن کوڈ! براہ کرم درست کوڈ درج کریں۔"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary),
                    modifier = Modifier.testTag("btn_confirm_pin")
                ) {
                    Text("تصدیق کریں", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("منسوخ کریں", color = Color.Gray)
                }
            }
        )
    }

    // Phone Number Edit Dialog
    studentForPhoneEdit?.let { st ->
        AlertDialog(
            onDismissRequest = { studentForPhoneEdit = null },
            title = {
                Text(
                    text = "طالب علم کا رابطہ نمبر اپ ڈیٹ کریں",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "${st.name} ولد ${st.fatherName} (رول نمبر: ${st.rollNo})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = { newPhoneInput = it },
                        label = { Text("موبائل نمبر (03xxxxxxxxx)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudentPhone(st.id, newPhoneInput.trim())
                        studentForPhoneEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                ) {
                    Text("محفوظ کریں", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForPhoneEdit = null }) {
                    Text("منسوخ کریں", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun StudentSearchResultCard(
    student: Student,
    onAnnualAttendanceClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEditPhoneClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_search_student_${student.rollNo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Roll Number Circle Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CollegeNavyPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.rollNo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CollegeNavyDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ولدیت: ${student.fatherName}",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "کلاس: ${student.className}",
                            fontSize = 11.sp,
                            color = CollegeSecondary
                        )
                        if (student.contactNo.isNotBlank()) {
                            Text(
                                text = "فون: ${student.contactNo}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary: View Annual Attendance
                Button(
                    onClick = onAnnualAttendanceClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_view_annual_${student.rollNo}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تدریسی سال کی حاضری", fontSize = 11.5.sp, color = Color.White)
                }

                // Call
                OutlinedButton(
                    onClick = onCallClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier.testTag("btn_call_${student.rollNo}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "کال",
                        tint = PresentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // WhatsApp
                OutlinedButton(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier.testTag("btn_wa_${student.rollNo}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "واٹس ایپ",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit Phone
                IconButton(
                    onClick = onEditPhoneClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_edit_phone_${student.rollNo}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ترمیم فون",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
