package com.example.newspulse.data.mock

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.InterestSlug

object MockDB {
    val interests: List<Interest> = buildList {
        listOf("USA", "Canada", "United Kingdom", "Germany", "Japan").forEach {
            add(Interest(InterestSlug.stableIdForName(it), InterestType.Country, it))
        }
        listOf("Donald Trump", "Elon Musk", "Taylor Swift", "Joe Biden", "Jeff Bezos").forEach {
            add(Interest(InterestSlug.stableIdForName(it), InterestType.Person, it))
        }
        listOf("Tesla", "Apple", "Google", "Microsoft", "Amazon").forEach {
            add(Interest(InterestSlug.stableIdForName(it), InterestType.Company, it))
        }
        listOf(
            "Technology", "Business", "Sports", "Entertainment",
            "Politics", "Science", "Health", "Climate"
        ).forEach {
            add(Interest(InterestSlug.stableIdForName(it), InterestType.Topic, it))
        }
    }

    private fun byName(name: String): Interest =
        interests.find { it.name == name }
            ?: error("Interest not found: $name. Available: ${interests.map { it.name }}")

    /** Stable placeholder so [Article.hasDisplayImage] is true for mock data (matches feed filter). */
    private fun mockImage(articleId: String): String =
        "https://picsum.photos/seed/${articleId}/800/450"

