package com.example.newspulse.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.DiscoverViewModel

private data class DiscoverCategory(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val interestType: InterestType = InterestType.Topic
)

// Names must match `DiscoverCategories.NAMES` (home feed splits followed topics by this list).
private val categories = listOf(
    DiscoverCategory("Technology", "Latest tech news & innovations", Icons.Outlined.Devices),
    DiscoverCategory("Finance", "Markets, stocks & economy", Icons.Outlined.AccountBalance),
    DiscoverCategory("Politics", "Political news & updates", Icons.Outlined.Gavel),
    DiscoverCategory("Artificial Intelligence", "AI breakthroughs & research", Icons.Outlined.Psychology),
    DiscoverCategory("Cryptocurrency", "Crypto markets & blockchain", Icons.Outlined.CurrencyBitcoin),
    DiscoverCategory("Space", "Space exploration & astronomy", Icons.Outlined.RocketLaunch),
    DiscoverCategory("Health & Wellness", "Health news & medical advances", Icons.Outlined.FavoriteBorder),
    DiscoverCategory("Business", "Corporate & startup news", Icons.Outlined.Business),
    DiscoverCategory("Sports", "Scores, trades & highlights", Icons.Outlined.SportsSoccer),
    DiscoverCategory("Entertainment", "Movies, music & culture", Icons.Outlined.Movie),
    DiscoverCategory("Science", "Discoveries & breakthroughs", Icons.Outlined.Biotech),
    DiscoverCategory("Environment", "Climate & sustainability", Icons.Outlined.Park),
    DiscoverCategory("Education", "Learning & academia", Icons.Outlined.School),
    DiscoverCategory("World News", "Global events & affairs", Icons.Outlined.Public, InterestType.Country),
)

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
            isLoading = state.isLoading,
            isFollowed = state.isFollowed,
            onBack = { viewModel.onClearSelection() },
            onArticleClick = { id -> navController.navigate("articleDetail/$id") },
            onFollow = { viewModel.onFollowTopic() },
            onUnfollow = { viewModel.onUnfollowTopic() }
        )
    } else {
        DiscoverBrowseView(
            onCategoryClick = { category ->
                val interest = Interest(
                    id = "interest-${category.name.lowercase().replace(" ", "-")}",
                    type = category.interestType,
                    name = category.name
                )
                viewModel.onSelectInterest(interest)
            }
        )
    }
}

@Composable
private fun DiscoverBrowseView(
    onCategoryClick: (DiscoverCategory) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) {
        categories
    } else {
        categories.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
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
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Discover Topics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search topics...", color = Color(0xFF9E9E9E))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF79747E)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF1C1B1F),
                        unfocusedContainerColor = Color(0xFFF9F9F9),
                        focusedContainerColor = Color.White,
                        cursorColor = Color(0xFF1C1B1F)
                    )
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: DiscoverCategory,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7E0EC))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF1C1B1F)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.description,
                fontSize = 12.sp,
                color = Color(0xFF79747E),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DiscoverArticleView(
    interest: Interest,
    articles: List<Article>,
    isLoading: Boolean,
    isFollowed: Boolean,
    onBack: () -> Unit,
    onArticleClick: (String) -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
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
            Column {
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = interest.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1F)
                        )
                        if (!isLoading) {
                            Text(
                                text = "${articles.size} article${if (articles.size == 1) "" else "s"}",
                                fontSize = 13.sp,
                                color = Color(0xFF79747E)
                            )
                        }
                    }
                    if (isFollowed) {
                        OutlinedButton(
                            onClick = onUnfollow,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Following", fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = onFollow,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1C1B1F)
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Follow", fontSize = 13.sp)
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFE7E0EC), thickness = 1.dp)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1C1B1F))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Finding articles...",
                        fontSize = 15.sp,
                        color = Color(0xFF79747E)
                    )
                }
            }
        } else if (articles.isEmpty()) {
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
                        imagePlaceholderText = "[IMAGE]",
                        onClick = { onArticleClick(article.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
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
