package com.example.newspulse.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.newspulse.ui.viewmodel.TopicSelectionViewModel

@Composable
fun TopicSelectionScreen(
    navController: NavController,
    viewModel: TopicSelectionViewModel = viewModel(factory = CompositionLocals.LocalViewModelFactory.current)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTopics by viewModel.selectedTopics.collectAsState()
    val filteredTopics = viewModel.getFilteredTopics()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NewsPulse",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select topics you're interested in to personalize your news feed",
                fontSize = 16.sp,
                color = Color(0xFF49454F),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search interests",
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

            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredTopics.forEach { topic ->
                    val isSelected = selectedTopics.contains(topic)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleTopic(topic) },
                        label = { Text(topic) },
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = {
                viewModel.saveAndContinue()
                navController.navigate("login") {
                    popUpTo("topicSelection") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(48.dp),
            enabled = selectedTopics.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1B1F),
                disabledContainerColor = Color(0xFFE7E0EC),
                contentColor = Color.White,
                disabledContentColor = Color(0xFF79747E)
            )
        ) {
            Text(
                text = "Continue (${selectedTopics.size} selected)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopicSelectionScreenPreview() {
    NewsPulseTheme {
        CompositionLocalProvider(
            CompositionLocals.LocalViewModelFactory provides createPreviewViewModelFactory()
        ) {
            TopicSelectionScreen(navController = rememberNavController())
        }
    }
}
