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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Student
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.util.CommunicationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDatabaseScreen(
    students: List<Student>,
    onUpdatePhone: (Long, String) -> Unit,
    onViewAnnualRecord: ((Student) -> Unit)? = null,
    onOpenAuthorizedSearch: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, 11th, 12th
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var isEditingPhone by remember { mutableStateOf(false) }
    var editedPhone by remember { mutableStateOf("") }

    val filtered = students.filter { student ->
        val matchesClass = when (selectedFilter) {
            "11th" -> student.classKey == "11th_class"
            "12th" -> student.classKey == "12th_class"
            else -> true
        }
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                student.rollNo.toString().contains(searchQuery) ||
                student.fatherName.contains(searchQuery, ignoreCase = true) ||
                student.contactNo.contains(searchQuery)

        matchesClass && matchesSearch
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مرکزی طلبہ ریکارڈ (${students.size} طلبہ)",
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
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("نام، رول نمبر، ولدیت یا موبائل سے تلاش کریں...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("تمام طلبہ (${students.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CollegeNavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedFilter == "11th",
                        onClick = { selectedFilter = "11th" },
                        label = { Text("گیارہویں (${students.count { it.classKey == "11th_class" }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CollegeNavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedFilter == "12th",
                        onClick = { selectedFilter = "12th" },
                        label = { Text("بارہویں (${students.count { it.classKey == "12th_class" }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CollegeNavyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Student List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStudentForDetail = student
                                    editedPhone = student.contactNo
                                    isEditingPhone = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                            color = CollegeNavyDark
                                        )
                                        Text(
                                            text = "ولدیت: ${student.fatherName}",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "${student.className} • فون: ${if (student.contactNo.isNotBlank()) student.contactNo else "خالی"}",
                                            fontSize = 11.5.sp,
                                            color = if (student.contactNo.isNotBlank()) CollegeSecondary else AbsentRed
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        CommunicationHelper.callParent(context, student.contactNo)
                                    }) {
                                        Icon(
                                            Icons.Default.Call,
                                            contentDescription = "کال کریں",
                                            tint = CollegeNavyPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Student Detail & Phone Update Dialog
            if (selectedStudentForDetail != null) {
                val st = selectedStudentForDetail!!

                AlertDialog(
                    onDismissRequest = { selectedStudentForDetail = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = CollegeNavyPrimary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = st.rollNo.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = st.name, fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            DetailRow(label = "ولدیت:", value = st.fatherName)
                            DetailRow(label = "کلاس:", value = st.className)
                            DetailRow(label = "سیشن:", value = st.session)

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isEditingPhone) {
                                OutlinedTextField(
                                    value = editedPhone,
                                    onValueChange = { editedPhone = it },
                                    label = { Text("نیا موبائل نمبر") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    DetailRow(
                                        label = "موبائل نمبر:",
                                        value = if (st.contactNo.isNotBlank()) st.contactNo else "نمبر محفوظ نہیں ہے"
                                    )
                                    IconButton(onClick = { isEditingPhone = true }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "تبدیل کریں",
                                            tint = CollegeSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Call, SMS, WhatsApp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { CommunicationHelper.callParent(context, st.contactNo) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("کال", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        CommunicationHelper.sendSms(
                                            context,
                                            st.contactNo,
                                            "محترم والد صاحب! آپ کے صاحبزادے ${st.name} کے تعلیمی ریکارڈ کے حوالے سے گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان سے رابطہ کیا جا رہا ہے۔"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CollegeSecondary)
                                ) {
                                    Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SMS", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        CommunicationHelper.openWhatsApp(
                                            context,
                                            st.contactNo,
                                            "السلام علیکم ورحمۃ اللہ! گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 10.sp)
                                }
                            }

                            if (onViewAnnualRecord != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        selectedStudentForDetail = null
                                        onViewAnnualRecord(st)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("پوری تدریسی سال کا تفصیلی حاضری ریکارڈ دیکھیں", fontSize = 12.sp, color = CollegeNavyPrimary)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (isEditingPhone) {
                            Button(
                                onClick = {
                                    onUpdatePhone(st.id, editedPhone.trim())
                                    selectedStudentForDetail = null
                                    isEditingPhone = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                            ) {
                                Text("محفوظ کریں")
                            }
                        } else {
                            Button(
                                onClick = { selectedStudentForDetail = null },
                                colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                            ) {
                                Text("بند کریں")
                            }
                        }
                    },
                    dismissButton = {
                        if (isEditingPhone) {
                            TextButton(onClick = { isEditingPhone = false }) {
                                Text("منسوخ")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CollegeNavyDark)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray)
    }
}
