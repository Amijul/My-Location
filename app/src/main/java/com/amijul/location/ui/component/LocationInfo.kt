package com.amijul.location.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationInfo(label: String, value: String) {
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(label, color = Color.Gray, fontSize = 12.dp.value.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}
