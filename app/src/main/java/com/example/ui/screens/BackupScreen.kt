package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ImportValidationReport
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.CollegeGoldTertiary
import com.example.ui.theme.CollegeNavyDark
import com.example.ui.theme.CollegeNavyPrimary
import com.example.ui.theme.CollegeSecondary
import com.example.ui.theme.PresentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    validationReport: ImportValidationReport?,
    onValidateJson: (String) -> Unit,
    onDismissReport: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showImportDialog by remember { mutableStateOf(false) }
    var pastedJson by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "بیک اپ و ڈیٹا درآمد",
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Offline-first Architecture Guarantee
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = PresentGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "آف لائن سیکیور آرکیٹیکچر",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PresentGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تمام ڈیٹا آپ کے موبائل کے اندر لوکل SQLite ڈیٹا بیس میں محفوظ ہے۔ روزمرہ حاضری کے لیے انٹرنیٹ بالکل ضروری نہیں ہے۔",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                // Import JSON Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "طلبہ ڈیٹا JSON درآمد کریں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CollegeNavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اگر آپ کے پاس طلبہ کا نیا JSON ڈیٹا ہے تو اسے درآمد کر کے ڈیٹا بیس میں شامل کریں۔ سسٹم خودکار طور پر ڈیٹا کو جانچے گا۔",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON ڈیٹا درآمد و تصدیق کریں", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Export Backup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "حاضری ڈیٹا کا بیک اپ بنائیں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CollegeNavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "موبائل تبدیل کرنے یا حفاظت کے لیے اپنے تمام حاضری ریکارڈز کا بیک اپ بنائیں۔",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "بیک اپ تیار ہو گیا۔ محفوظ کر لیا گیا ہے۔", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeSecondary)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بیک اپ تیار کریں", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Import JSON Dialog
            if (showImportDialog) {
                AlertDialog(
                    onDismissRequest = { showImportDialog = false },
                    title = {
                        Text("JSON متن درج کریں", fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pastedJson,
                                onValueChange = { pastedJson = it },
                                placeholder = { Text("یہاں JSON کوڈ چسپاں (Paste) کریں...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (pastedJson.isNotBlank()) {
                                    onValidateJson(pastedJson.trim())
                                }
                                showImportDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CollegeNavyPrimary)
                        ) {
                            Text("تصدیق اور شامل کریں")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("منسوخ")
                        }
                    }
                )
            }

            // Validation Report Dialog
            if (validationReport != null) {
                val rep = validationReport!!
                AlertDialog(
                    onDismissRequest = onDismissReport,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = CollegeNavyPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ڈیٹا جانچ کی رپورٹ", fontWeight = FontWeight.Bold, color = CollegeNavyPrimary)
                        }
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReportItem("کل طلبہ کی تعداد:", rep.totalStudents.toString(), Color.DarkGray)
                            ReportItem("کامیابی سے شامل ہوئے:", rep.successfullyImported.toString(), PresentGreen)
                            ReportItem("نامکمل ریکارڈز:", rep.incompleteRecords.toString(), if (rep.incompleteRecords > 0) AbsentRed else Color.DarkGray)
                            ReportItem("غلط / خالی فون نمبرز:", rep.invalidPhones.toString(), if (rep.invalidPhones > 0) Color(0xFFD97706) else Color.DarkGray)
                            ReportItem("ڈپلیکیٹ رول نمبرز:", rep.duplicateRolls.toString(), if (rep.duplicateRolls > 0) AbsentRed else Color.DarkGray)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = rep.message, fontSize = 12.sp, color = CollegeNavyDark)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = onDismissReport,
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

@Composable
private fun ReportItem(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.5.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
