package com.example.newspulse.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.SavedArticlesViewModel

@Composable
fun SavedArticlesScreen(
    navController: NavController,
    viewModel: SavedArticlesViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val savedArticles by viewModel.savedArticles.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black
        ) {
            Column {
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
                Text(
                    text = "Saved Articles",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color.White
        ) {
            if (savedArticles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No saved articles yet",
                        color = Color(0xFF666666),
                        fontSize = 16.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    savedArticles.forEachIndexed { index, article ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("articleDetail/${article.id}")
                                }
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = article.title,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = article.source,
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = " • ",
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = article.readTime,
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = " • ",
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = article.hoursAgo,
                                    color = Color(0xFF666666),
                                    fontSize = 12.sp
                                )
                            }
                            if (index < savedArticles.size - 1) {
                                Divider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedArticlesScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            SavedArticlesScreen(navController = rememberNavController())
        }
    }
}
