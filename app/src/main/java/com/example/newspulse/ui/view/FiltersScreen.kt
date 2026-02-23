package com.example.newspulse.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.newspulse.ui.theme.NewsPulseTheme

@Composable
fun FiltersScreen(
    navController: NavController
) {
    val topics = listOf(
        "Technology",
        "Business",
        "Health",
        "Sports",
        "Entertainment",
        "Science"
    )

    var selectedTopics by remember { mutableStateOf(emptySet<String>()) }

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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "9:41",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Surface(
                        color = Color(0xFF666666),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Preview",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "100%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Text(
                    text = "Filters",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Topics",
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                topics.forEach { topic ->
                    TopicRow(
                        topic = topic,
                        isSelected = selectedTopics.contains(topic),
                        onToggle = {
                            val current = selectedTopics.toMutableSet()
                            if (current.contains(topic)) {
                                current.remove(topic)
                            } else {
                                current.add(topic)
                            }
                            selectedTopics = current
                        }
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Apply Filters",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        selectedTopics = emptySet()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Reset",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TopicRow(
    topic: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Text(
            text = topic,
            color = Color(0xFF333333),
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltersScreenPreview() {
    NewsPulseTheme {
        FiltersScreen(navController = rememberNavController())
    }
}
