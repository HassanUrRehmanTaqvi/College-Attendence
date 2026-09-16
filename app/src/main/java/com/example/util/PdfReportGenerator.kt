package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.dao.StudentAttendanceStat
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    data class ReportMeta(
        val title: String,
        val collegeName: String = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
        val teacherName: String,
        val department: String,
        val className: String,
        val subject: String,
        val period: String,
        val totalSessions: Int = 0
    )

    fun generateAttendancePdf(
        context: Context,
        meta: ReportMeta,
        stats: List<StudentAttendanceStat>
    ): File? {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points
            val itemsPerPage = 22
            val totalPages = if (stats.isEmpty()) 1 else ((stats.size - 1) / itemsPerPage) + 1

            val titlePaint = Paint().apply {
                color = Color.rgb(16, 54, 126) // Deep royal blue
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9.5f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(230, 240, 255)
                style = Paint.Style.FILL
            }

            val headerTextPaint = Paint().apply {
                color = Color.rgb(16, 54, 126)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 9f
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }

            val rowAltPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Outer decorative border
                val outerRect = RectF(20f, 20f, (pageWidth - 20).toFloat(), (pageHeight - 20).toFloat())
                val outerBorder = Paint().apply {
                    color = Color.rgb(16, 54, 126)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                }
                canvas.drawRect(outerRect, outerBorder)

                // Inner margin
                var y = 50f

                // Header
                canvas.drawText(meta.collegeName, (pageWidth / 2).toFloat(), y, titlePaint)
                y += 20f
                canvas.drawText(meta.title, (pageWidth / 2).toFloat(), y, subtitlePaint)
                y += 20f

                // Meta Info Box
                val metaBoxRect = RectF(35f, y, (pageWidth - 35).toFloat(), y + 42f)
                val metaBg = Paint().apply {
                    color = Color.rgb(241, 245, 249)
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(metaBoxRect, 6f, 6f, metaBg)
                canvas.drawRoundRect(metaBoxRect, 6f, 6f, borderPaint)

                // Row 1
                canvas.drawText("استاد: ${meta.teacherName}", 45f, y + 18f, metaPaint)
                canvas.drawText("شعبہ: ${meta.department}", 230f, y + 18f, metaPaint)
                canvas.drawText("کلاس: ${meta.className}", 400f, y + 18f, metaPaint)

                // Row 2
                canvas.drawText("مضمون: ${meta.subject}", 45f, y + 34f, metaPaint)
                canvas.drawText("مدت / تاریخ: ${meta.period}", 230f, y + 34f, metaPaint)
                canvas.drawText("صفحہ: ${pageIndex + 1} / $totalPages", 450f, y + 34f, metaPaint)

                y += 56f

                // Table Header
                val tableLeft = 35f
                val tableRight = (pageWidth - 35).toFloat()
                val rowHeight = 22f

                // Table columns: Roll (45), Name (150), Father (140), Total (45), Present (45), Absent (45), % (50)
                val cRoll = tableLeft
                val cName = tableLeft + 45f
                val cFather = cName + 155f
                val cTotal = cFather + 145f
                val cPres = cTotal + 45f
                val cAbs = cPres + 45f
                val cPct = cAbs + 45f

                val headerRect = RectF(tableLeft, y, tableRight, y + rowHeight)
                canvas.drawRect(headerRect, headerBgPaint)
                canvas.drawRect(headerRect, borderPaint)

                canvas.drawText("رول نمبر", cRoll + 5f, y + 15f, headerTextPaint)
                canvas.drawText("نام طالب علم", cName + 8f, y + 15f, headerTextPaint)
                canvas.drawText("والد کا نام", cFather + 8f, y + 15f, headerTextPaint)
                canvas.drawText("کل ایام", cTotal + 6f, y + 15f, headerTextPaint)
                canvas.drawText("حاضر", cPres + 10f, y + 15f, headerTextPaint)
                canvas.drawText("غیر حاضر", cAbs + 5f, y + 15f, headerTextPaint)
                canvas.drawText("فیصد %", cPct + 8f, y + 15f, headerTextPaint)

                y += rowHeight

                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(startIndex + itemsPerPage, stats.size)

                for (i in startIndex until endIndex) {
                    val stat = stats[i]
                    val rRect = RectF(tableLeft, y, tableRight, y + rowHeight)
                    if (i % 2 == 1) {
                        canvas.drawRect(rRect, rowAltPaint)
                    }
                    canvas.drawRect(rRect, borderPaint)

                    canvas.drawText(stat.rollNo.toString(), cRoll + 12f, y + 15f, textPaint)
                    canvas.drawText(stat.name, cName + 8f, y + 15f, textPaint)
                    canvas.drawText(stat.fatherName, cFather + 8f, y + 15f, textPaint)
                    canvas.drawText(stat.totalDays.toString(), cTotal + 15f, y + 15f, textPaint)
                    canvas.drawText(stat.presentCount.toString(), cPres + 16f, y + 15f, textPaint)
                    canvas.drawText(stat.absentCount.toString(), cAbs + 16f, y + 15f, textPaint)
                    canvas.drawText("${stat.attendancePercentage}%", cPct + 10f, y + 15f, textPaint)

                    y += rowHeight
                }

                // Signatures and Footer on last page
                if (pageIndex == totalPages - 1) {
                    val footerY = pageHeight - 65f
                    canvas.drawLine(tableLeft, footerY, tableLeft + 150f, footerY, borderPaint)
                    canvas.drawText("دستخط استاد محترم", tableLeft + 30f, footerY + 15f, metaPaint)

                    canvas.drawLine(tableRight - 150f, footerY, tableRight, footerY, borderPaint)
                    canvas.drawText("دستخط پرنسپل / وائس پرنسپل", tableRight - 145f, footerY + 15f, metaPaint)

                    val devCreditPaint = Paint().apply {
                        color = Color.rgb(100, 116, 139)
                        textSize = 8f
                        typeface = Typeface.DEFAULT
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.drawText("مرتب و تیار کردہ حاضری نظام: حسن الرحمٰن تقوی", (pageWidth / 2).toFloat(), pageHeight - 25f, devCreditPaint)
                }

                pdfDoc.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val file = File(reportsDir, "Attendance_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateStudentAnnualPdf(
        context: Context,
        student: com.example.data.entity.Student,
        teacherName: String,
        department: String,
        className: String,
        subject: String,
        academicYear: String,
        summary: com.example.data.dao.SimpleAttendanceSummary?,
        monthlyStats: List<com.example.data.dao.MonthlyStudentStat>
    ): File? {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val outerRect = RectF(20f, 20f, (pageWidth - 20).toFloat(), (pageHeight - 20).toFloat())
            val outerBorder = Paint().apply {
                color = Color.rgb(16, 54, 126)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(outerRect, outerBorder)

            val titlePaint = Paint().apply {
                color = Color.rgb(16, 54, 126)
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(202, 138, 4)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val headPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 10f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = Color.rgb(16, 54, 126)
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }

            var y = 55f
            canvas.drawText("گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان", (pageWidth / 2).toFloat(), y, titlePaint)
            y += 20f
            canvas.drawText("سالانہ تدریسی سیشن: $academicYear", (pageWidth / 2).toFloat(), y, subtitlePaint)
            y += 22f
            canvas.drawText("طالب علم کا سالانہ حاضری رپورٹ کارڈ و سرٹیفکیٹ", (pageWidth / 2).toFloat(), y, headPaint)
            y += 25f

            // Student Bio Card
            val bioRect = RectF(35f, y, (pageWidth - 35).toFloat(), y + 70f)
            val bioBg = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(bioRect, 8f, 8f, bioBg)
            canvas.drawRoundRect(bioRect, 8f, 8f, borderPaint)

            canvas.drawText("رول نمبر: ${student.rollNo}", 48f, y + 22f, boldPaint)
            canvas.drawText("نام طالب علم: ${student.name}", 180f, y + 22f, boldPaint)
            canvas.drawText("ولدیت: ${student.fatherName}", 380f, y + 22f, textPaint)

            canvas.drawText("کلاس / سیکشن: $className", 48f, y + 44f, textPaint)
            canvas.drawText("مضمون: $subject", 180f, y + 44f, textPaint)
            canvas.drawText("موبائل / رابطہ: ${student.contactNo}", 380f, y + 44f, textPaint)

            canvas.drawText("استاد محترم: $teacherName", 48f, y + 62f, textPaint)
            canvas.drawText("شعبہ: $department", 250f, y + 62f, textPaint)

            y += 85f

            // Annual Attendance Ratio Highlights
            val totDays = summary?.totalDays ?: 0
            val presents = summary?.presentCount ?: 0
            val absents = summary?.absentCount ?: 0
            val percentage = summary?.attendancePercentage ?: 0

            val statBoxRect = RectF(35f, y, (pageWidth - 35).toFloat(), y + 60f)
            val statBg = Paint().apply {
                color = if (percentage >= 75) Color.rgb(240, 253, 244) else Color.rgb(254, 242, 242)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(statBoxRect, 8f, 8f, statBg)
            val statBorder = Paint().apply {
                color = if (percentage >= 75) Color.rgb(34, 197, 94) else Color.rgb(239, 68, 68)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            canvas.drawRoundRect(statBoxRect, 8f, 8f, statBorder)

            canvas.drawText("کل تدریسی ایام: $totDays", 55f, y + 25f, textPaint)
            canvas.drawText("کل حاضری: $presents", 180f, y + 25f, textPaint)
            canvas.drawText("کل غیر حاضری: $absents", 290f, y + 25f, textPaint)

            val ratioPaint = Paint().apply {
                color = if (percentage >= 75) Color.rgb(22, 101, 52) else Color.rgb(185, 28, 28)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("حاضری کا تناسب: $percentage%", 400f, y + 25f, ratioPaint)

            val eligibilityText = if (percentage >= 75) {
                "اہلیت برائے امتحانات: اہل (حاضری کا تناسب مطلوبہ 75 فیصد سے زیادہ ہے)"
            } else {
                "اہلیت برائے امتحانات: تنبیہ! حاضری کا تناسب 75 فیصد سے کم ہے (شارٹ اٹینڈنس کیس)"
            }
            canvas.drawText(eligibilityText, 55f, y + 48f, ratioPaint)

            y += 75f

            // Monthly Breakdown Table Header
            val tableLeft = 35f
            val tableRight = (pageWidth - 35).toFloat()
            val rowHeight = 22f
            val headerBg = Paint().apply {
                color = Color.rgb(230, 240, 255)
                style = Paint.Style.FILL
            }
            val headerText = Paint().apply {
                color = Color.rgb(16, 54, 126)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            canvas.drawText("ماہ بہ ماہ حاضری رپورٹ برائے تدریسی سال", tableLeft, y - 6f, boldPaint)
            canvas.drawRect(RectF(tableLeft, y, tableRight, y + rowHeight), headerBg)
            canvas.drawRect(RectF(tableLeft, y, tableRight, y + rowHeight), borderPaint)

            canvas.drawText("مہینہ / سیشن", tableLeft + 15f, y + 15f, headerText)
            canvas.drawText("کل لیکچرز", tableLeft + 160f, y + 15f, headerText)
            canvas.drawText("حاضری", tableLeft + 260f, y + 15f, headerText)
            canvas.drawText("غیر حاضری", tableLeft + 360f, y + 15f, headerText)
            canvas.drawText("حاضری کا تناسب %", tableLeft + 440f, y + 15f, headerText)

            y += rowHeight

            if (monthlyStats.isEmpty()) {
                val noDataRect = RectF(tableLeft, y, tableRight, y + rowHeight)
                canvas.drawRect(noDataRect, borderPaint)
                canvas.drawText("اس تدریسی سال میں کوئی حاضری ریکارڈ درج نہیں ہے۔", tableLeft + 130f, y + 15f, textPaint)
                y += rowHeight
            } else {
                monthlyStats.forEachIndexed { idx, mStat ->
                    val rowRect = RectF(tableLeft, y, tableRight, y + rowHeight)
                    if (idx % 2 == 1) {
                        val altBg = Paint().apply {
                            color = Color.rgb(248, 250, 252)
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(rowRect, altBg)
                    }
                    canvas.drawRect(rowRect, borderPaint)

                    canvas.drawText(mStat.monthPrefix, tableLeft + 15f, y + 15f, boldPaint)
                    canvas.drawText("${mStat.totalDays}", tableLeft + 175f, y + 15f, textPaint)
                    canvas.drawText("${mStat.presentCount}", tableLeft + 275f, y + 15f, textPaint)
                    canvas.drawText("${mStat.absentCount}", tableLeft + 375f, y + 15f, textPaint)
                    canvas.drawText("${mStat.attendancePercentage}%", tableLeft + 460f, y + 15f, boldPaint)

                    y += rowHeight
                }
            }

            // Official Signature & Stamp Area
            val footerY = (pageHeight - 65).toFloat()
            val metaPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9.5f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }
            canvas.drawLine(tableLeft, footerY, tableLeft + 150f, footerY, borderPaint)
            canvas.drawText("دستخط استاد مضمون / انچارج", tableLeft + 15f, footerY + 15f, metaPaint)

            canvas.drawLine(tableRight - 150f, footerY, tableRight, footerY, borderPaint)
            canvas.drawText("دستخط پرنسپل و کالج مہر", tableRight - 140f, footerY + 15f, metaPaint)

            val devCreditPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8f
                typeface = Typeface.DEFAULT
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("مرتب و تیار کردہ حاضری نظام: حسن الرحمٰن تقوی", (pageWidth / 2).toFloat(), (pageHeight - 25).toFloat(), devCreditPaint)

            pdfDoc.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val file = File(reportsDir, "Student_Annual_${student.rollNo}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun openOrSharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "رپورٹ کھولیں یا شیئر کریں").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "PDF کھولنے کے لیے کوئی ایپ دستیاب نہیں ہے: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
