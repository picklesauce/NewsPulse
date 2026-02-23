package com.example.newspulse.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.domain.IOnboardingPreferences
import com.example.newspulse.domain.model.Article
import com.example.newspulse.ui.preview.FakeOnboardingPreferences
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.ArticleViewModel

@Composable
fun ArticleListScreen(
    navController: NavController,
    onboardingPreferences: IOnboardingPreferences,
    viewModel: ArticleViewModel = viewModel()
) {
    val selectedInterests = onboardingPreferences.getSelectedTopics()
    val filteredArticles = if (selectedInterests.isEmpty()) {
        viewModel.allArticles
    } else {
        viewModel.allArticles.filter { article ->
            article.topics.any { it in selectedInterests }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NewsPulse",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: search */ }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF1C1B1F)
                    )
                }
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF1C1B1F)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedInterests.forEach { topic ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { /* chip tapped - could filter further */ }
                ) {
                    Text(
                        text = topic,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles match your interests",
                        color = Color(0xFF79747E),
                        fontSize = 16.sp
                    )
                }
            } else {
                filteredArticles.forEach { article ->
                    ArticleCard(
                        article = article,
                        onClick = { navController.navigate("articleDetail/${article.title}") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFE7E0EC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[IMAGE]",
                    color = Color(0xFF79747E),
                    fontSize = 14.sp
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.source,
                        fontSize = 12.sp,
                        color = Color(0xFF79747E)
                    )
                    Text(
                        text = article.hoursAgo,
                        fontSize = 12.sp,
                        color = Color(0xFF79747E)
                    )
                }
                if (article.snippet.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.snippet,
                        fontSize = 14.sp,
                        color = Color(0xFF49454F),
                        lineHeight = 20.sp,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListScreenPreview() {
    NewsPulseTheme {
        ArticleListScreen(
            navController = rememberNavController(),
            onboardingPreferences = FakeOnboardingPreferences
        )
    }
}
