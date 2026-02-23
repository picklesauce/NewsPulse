package com.example.newspulse.data.mock

import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.util.toInterest

class MockNewsRepository : NewsRepository {
    override fun getArticles(): List<Article> {
        val now = System.currentTimeMillis()
        val ms = { h: Long -> h * 60 * 60 * 1000 }
        val ds = { d: Long -> d * 24 * 60 * 60 * 1000 }
        var idx = 0
        return listOf(
            Article(
                id = "art-1",
                title = "Breaking: Major Tech Company Announces New Product Launch",
                source = "TechNews",
                url = "https://example.com/1",
                publishedAt = now - ms(2),
                summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                interests = listOf("Technology", "Business", "Apple").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-2",
                title = "Political Update: New Policy Changes Announced Today",
                source = "NewsWire",
                url = "https://example.com/2",
                publishedAt = now - ms(4),
                summary = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                interests = listOf("Politics", "USA").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-3",
                title = "Business Report: Market Trends Show Growth",
                source = "Financial Times",
                url = "https://example.com/3",
                publishedAt = now - ds(1),
                summary = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                interests = listOf("Business", "Technology").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-4",
                title = "Technology Innovation: AI Breakthrough",
                source = "Tech Daily",
                url = "https://example.com/4",
                publishedAt = now - ds(3),
                summary = "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                interests = listOf("Technology", "Science").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-5",
                title = "Tesla Announces New Electric Vehicle Line",
                source = "Auto News",
                url = "https://example.com/5",
                publishedAt = now - ms(5),
                summary = "Elon Musk's company reveals ambitious plans for next-generation electric vehicles targeting mass market.",
                interests = listOf("Tesla", "Elon Musk", "Technology", "Business").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-6",
                title = "Elon Musk Addresses SpaceX Mars Mission",
                source = "Space Today",
                url = "https://example.com/6",
                publishedAt = now - ms(6),
                summary = "SpaceX founder outlines timeline for crewed Mars missions and discusses challenges ahead.",
                interests = listOf("Elon Musk", "Technology", "Science").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-7",
                title = "Climate Summit Reaches Historic Agreement",
                source = "World News",
                url = "https://example.com/7",
                publishedAt = now - ds(2),
                summary = "Global leaders commit to ambitious carbon reduction targets in landmark environmental accord.",
                interests = listOf("Climate", "Politics").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-8",
                title = "New Study Reveals Surprising Health Benefits",
                source = "Health Monitor",
                url = "https://example.com/8",
                publishedAt = now - ds(1),
                summary = "Researchers uncover unexpected connections between lifestyle choices and long-term wellness.",
                interests = listOf("Health", "Science").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-9",
                title = "Taylor Swift Tour Breaks Entertainment Records",
                source = "Entertainment Weekly",
                url = "https://example.com/9",
                publishedAt = now - ms(12),
                summary = "Record-breaking tour continues to dominate charts and set new benchmarks for live performances.",
                interests = listOf("Taylor Swift", "Entertainment").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-10",
                title = "Google and Microsoft Expand AI Partnerships",
                source = "TechCrunch",
                url = "https://example.com/10",
                publishedAt = now - ms(8),
                summary = "Major tech giants announce new collaborations to accelerate artificial intelligence development.",
                interests = listOf("Google", "Microsoft", "Technology").map { it.toInterest(idx++) }
            ),
            Article(
                id = "art-11",
                title = "USA Economic Policy: What's Next",
                source = "Reuters",
                url = "https://example.com/11",
                publishedAt = now - ms(10),
                summary = "Analysts weigh in on potential policy shifts and their impact on domestic markets.",
                interests = listOf("USA", "Politics", "Business").map { it.toInterest(idx++) }
            )
        )
    }
}
