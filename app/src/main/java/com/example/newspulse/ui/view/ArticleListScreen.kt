package com.example.newspulse.ui.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
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
    var interestFilterExpanded by remember { mutableStateOf(false) }
    var categoryFilterExpanded by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResumeRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh feed",
                            tint = Color(0xFF1C1B1F)
                        )
                    }
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeTopicFilterDropdown(
                sectionTitle = state.interestFilterSectionTitle,
                topicNames = state.followedInterestNames.sorted(),
                activeFilters = state.activeInterestFilters,
                expanded = interestFilterExpanded,
                onExpandedChange = { interestFilterExpanded = it },
                allSelectedLabel = "All interests",
                emptyLabel = "No interests followed",
                onClearFilters = { viewModel.onClearInterestFilters() },
                onToggleTopic = { viewModel.onToggleInterestFilter(it) }
            )
            HomeTopicFilterDropdown(
                sectionTitle = state.categoryFilterSectionTitle,
                topicNames = state.followedCategoryNames.sorted(),
                activeFilters = state.activeCategoryFilters,
                expanded = categoryFilterExpanded,
                onExpandedChange = { categoryFilterExpanded = it },
                allSelectedLabel = "All categories",
                emptyLabel = "No categories followed",
                onClearFilters = { viewModel.onClearCategoryFilters() },
                onToggleTopic = { viewModel.onToggleCategoryFilter(it) }
            )
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.emptyStateMessage ?: "",
                            color = Color(0xFF79747E),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Explore topics",
                            color = Color(0xFF6750A4),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                navController.navigate("explore")
                            }
                        )
                    }
                }
            } else {
                if (state.isFallbackFeed) {
                    FeedBanner(
                        text = "Showing trending articles while we find content for your interests",
                        actionLabel = "Add more interests",
                        onAction = { navController.navigate("explore") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (state.isCoverageThin) {
                    FeedBanner(
                        text = "Coverage is limited for some interests",
                        actionLabel = "Discover more topics",
                        onAction = { navController.navigate("explore") }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
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
private fun HomeTopicFilterDropdown(
    sectionTitle: String,
    topicNames: List<String>,
    activeFilters: Set<String>?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    allSelectedLabel: String,
    emptyLabel: String,
    onClearFilters: () -> Unit,
    onToggleTopic: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF49454F),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            val summaryLabel = when {
                topicNames.isEmpty() -> emptyLabel
                activeFilters == null -> allSelectedLabel
                activeFilters.isEmpty() -> "None selected"
                else -> activeFilters.sorted().joinToString(", ")
            }
            OutlinedTextField(
                value = summaryLabel,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
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
            if (topicNames.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable {
                            if (!expanded) onExpandedChange(true)
                        }
                )
            }
            DropdownMenu(
                expanded = expanded && topicNames.isNotEmpty(),
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = activeFilters == null,
                                onCheckedChange = null
                            )
                            Text(allSelectedLabel, fontWeight = FontWeight.Medium)
                        }
                    },
                    onClick = { onClearFilters() }
                )
                HorizontalDivider()
                topicNames.forEach { topic ->
                    val isChecked = when (activeFilters) {
                        null -> true
                        else -> topic in activeFilters
                    }
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
                        onClick = { onToggleTopic(topic) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedBanner(
    text: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3EDF7)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = Color(0xFF49454F),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6750A4),
                modifier = Modifier.clickable(onClick = onAction)
            )
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
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                if (article.imageUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = article.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE7E0EC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = imagePlaceholderText,
                                    color = Color(0xFF79747E),
                                    fontSize = 14.sp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE7E0EC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = imagePlaceholderText,
                                    color = Color(0xFF79747E),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE7E0EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = imagePlaceholderText,
                            color = Color(0xFF79747E),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.padding(bottom = if (article.topics.isEmpty()) 8.dp else 4.dp)
                )
                if (article.topics.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        article.topics.distinct().forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8DEF8)
                            ) {
                                Text(
                                    text = name,
                                    color = Color(0xFF1C1B1F),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
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
