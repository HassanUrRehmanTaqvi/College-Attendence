package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary

@Composable
fun WelcomeScreen(
    onProfileSaved: (String, String, String?) -> Unit
) {
    var teacherName by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("اسلامیات") }
    var pinCode by remember { mutableStateOf("") }
    var expandedDept by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf(
        "اسلامیات",
        "اردو",
        "انگریزی",
        "فزکس",
        "کیمسٹری",
        "بیالوجی",
        "ریاضی",
        "کمپیوٹر سائنس",
        "مطالعہ پاکستان",
        "معاشیات",
        "تاریخ",
        "دیگر شعبہ"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // College Crest Emblem
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_college_logo),
                            contentDescription = "کالج مونوگرام",
                            modifier = Modifier.size(95.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // College Name Header
                Text(
                    text = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavyPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "اساتذہ حاضری نظام — Teacher Attendance System",
                        fontSize = 13.sp,
                        color = CollegeNavyDark,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Setup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "استاد کا پروفائل درج فرمائیں",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CollegeNavyPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "یہ معلومات حاضری شیٹس اور پیغامات میں استعمال ہوں گی",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Teacher Name Input
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = {
                                teacherName = it
                                errorMessage = null
                            },
                            label = { Text("استاد محترم کا نام") },
                            placeholder = { Text("مثال: حسن الرحمٰن تقوی") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = CollegeNavyPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CollegeNavyPrimary,
                                focusedLabelColor = CollegeNavyPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Auto Preview of Title
                        val previewTitle = if (teacherName.isNotBlank()) {
                            if (teacherName.startsWith("پروفیسر")) teacherName else "پروفیسر $teacherName صاحب"
                        } else {
                            "پروفیسر [نام] صاحب"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = CollegeGoldTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = "عنوان: $previewTitle",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CollegeNavyDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Department Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = department,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("شعبہ (Department)") },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, tint = CollegeNavyPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { expandedDept = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "منتخب کریں")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CollegeNavyPrimary,
                                    focusedLabelColor = CollegeNavyPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            DropdownMenu(
                                expanded = expandedDept,
                                onDismissRequest = { expandedDept = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept) },
                                        onClick = {
                                            department = dept
                                            expandedDept = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Optional PIN Code
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pinCode = it
                                }
                            },
                            label = { Text("4 ہندسوں کا پن کوڈ (اختیاری)") },
                            placeholder = { Text("سیکیورٹی لاک کے لیے") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = CollegeNavyPrimary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CollegeNavyPrimary,
                                focusedLabelColor = CollegeNavyPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (teacherName.trim().isEmpty()) {
                                    errorMessage = "براہِ کرم استاد کا نام درج فرمائیں۔"
                                } else {
                                    onProfileSaved(teacherName.trim(), department.trim(), pinCode.ifBlank { null })
                                }
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
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "ایپ شروع کریں",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer note
                Text(
                    text = "تاریخ قیام کالج: 27 دسمبر 1999 • آف لائن حاضری نظام",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
