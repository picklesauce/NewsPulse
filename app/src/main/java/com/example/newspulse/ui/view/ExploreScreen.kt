package com.example.newspulse.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.ui.theme.NewsPulseTheme

@Composable
fun ExploreScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Text(
                text = "Explore",
                modifier = Modifier.padding(24.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Discover trending topics and categories",
                color = Color(0xFF79747E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("filters") },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "Browse by topic",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    color = Color(0xFF1C1B1F)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    NewsPulseTheme {
        ExploreScreen(navController = rememberNavController())
    }
}
