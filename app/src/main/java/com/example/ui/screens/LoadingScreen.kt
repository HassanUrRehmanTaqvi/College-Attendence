package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splash_alpha"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .alpha(alphaAnim),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // College Crest Emblem
                Surface(
                    modifier = Modifier
                        .size(115.dp)
                        .shadow(10.dp, CircleShape),
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
                            modifier = Modifier.size(98.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Official College Name
                Text(
                    text = "گورنمنٹ ایسوسی ایٹ کالج مخدوم رشید ملتان",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CollegeNavyPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Creator & Developer Credit (Clean, dignified, smaller than college title)
                Text(
                    text = "مرتب و تیار کردہ: حسن الرحمٰن تقوی",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = CollegeNavyDark.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Circular Progress
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = CollegeGoldTertiary,
                    strokeWidth = 2.5.dp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "الَّذِی عَلَّمَ بِالْقَلَمِ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CollegeGoldTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
