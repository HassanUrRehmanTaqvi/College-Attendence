package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AcademicClass
import com.example.data.entity.TeacherProfile
import com.example.R
import com.example.ui.Screen
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen

@Composable
fun DashboardScreen(
    teacher: TeacherProfile?,
    classes: List<AcademicClass>,
    studentCount: Int,
    todayUrduDate: String,
    onNavigate: (String) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Banner
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            CollegeNavyPrimary.copy(alpha = 0.04f),
                                            Color.White
                                        )
                                    )
                                )
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // College Crest Logo
                                Surface(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .shadow(4.dp, CircleShape),
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
                                            modifier = Modifier.size(54.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "گورنمنٹ ایسوسی ایٹ کالج",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollegeNavyPrimary
                                    )
                                    Text(
                                        text = "مخدوم رشید ملتان",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollegeGoldTertiary
                                    )
                                    Text(
                                        text = "اساتذہ حاضری نظام",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Teacher Info Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CollegeNavyPrimary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = teacher?.formattedDisplayName ?: "پروفیسر صاحب",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "شعبہ: ${teacher?.department ?: "عام"}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFDBEAFE)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = todayUrduDate,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Student Search for Authorized Teachers (Full Width)
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.AUTHORIZED_STUDENT_SEARCH) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFEFF6FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "تلاش",
                                    tint = CollegeNavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "طلبہ سرچ (صرف مجاز اساتذہ)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavyDark
                                )
                                Text(
                                    text = "نام، رول نمبر، یا والد کے نام سے تلاش کریں...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFFDF5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PresentGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("محفوظ", fontSize = 10.sp, color = PresentGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Primary Action: Take Attendance (Full Width)
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.TAKE_ATTENDANCE) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CollegeNavyPrimary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CollegeGoldTertiary.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "روزمرہ حاضری",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "حاضری لگائیں",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (classes.isEmpty()) "پہلے کلاس شامل کریں" else "${classes.size} فعال کلاسز دستیاب ہیں",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = "حاضری لگائیں",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Full Academic Year Attendance Record (Full Width)
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.ANNUAL_ATTENDANCE) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8FAFC)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CollegeNavyPrimary.copy(alpha = 0.3f))
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(CollegeGoldTertiary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = CollegeGoldTertiary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CollegeGoldTertiary.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "تفصیلی سالانہ حاضری",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = "پوری تدریسی سال کا تفصیلی ریکارڈ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CollegeNavyDark
                                )
                                Text(
                                    text = "کلاس و طالب علم کے لحاظ سے حاضری، غیر حاضری، اور حاضری کا تناسب %",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                // Grid Actions
                item {
                    DashboardCard(
                        title = "میری کلاسز",
                        subtitle = "${classes.size} کلاسز درج ہیں",
                        icon = Icons.Default.Groups,
                        iconColor = CollegeNavyPrimary,
                        bgColor = Color(0xFFEFF6FF),
                        onClick = { onNavigate(Screen.CLASSES) }
                    )
                }

                item {
                    DashboardCard(
                        title = "حاضری رپورٹس و PDF",
                        subtitle = "ماہانہ و سالانہ شیٹس",
                        icon = Icons.Default.PictureAsPdf,
                        iconColor = Color(0xFFDC2626),
                        bgColor = Color(0xFFFEF2F2),
                        onClick = { onNavigate(Screen.REPORTS) }
                    )
                }

                item {
                    DashboardCard(
                        title = "طلبہ ڈیٹا بیس",
                        subtitle = "$studentCount طلبہ کا ریکارڈ",
                        icon = Icons.Default.MenuBook,
                        iconColor = PresentGreen,
                        bgColor = Color(0xFFF0FDF4),
                        onClick = { onNavigate(Screen.STUDENT_DATABASE) }
                    )
                }

                item {
                    DashboardCard(
                        title = "والدین سے رابطہ",
                        subtitle = "کال، SMS، واٹس ایپ",
                        icon = Icons.Default.ContactPhone,
                        iconColor = CollegeGoldTertiary,
                        bgColor = Color(0xFFFFFBEB),
                        onClick = { onNavigate(Screen.PARENT_COMMUNICATION) }
                    )
                }

                item {
                    DashboardCard(
                        title = "بیک اپ و ڈیٹا",
                        subtitle = "JSON درآمد و برآمد",
                        icon = Icons.Default.SaveAlt,
                        iconColor = Color(0xFF7C3AED),
                        bgColor = Color(0xFFF5F3FF),
                        onClick = { onNavigate(Screen.BACKUP) }
                    )
                }

                item {
                    DashboardCard(
                        title = "پروفائل و ترتیبات",
                        subtitle = "شعبہ و سیکیورٹی لاک",
                        icon = Icons.Default.Settings,
                        iconColor = CollegeNavyDark,
                        bgColor = Color(0xFFF1F5F9),
                        onClick = { onNavigate(Screen.PROFILE) }
                    )
                }

                // Footer Info
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PresentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آف لائن نظام: انٹرنیٹ کے بغیر تمام ریکارڈ موبائل میں مستقل محفوظ رہتا ہے۔",
                                fontSize = 11.5.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavyPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
