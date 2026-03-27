package com.example.newspulse.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.preview.createPreviewViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.viewmodel.InterestsViewModel

@Composable
fun InterestsScreen(
    navController: NavController,
    viewModel: InterestsViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val state by viewModel.uiState.collectAsState()
    var typePickerExpanded by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Profile",
                        tint = Color(0xFF1C1B1F)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp)
                ) {
                    Text(
                        text = state.headerTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.subtitle,
                        fontSize = 14.sp,
                        color = Color(0xFF79747E),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search or add interests", color = Color(0xFF79747E)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF79747E))
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE7E0EC),
                    cursorColor = Color(0xFF6750A4),
                    focusedBorderColor = Color(0xFF6750A4)
                )
            )

            if (state.canAddCustom) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { typePickerExpanded = true },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF6750A4))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add \"${state.searchQuery}\"", color = Color(0xFF6750A4))
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF3EDF7)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6750A4))
                    )
                    DropdownMenu(
                        expanded = typePickerExpanded,
                        onDismissRequest = { typePickerExpanded = false }
                    ) {
                        InterestType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    viewModel.addCustomInterest(type)
                                    typePickerExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TypeFilterChips(
                currentFilter = state.typeFilter,
                filterAllLabel = state.filterAllLabel,
                onFilterChange = viewModel::setTypeFilter
            )
            Spacer(modifier = Modifier.height(16.dp))

            state.typeFilter?.let { current ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Text(
                        text = state.showingFilterLabel.replace("%s", current.name),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF49454F)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            state.interestsToShow.forEach { (type, interests) ->
                SectionHeader(title = type.name)
                Spacer(modifier = Modifier.height(8.dp))
                InterestChipRow(
                    interests = interests,
                    followedIds = state.followedIds,
                    onFollowToggle = viewModel::onFollowToggle
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TypeFilterChips(
    currentFilter: InterestType?,
    filterAllLabel: String,
    onFilterChange: (InterestType?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentFilter == null,
            onClick = { onFilterChange(null) },
            label = { Text(filterAllLabel) },
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
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF79747E),
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun InterestChipRow(
    interests: List<Interest>,
    followedIds: Set<String>,
    onFollowToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        interests.chunked(2).forEach { rowInterests ->
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowInterests.forEach { interest ->
                    val isFollowed = interest.id in followedIds
                    FilterChip(
                        selected = isFollowed,
                        onClick = { onFollowToggle(interest.id) },
                        label = { Text(interest.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6750A4),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFE7E0EC),
                            labelColor = Color(0xFF1C1B1F)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InterestsScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            InterestsScreen(navController = rememberNavController())
        }
    }
}