    val articles: List<Article> = run {
        val now = System.currentTimeMillis()
        val ms = { h: Long -> h * 60 * 60 * 1000 }
        val ds = { d: Long -> d * 24 * 60 * 60 * 1000 }
        listOf(
            Article(
                id = "art-1",
                title = "Breaking: Major Tech Company Announces New Product Launch",
                source = "TechNews",
                url = "https://example.com/1",
                publishedAt = now - ms(2),
                summary = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                imageUrl = mockImage("art-1"),
                interests = listOf(byName("Technology"), byName("Business"), byName("Apple"))
            ),
            Article(
                id = "art-2",
                title = "Political Update: New Policy Changes Announced Today",
                source = "NewsWire",
                url = "https://example.com/2",
                publishedAt = now - ms(4),
                summary = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                imageUrl = mockImage("art-2"),
                interests = listOf(byName("Politics"), byName("USA"))
            ),
            Article(
                id = "art-3",
                title = "Business Report: Market Trends Show Growth",
                source = "Financial Times",
                url = "https://example.com/3",
                publishedAt = now - ds(1),
                summary = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                imageUrl = mockImage("art-3"),
                interests = listOf(byName("Business"), byName("Technology"))
            ),
            Article(
                id = "art-4",
                title = "Technology Innovation: AI Breakthrough",
                source = "Tech Daily",
                url = "https://example.com/4",
                publishedAt = now - ds(3),
                summary = "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                imageUrl = mockImage("art-4"),
                interests = listOf(byName("Technology"), byName("Science"))
            ),
            Article(
                id = "art-5",
                title = "Tesla Announces New Electric Vehicle Line",
                source = "Auto News",
                url = "https://example.com/5",
                publishedAt = now - ms(5),
                summary = "Elon Musk's company reveals ambitious plans for next-generation electric vehicles targeting mass market.",
                imageUrl = mockImage("art-5"),
                interests = listOf(byName("Tesla"), byName("Elon Musk"), byName("Technology"), byName("Business"))
            ),
            Article(
                id = "art-6",
                title = "Elon Musk Addresses SpaceX Mars Mission",
                source = "Space Today",
                url = "https://example.com/6",
                publishedAt = now - ms(6),
                summary = "SpaceX founder outlines timeline for crewed Mars missions and discusses challenges ahead.",
                imageUrl = mockImage("art-6"),
                interests = listOf(byName("Elon Musk"), byName("Technology"), byName("Science"))
            ),
            Article(
                id = "art-7",
                title = "Climate Summit Reaches Historic Agreement",
                source = "World News",
                url = "https://example.com/7",
                publishedAt = now - ds(2),
                summary = "Global leaders commit to ambitious carbon reduction targets in landmark environmental accord.",
                imageUrl = mockImage("art-7"),
                interests = listOf(byName("Climate"), byName("Politics"))
            ),
            Article(
                id = "art-8",
                title = "New Study Reveals Surprising Health Benefits",
                source = "Health Monitor",
                url = "https://example.com/8",
                publishedAt = now - ds(1),
                summary = "Researchers uncover unexpected connections between lifestyle choices and long-term wellness.",
                imageUrl = mockImage("art-8"),
                interests = listOf(byName("Health"), byName("Science"))
            ),
            Article(
                id = "art-9",
                title = "Taylor Swift Tour Breaks Entertainment Records",
                source = "Entertainment Weekly",
                url = "https://example.com/9",
                publishedAt = now - ms(12),
                summary = "Record-breaking tour continues to dominate charts and set new benchmarks for live performances.",
                imageUrl = mockImage("art-9"),
                interests = listOf(byName("Taylor Swift"), byName("Entertainment"))
            ),
            Article(
                id = "art-10",
                title = "Google and Microsoft Expand AI Partnerships",
                source = "TechCrunch",
                url = "https://example.com/10",
                publishedAt = now - ms(8),
                summary = "Major tech giants announce new collaborations to accelerate artificial intelligence development.",
                imageUrl = mockImage("art-10"),
                interests = listOf(byName("Google"), byName("Microsoft"), byName("Technology"))
            ),
            Article(
                id = "art-11",
                title = "USA Economic Policy: What's Next",
                source = "Reuters",
                url = "https://example.com/11",
                publishedAt = now - ms(10),
                summary = "Analysts weigh in on potential policy shifts and their impact on domestic markets.",
                imageUrl = mockImage("art-11"),
                interests = listOf(byName("USA"), byName("Politics"), byName("Business"))
            ),
            Article(
                id = "art-12",
                title = "Canada and UK Strengthen Trade Ties",
                source = "Global Trade News",
                url = "https://example.com/12",
                publishedAt = now - ms(14),
                summary = "Bilateral agreements signal renewed focus on transatlantic economic cooperation.",
                imageUrl = mockImage("art-12"),
                interests = listOf(byName("Canada"), byName("United Kingdom"), byName("Business"))
            ),
            Article(
                id = "art-13",
                title = "Japan Hosts International Science Symposium",
                source = "Science Daily",
                url = "https://example.com/13",
                publishedAt = now - ds(1),
                summary = "Leading researchers convene to discuss breakthroughs in renewable energy and materials science.",
                imageUrl = mockImage("art-13"),
                interests = listOf(byName("Japan"), byName("Science"), byName("Technology"))
            ),
            Article(
                id = "art-14",
                title = "Germany Leads European Climate Initiatives",
                source = "EU Environment",
                url = "https://example.com/14",
                publishedAt = now - ms(18),
                summary = "Berlin unveils ambitious green energy roadmap as EU deadline approaches.",
                imageUrl = mockImage("art-14"),
                interests = listOf(byName("Germany"), byName("Climate"), byName("Politics"))
            ),
            Article(
                id = "art-15",
                title = "Jeff Bezos Invests in Space Tourism Venture",
                source = "Space Industry Today",
                url = "https://example.com/15",
                publishedAt = now - ms(3),
                summary = "Blue Origin founder commits significant capital to commercial spaceflight expansion.",
                imageUrl = mockImage("art-15"),
                interests = listOf(byName("Jeff Bezos"), byName("Technology"), byName("Science"))
            ),
            Article(
                id = "art-16",
                title = "Amazon Expands Healthcare Division",
                source = "Business Insider",
                url = "https://example.com/16",
                publishedAt = now - ms(7),
                summary = "E-commerce giant acquires telehealth startup to bolster primary care offerings.",
                imageUrl = mockImage("art-16"),
                interests = listOf(byName("Amazon"), byName("Health"), byName("Business"))
            ),
            Article(
                id = "art-17",
                title = "Championship Finals Draw Record Sports Viewership",
                source = "Sports Central",
                url = "https://example.com/17",
                publishedAt = now - ms(1),
                summary = "Millions tune in as underdog team takes home the trophy in thrilling overtime finish.",
                imageUrl = mockImage("art-17"),
                interests = listOf(byName("Sports"), byName("Entertainment"))
            ),
            Article(
                id = "art-18",
                title = "Joe Biden Announces Infrastructure Investment Plan",
                source = "Washington Post",
                url = "https://example.com/18",
                publishedAt = now - ms(9),
                summary = "President outlines multi-year spending package focused on roads, bridges, and clean energy.",
                imageUrl = mockImage("art-18"),
                interests = listOf(byName("Joe Biden"), byName("USA"), byName("Politics"), byName("Climate"))
            ),
            Article(
                id = "art-19",
                title = "Apple Unveils Next-Generation Chip Technology",
                source = "Tech Review",
                url = "https://example.com/19",
                publishedAt = now - ms(11),
                summary = "New M-series processors promise significant gains in performance and battery life.",
                imageUrl = mockImage("art-19"),
                interests = listOf(byName("Apple"), byName("Technology"))
            ),
            Article(
                id = "art-20",
                title = "Donald Trump Returns to Campaign Trail",
                source = "Political Digest",
                url = "https://example.com/20",
                publishedAt = now - ms(15),
                summary = "Former president draws large crowds at rally as election season heats up.",
                imageUrl = mockImage("art-20"),
                interests = listOf(byName("Donald Trump"), byName("Politics"), byName("USA"))
            )
        )
    }
}
