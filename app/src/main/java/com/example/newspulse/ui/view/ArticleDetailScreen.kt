package com.example.newspulse.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.domain.model.Article
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.ArticleDetailViewModel

@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: String? = null,
    articleDetailViewModel: ArticleDetailViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val article = articleId?.let { articleDetailViewModel.getArticleById(it) }
    val displayTitle = article?.title ?: "Article Not Found"
    val source = article?.source ?: "Tech Daily"
    val timeAgo = article?.hoursAgo ?: "2 hours ago"
    val articleBody = generatePlaceholderBody()

    LaunchedEffect(articleId) {
        article?.let { articleDetailViewModel.addToReadingHistory(it.id, it.title) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ArticleTopBar(
            title = displayTitle,
            source = source,
            timeAgo = timeAgo,
            onBackClick = { navController.popBackStack() }
        )

        ArticleContent(
            body = articleBody
        )

        article?.let { a ->
            SaveOfflineButton(
                article = a,
                onSave = { articleDetailViewModel.saveArticle(it) }
            )
        }
    }
}

@Composable
fun ArticleTopBar(
    title: String,
    source: String,
    timeAgo: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.padding(start = 48.dp, top = 8.dp)
            ) {
                Text(
                    text = source,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = " • ",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = timeAgo,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ColumnScope.ArticleContent(
    body: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = body,
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun SaveOfflineButton(
    article: Article,
    onSave: (Article) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onSave(article) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(
                    text = "Save for Offline",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun generatePlaceholderBody(): String {
    return """
        In a groundbreaking development that could reshape the future of technology, researchers at the Institute of Advanced Studies have announced a major breakthrough in quantum computing capabilities. The discovery promises to accelerate computational power by orders of magnitude.

        Dr. Sarah Chen, lead researcher on the project, explained that the team has successfully developed a new approach to quantum error correction that significantly improves the stability and reliability of quantum systems. "This is a game-changer for the field," she stated during a press conference held earlier today.

        The implications of this breakthrough extend far beyond the laboratory. Industry experts predict that this advancement could revolutionize fields ranging from cryptography to drug discovery, potentially solving complex problems that have stumped classical computers for decades.

        The research team, which has been working on this project for over five years, utilized cutting-edge techniques in quantum mechanics to achieve these results. Their findings have been peer-reviewed and published in several leading scientific journals, receiving widespread acclaim from the academic community.

        As the technology continues to mature, we can expect to see practical applications emerge in the coming years, marking a significant milestone in the evolution of computing technology.
    """.trimIndent()
}

@Preview(showBackground = true)
@Composable
private fun ArticleDetailScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            ArticleDetailScreen(
                navController = rememberNavController(),
                articleId = "art-1"
            )
        }
    }
}
