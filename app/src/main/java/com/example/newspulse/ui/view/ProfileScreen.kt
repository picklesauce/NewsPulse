package com.example.newspulse.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    LaunchedEffect(viewModel.sessionIdentity()) {
        viewModel.refreshFromModel()
    }
    val username = viewModel.username
    val memberSince = viewModel.memberSince
    val interests = viewModel.interests
    val readingHistory = viewModel.readingHistory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE7E0EC), thickness = 1.dp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFE7E0EC), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile picture",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF79747E)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = username,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Member since $memberSince",
                        fontSize = 14.sp,
                        color = Color(0xFF79747E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE7E0EC), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "YOUR INTERESTS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                interests.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF5F5F5),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = topic,
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { navController.navigate("interests") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1C1B1F)
                )
            ) {
                Text(text = "+ Add More Interests")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE7E0EC), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "READING HISTORY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    letterSpacing = 0.5.sp
                )
                if (readingHistory.size > 5) {
                    Text(
                        text = "See all",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2979FF),
                        modifier = Modifier.clickable {
                            navController.navigate("readingHistory")
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (readingHistory.isEmpty()) {
                Text(
                    text = "No articles read yet",
                    fontSize = 14.sp,
                    color = Color(0xFF79747E),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                readingHistory.take(5).forEach { item ->
                    ReadingHistoryCard(
                        title = item.title,
                        timeAgo = formatHistoryTimeAgo(item.readAtMillis),
                        onClick = { navController.navigate("articleDetail/${item.articleId}") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    viewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1C1B1F)
                )
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReadingHistoryCard(
    title: String,
    timeAgo: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7E0EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF79747E)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeAgo,
                    fontSize = 12.sp,
                    color = Color(0xFF79747E)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            ProfileScreen(navController = rememberNavController())
        }
    }
}
