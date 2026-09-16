package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CommunicationHelper {

    fun formatParentMessage(
        studentName: String,
        rollNo: Int,
        className: String,
        date: String,
        presentDays: Int,
        absentDays: Int,
        teacherDisplayName: String,
        department: String
    ): String {
        return buildString {
            append("محترم والد صاحب!\n\n")
            append("آپ کے صاحبزادے ")
            append(studentName)
            append("، رول نمبر ")
            append(rollNo)
            append("، جماعت ")
            append(className)
            append(" آج مورخہ ")
            append(date)
            append(" کو غیر حاضر رہے۔\n\n")
            append("اس ماہ اب تک ان کی حاضری ")
            append(presentDays)
            append(" اور غیر حاضری ")
            append(absentDays)
            append(" ہے۔\n\n")
            append("آپ کی توجہ اور تعاون بچے کی تعلیمی کامیابی میں اہم کردار ادا کرتا ہے۔ براہِ کرم حاضری کی پابندی پر توجہ فرمائیں۔\n\n")
            append(teacherDisplayName)
            append("\n")
            append("شعبہ: ")
            append(department)
            append("\n")
            append("گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان")
        }
    }

    fun sanitizePakistaniNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("03") && digits.length == 11 -> digits
            digits.startsWith("923") && digits.length == 12 -> "0" + digits.substring(2)
            digits.startsWith("3") && digits.length == 10 -> "0$digits"
            else -> raw.trim()
        }
    }

    fun toInternationalNumber(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("03") && digits.length == 11 -> "92" + digits.substring(1)
            digits.startsWith("923") && digits.length == 12 -> digits
            digits.startsWith("3") && digits.length == 10 -> "92$digits"
            else -> digits
        }
    }

    fun callParent(context: Context, phoneNumber: String) {
        val clean = phoneNumber.trim()
        if (clean.isEmpty()) {
            Toast.makeText(
                context,
                "اس طالب علم کے والد کا موبائل نمبر محفوظ نہیں ہے۔ براہِ کرم نمبر درست کریں۔",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$clean")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "کال ڈائلر کھولنے میں خرابی: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        val clean = phoneNumber.trim()
        if (clean.isEmpty()) {
            Toast.makeText(
                context,
                "اس طالب علم کے والد کا موبائل نمبر محفوظ نہیں ہے۔ براہِ کرم نمبر درست کریں۔",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$clean")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            // Fallback to generic SEND intent
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    putExtra("address", clean)
                    putExtra("sms_body", message)
                    type = "vnd.android-dir/mms-sms"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(
                    context,
                    "میسجنگ ایپ دستیاب نہیں ہے۔",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        val clean = phoneNumber.trim()
        if (clean.isEmpty()) {
            Toast.makeText(
                context,
                "اس طالب علم کے والد کا موبائل نمبر محفوظ نہیں ہے۔ براہِ کرم نمبر درست کریں۔",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val international = toInternationalNumber(clean)
        try {
            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$international&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "واٹس ایپ کھولنے میں خرابی پیش آئی ہے۔ براہِ کرم یقینی بنائیں کہ واٹس ایپ انسٹال ہے۔",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
