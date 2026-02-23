package com.example.newspulse.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FakeStatusBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "9:41",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "100%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
