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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AcademicClass
import com.example.data.entity.Student
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    academicClass: AcademicClass,
    enrolledStudents: List<Student>,
    allStudents: List<Student>,
    onRemoveStudent: (Long) -> Unit,
    onAddStudents: (List<Long>) -> Unit,
    onBack: () -> Unit
) {
    var studentToRemove by remember { mutableStateOf<Student?>(null) }
    var showAddMoreDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = enrolledStudents.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.rollNo.toString().contains(searchQuery) ||
                it.fatherName.contains(searchQuery, ignoreCase = true)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = academicClass.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${academicClass.subject} • ${academicClass.section} • ${enrolledStudents.size} طلبہ",
                                fontSize = 12.sp,
                                color = Color(0xFFDBEAFE)
                            )
                        }
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
                    onClick = { showAddMoreDialog = true },
                    containerColor = CollegeNavyPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "مزید طلبہ شامل کریں")
                }
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
                    placeholder = { Text("رول نمبر یا نام سے تلاش کریں...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = student.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CollegeNavyDark
                                        )
                                        Text(
                                            text = "ولدیت: ${student.fatherName} • فون: ${student.contactNo}",
                                            fontSize = 11.5.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(onClick = { studentToRemove = student }) {
                                    Icon(
                                        Icons.Default.PersonRemove,
                                        contentDescription = "کلاس سے نکالیں",
                                        tint = AbsentRed.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Remove confirmation dialog (strictly specifying student remains in central database!)
            if (studentToRemove != null) {
                AlertDialog(
                    onDismissRequest = { studentToRemove = null },
                    title = {
                        Text("کلاس سے نکالیں", fontWeight = FontWeight.Bold, color = AbsentRed)
                    },
                    text = {
                        Text(
                            "کیا آپ واقعی ${studentToRemove?.name} (رول نمبر: ${studentToRemove?.rollNo}) کو اس کلاس سے نکالنا چاہتے ہیں؟\n\nنوٹ: طالب علم صرف اس کلاس سے نکالا جائے گا، وہ کالج کے مرکزی ڈیٹا بیس میں بدستور محفوظ رہے گا۔"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                studentToRemove?.let { onRemoveStudent(it.id) }
                                studentToRemove = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                        ) {
                            Text("کلاس سے نکالیں")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { studentToRemove = null }) {
                            Text("منسوخ")
                        }
                    }
                )
            }

            // Add More Students Dialog
            if (showAddMoreDialog) {
                val enrolledIds = enrolledStudents.map { it.id }.toSet()
                val unenrolledStudents = allStudents.filter { !enrolledIds.contains(it.id) }
                var selectedNewIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
                var dialogSearch by remember { mutableStateOf("") }

                val searchList = unenrolledStudents.filter {
                    it.name.contains(dialogSearch, ignoreCase = true) ||
                            it.rollNo.toString().contains(dialogSearch) ||
                            it.className.contains(dialogSearch, ignoreCase = true)
                }

                AlertDialog(
                    onDismissRequest = { showAddMoreDialog = false },
                    title = {
                        Text("مزید طلبہ شامل کریں", fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = dialogSearch,
                                onValueChange = { dialogSearch = it },
                                placeholder = { Text("تلاش کریں...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${selectedNewIds.size} طلبہ منتخب ہوئے", fontSize = 12.sp, color = CollegeSecondary)
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyColumn(modifier = Modifier.height(260.dp)) {
                                items(searchList, key = { it.id }) { st ->
                                    val isChecked = selectedNewIds.contains(st.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                selectedNewIds = if (isChecked) {
                                                    selectedNewIds - st.id
                                                } else {
                                                    selectedNewIds + st.id
                                                }
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${st.rollNo} - ${st.name}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${st.className} • ${st.fatherName}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (selectedNewIds.isNotEmpty()) {
                                    onAddStudents(selectedNewIds.toList())
                                }
                                showAddMoreDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                        ) {
                            Text("شامل کریں")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddMoreDialog = false }) {
                            Text("منسوخ")
                        }
                    }
                )
            }
        }
    }
}
