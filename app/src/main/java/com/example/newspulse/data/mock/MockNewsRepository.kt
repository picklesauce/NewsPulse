package com.example.newspulse.data.mock

import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.model.Article

class MockNewsRepository : NewsRepository {
    override fun getArticles(): List<Article> = listOf(
        Article(
            title = "Breaking: Major Tech Company Announces New Product Launch",
            source = "TechNews",
            hoursAgo = "2h ago",
            snippet = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            topics = listOf("Technology", "Business", "Apple")
        ),
        Article(
            title = "Political Update: New Policy Changes Announced Today",
            source = "NewsWire",
            hoursAgo = "4h ago",
            snippet = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            topics = listOf("Politics", "USA")
        ),
        Article(
            title = "Business Report: Market Trends Show Growth",
            source = "Financial Times",
            hoursAgo = "1d ago",
            snippet = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
            topics = listOf("Business", "Technology")
        ),
        Article(
            title = "Technology Innovation: AI Breakthrough",
            source = "Tech Daily",
            hoursAgo = "3d ago",
            snippet = "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            topics = listOf("Technology", "Science")
        ),
        Article(
            title = "Tesla Announces New Electric Vehicle Line",
            source = "Auto News",
            hoursAgo = "5h ago",
            snippet = "Elon Musk's company reveals ambitious plans for next-generation electric vehicles targeting mass market.",
            topics = listOf("Tesla", "Elon Musk", "Technology", "Business")
        ),
        Article(
            title = "Elon Musk Addresses SpaceX Mars Mission",
            source = "Space Today",
            hoursAgo = "6h ago",
            snippet = "SpaceX founder outlines timeline for crewed Mars missions and discusses challenges ahead.",
            topics = listOf("Elon Musk", "Technology", "Science")
        ),
        Article(
            title = "Climate Summit Reaches Historic Agreement",
            source = "World News",
            hoursAgo = "2d ago",
            snippet = "Global leaders commit to ambitious carbon reduction targets in landmark environmental accord.",
            topics = listOf("Climate", "Politics")
        ),
        Article(
            title = "New Study Reveals Surprising Health Benefits",
            source = "Health Monitor",
            hoursAgo = "1d ago",
            snippet = "Researchers uncover unexpected connections between lifestyle choices and long-term wellness.",
            topics = listOf("Health", "Science")
        ),
        Article(
            title = "Taylor Swift Tour Breaks Entertainment Records",
            source = "Entertainment Weekly",
            hoursAgo = "12h ago",
            snippet = "Record-breaking tour continues to dominate charts and set new benchmarks for live performances.",
            topics = listOf("Taylor Swift", "Entertainment")
        ),
        Article(
            title = "Google and Microsoft Expand AI Partnerships",
            source = "TechCrunch",
            hoursAgo = "8h ago",
            snippet = "Major tech giants announce new collaborations to accelerate artificial intelligence development.",
            topics = listOf("Google", "Microsoft", "Technology")
        ),
        Article(
            title = "USA Economic Policy: What's Next",
            source = "Reuters",
            hoursAgo = "10h ago",
            snippet = "Analysts weigh in on potential policy shifts and their impact on domestic markets.",
            topics = listOf("USA", "Politics", "Business")
        )
    )
}
