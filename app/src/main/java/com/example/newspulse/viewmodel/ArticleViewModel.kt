package com.example.newspulse.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.model.Article

class ArticleViewModel : ViewModel() {
    val articles = listOf(
        Article(
            title = "Breaking: Major Technology Breakthrough Announced by Researchers",
            source = "Tech Daily",
            hoursAgo = "2h ago"
        ),
        Article(
            title = "Global Markets React to Economic Policy Changes",
            source = "Financial Times",
            hoursAgo = "3h ago"
        ),
        Article(
            title = "Climate Summit Reaches Historic Agreement",
            source = "World News",
            hoursAgo = "5h ago"
        ),
        Article(
            title = "New Study Reveals Surprising Health Benefits",
            source = "Health Monitor",
            hoursAgo = "6h ago"
        ),
        Article(
            title = "Sports Championship Delivers Unexpected Results",
            source = "Sports Weekly",
            hoursAgo = "7h ago"
        )
    )
}

