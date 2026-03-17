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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.newspulse.ui.viewmodel.FeedViewModel

@Composable
fun ArticleListScreen(
    navController: NavController,
    viewModel: FeedViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var filterExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onRefresh()
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
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.headerTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded }
                    ) {
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
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearch(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = {
                            Text(
                                text = state.searchPlaceholder,
                                color = Color(0xFF79747E)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF79747E)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE7E0EC),
                            unfocusedLeadingIconColor = Color(0xFF79747E),
                            cursorColor = Color(0xFF6750A4),
                            focusedBorderColor = Color(0xFF6750A4),
                            focusedLeadingIconColor = Color(0xFF6750A4)
                        )
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFFB3261E),
                fontSize = 14.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val filterLabel = if (state.activeTopicFilters.isEmpty()) {
                "All topics"
            } else {
                state.activeTopicFilters.sorted().joinToString(", ")
            }

            OutlinedTextField(
                value = filterLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Filter by topic"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE7E0EC),
                    unfocusedTrailingIconColor = Color(0xFF79747E),
                    cursorColor = Color(0xFF6750A4),
                    focusedBorderColor = Color(0xFF6750A4),
                    focusedTrailingIconColor = Color(0xFF6750A4),
                    disabledBorderColor = Color(0xFFE7E0EC),
                    disabledTextColor = Color(0xFF1C1B1F),
                    disabledTrailingIconColor = Color(0xFF79747E)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
            // Transparent overlay to capture click — required because OutlinedTextField
            // consumes touch events internally and ignores .clickable on itself
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { filterExpanded = true }
            )
            DropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = state.activeTopicFilters.isEmpty(),
                                onCheckedChange = null
                            )
                            Text("All topics", fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = { viewModel.onClearTopicFilters() }
                )
                HorizontalDivider()
                state.selectedInterests.sorted().forEach { topic ->
                    val isChecked = topic in state.activeTopicFilters
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null
                                )
                                Text(topic)
                            }
                        },
                        onClick = { viewModel.onToggleTopicFilter(topic) }
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
            state.selectedInterests.forEach { topic ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.clickable { viewModel.unfollowInterest(topic) }
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
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6750A4))
                }
            } else if (state.articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.emptyStateMessage ?: "",
                        color = Color(0xFF79747E),
                        fontSize = 16.sp
                    )
                }
            } else {
                state.articles.forEach { article ->
                    ArticleCard(
                        article = article,
                        imagePlaceholderText = state.imagePlaceholderText,
                        onClick = { navController.navigate("articleDetail/${article.id}") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun ArticleCard(
    article: Article,
    imagePlaceholderText: String,
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
                    text = imagePlaceholderText,
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = article.readTime,
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                        Text(
                            text = article.hoursAgo,
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                    }
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
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            ArticleListScreen(navController = rememberNavController())
        }
    }
}
