package com.example.newspulse.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.DiscoverViewModel

@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: DiscoverViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsState()

    if (state.selectedInterest != null) {
        DiscoverArticleView(
            interest = state.selectedInterest!!,
            articles = state.articlesForSelected,
            imagePlaceholderText = "[IMAGE]",
            onBack = { viewModel.onClearSelection() },
            onArticleClick = { id -> navController.navigate("articleDetail/$id") }
        )
    } else {
        DiscoverBrowseView(
            interestsByType = viewModel.visibleInterestsByType(),
            typeFilter = state.typeFilter,
            onTypeFilterChange = { viewModel.onSetTypeFilter(it) },
            onInterestClick = { viewModel.onSelectInterest(it) }
        )
    }
}

@Composable
private fun DiscoverBrowseView(
    interestsByType: Map<InterestType, List<Interest>>,
    typeFilter: InterestType?,
    onTypeFilterChange: (InterestType?) -> Unit,
    onInterestClick: (Interest) -> Unit
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
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Discover",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Browse articles by any topic — no changes to your feed",
                    fontSize = 13.sp,
                    color = Color(0xFF79747E)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TypeFilterRow(
                    currentFilter = typeFilter,
                    onFilterChange = onTypeFilterChange
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            interestsByType.forEach { (type, interests) ->
                SectionLabel(type.name)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.clickable { onInterestClick(interest) }
                        ) {
                            Text(
                                text = interest.name,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF1C1B1F)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DiscoverArticleView(
    interest: Interest,
    articles: List<Article>,
    imagePlaceholderText: String,
    onBack: () -> Unit,
    onArticleClick: (String) -> Unit
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Discover",
                        tint = Color(0xFF1C1B1F)
                    )
                }
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = interest.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = "${articles.size} article${if (articles.size == 1) "" else "s"}",
                        fontSize = 13.sp,
                        color = Color(0xFF79747E)
                    )
                }
            }
        }

        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No articles available for ${interest.name}",
                    fontSize = 15.sp,
                    color = Color(0xFF79747E)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                articles.forEach { article ->
                    ArticleCard(
                        article = article,
                        imagePlaceholderText = imagePlaceholderText,
                        onClick = { onArticleClick(article.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TypeFilterRow(
    currentFilter: InterestType?,
    onFilterChange: (InterestType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == null,
            onClick = { onFilterChange(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF1C1B1F),
                selectedLabelColor = Color.White,
                containerColor = Color(0xFFE7E0EC),
                labelColor = Color(0xFF1C1B1F)
            )
        )
        InterestType.entries.forEach { type ->
            FilterChip(
                selected = currentFilter == type,
                onClick = { onFilterChange(if (currentFilter == type) null else type) },
                label = { Text(type.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1C1B1F),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFE7E0EC),
                    labelColor = Color(0xFF1C1B1F)
                )
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF79747E),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            ExploreScreen(navController = rememberNavController())
        }
    }
}
