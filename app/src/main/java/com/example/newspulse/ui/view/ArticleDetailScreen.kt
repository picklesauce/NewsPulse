package com.example.newspulse.ui.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
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
    val article by articleDetailViewModel.article.collectAsState()
    val relatedArticles by articleDetailViewModel.relatedArticles.collectAsState()
    val isLoadingRelated by articleDetailViewModel.isLoadingRelated.collectAsState()

    LaunchedEffect(articleId) {
        articleId?.let { articleDetailViewModel.loadArticle(it) }
    }

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ArticleTopBar(
            title = article?.title ?: "Article Not Found",
            source = article?.source ?: "—",
            timeAgo = article?.hoursAgo ?: "—",
            onBackClick = { navController.popBackStack() },
            onShareClick = article?.let { a -> { shareArticle(context, a) } }
        )

        ArticleContent(
            body = article?.summary ?: "",
            imageUrl = article?.imageUrl ?: "",
            readTime = article?.readTime ?: "",
            relatedArticles = relatedArticles,
            isLoadingRelated = isLoadingRelated,
            onRelatedArticleClick = { id -> navController.navigate("articleDetail/$id") }
        )

        article?.let { a ->
            val saved by articleDetailViewModel.isSaved.collectAsState()
            SaveOfflineButton(
                article = a,
                isSaved = saved,
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
    onBackClick: () -> Unit,
    onShareClick: (() -> Unit)? = null
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
                if (onShareClick != null) {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share article",
                            tint = Color.White
                        )
                    }
                }
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
    body: String,
    imageUrl: String = "",
    readTime: String = "",
    relatedArticles: List<Article> = emptyList(),
    isLoadingRelated: Boolean = false,
    onRelatedArticleClick: (String) -> Unit = {}
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
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (readTime.isNotEmpty()) {
                Text(
                    text = readTime,
                    color = Color(0xFF79747E),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Text(
                text = body,
                color = Color.Black,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Related articles section
            if (isLoadingRelated || relatedArticles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE7E0EC), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "You might also like",
                    color = Color(0xFF1C1B1F),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isLoadingRelated) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFF1C1B1F),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    relatedArticles.forEach { related ->
                        RelatedArticleRow(
                            article = related,
                            onClick = { onRelatedArticleClick(related.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RelatedArticleRow(
    article: Article,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F8F8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (article.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE7E0EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF79747E)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 3,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${article.source} · ${article.hoursAgo}",
                    fontSize = 12.sp,
                    color = Color(0xFF79747E)
                )
            }
        }
    }
}

@Composable
fun SaveOfflineButton(
    article: Article,
    isSaved: Boolean,
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
                onClick = { if (!isSaved) onSave(article) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaved,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1B1F),
                    disabledContainerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = if (isSaved) "Saved" else "Save Article",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun shareArticle(context: Context, article: Article) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.url.ifBlank { "https://newspulse.example/article/${article.id}" }}")
    }
    context.startActivity(Intent.createChooser(intent, "Share article"))
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
