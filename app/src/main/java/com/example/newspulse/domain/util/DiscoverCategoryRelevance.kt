package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import java.util.Locale

/**
 * Post-filters Event Registry keyword results for Discover categories.
 * The API often returns loosely related items; we keep articles whose title or summary
 * clearly relates to the selected topic using phrases and curated terms.
 */
object DiscoverCategoryRelevance {

    private val GENERIC = setOf("news", "latest", "today", "update", "updates", "report", "reports", "breaking")

    /** Film / celebrity context — Science must not match only via "science fiction" or movie casting. */
    private val ENTERTAINMENT_FILM_PATTERN = Regex(
        "\\b(film|films|movie|movies|starring|actor|actress|hollywood|oscar|premiere|sequel|prequel|" +
            "screenplay|box office|gosling|celebrity|celebrities|casting|blockbuster)\\b",
        RegexOption.IGNORE_CASE
    )

    /** Clear STEM / research signals (not movie sci-fi). */
    private val STRONG_SCIENCE_PATTERN = Regex(
        "\\b(peer-reviewed|peer reviewed|scientific study|clinical trial|randomized trial|double-blind|" +
            "physics|chemistry|biology|neuroscience|astronomy|geology|genetics|genome|molecule|particle|" +
            "laboratory|researchers found|study published|according to scientists|nobel prize|" +
            "nature journal|science journal|new england journal|the lancet|cell journal|climate science|" +
            "space telescope|mars rover|archaeolog|paleontolog)\\b|\\bnasa\\b",
        RegexOption.IGNORE_CASE
    )

    private val SCIENCE_WORD_NOT_FICTION = Regex(
        "\\bscience\\b(?!\\s*[-]?fiction)(?!\\s+fiction)",
        RegexOption.IGNORE_CASE
    )

    private val SCI_FI_GENRE_PATTERN = Regex(
        "\\b(sci-fi|sci fi|science-fiction|science fiction)\\b",
        RegexOption.IGNORE_CASE
    )

    private val SCIENCE_SUPPORT_TERMS = listOf(
        "scientific", "physics", "chemistry", "biology", "laboratory", "experiment suggests",
        "hypothesis", "peer-reviewed", "university study", "climate study", "research suggests",
        "scientists have", "research team", "fossil", "species", "ecosystem", "vaccine trial"
    )

    /**
     * Terms and phrases (lowercase) — a match on any is enough after the full category
     * name is checked, except ultra-short tokens use word boundaries.
     */
    private val TERMS: Map<String, List<String>> = mapOf(
        "Technology" to listOf(
            "technology", "tech", "software", "hardware", "digital", "computing", "semiconductor",
            "cybersecurity", "smartphone", "silicon valley", "cloud", "laptop", "tablet",
            "microsoft", "google", "apple", "amazon", "nvidia", "intel", "startup", "gadget",
            "5g", "wi-fi", "wifi", "programming", "developer"
        ),
        "Finance" to listOf(
            "finance", "financial", "stock", "stocks", "market", "markets", "economy", "economic",
            "investment", "investor", "banking", "fed", "federal reserve", "interest rate",
            "inflation", "recession", "gdp", "nasdaq", "s&p", "dow jones", "bond", "trader",
            "wall street", "earnings", "revenue"
        ),
        "Politics" to listOf(
            "politic", "election", "congress", "senate", "parliament", "minister", "president",
            "democrat", "republican", "legislation", "ballot", "campaign", "white house",
            "government", "governor", "mayor", "vote", "voting", "policy", "lawmaker"
        ),
        "Artificial Intelligence" to listOf(
            "artificial intelligence", "machine learning", "neural", "openai", "chatgpt",
            "large language", "llm", "generative ai", "deep learning", "algorithm",
            "gpu", "chat bot", "chatbot", "ai"
        ),
        "Cryptocurrency" to listOf(
            "crypto", "cryptocurrency", "bitcoin", "ethereum", "blockchain", "defi", "token",
            "coinbase", "binance", "nft", "web3", "mining", "stablecoin", "altcoin"
        ),
        "Space" to listOf(
            "space", "nasa", "spacex", "rocket", "satellite", "orbit", "astronaut", "moon",
            "mars", "telescope", "galaxy", "launch", "international space station",
            "hubble", "james webb"
        ),
        "Health & Wellness" to listOf(
            "health", "medical", "medicine", "hospital", "doctor", "patient", "clinical", "trial",
            "disease", "cancer", "vaccine", "wellness", "mental health", "fda", "pharma",
            "therapy", "surgery", "diagnosis", "covid"
        ),
        "Business" to listOf(
            "business", "corporate", "company", "ceo", "startup", "merger", "acquisition",
            "quarterly", "profit", "revenue", "sales", "enterprise", "industry", "retail",
            "franchise", "board", "shareholder"
        ),
        "Sports" to listOf(
            "sport", "nba", "nfl", "mlb", "nhl", "soccer", "football", "basketball", "baseball",
            "tennis", "golf", "olympic", "championship", "playoff", "coach", "athlete", "stadium",
            "league", "fifa", "uefa", "world cup"
        ),
        "Entertainment" to listOf(
            "entertainment", "movie", "film", "hollywood", "celebrity", "actor", "actress",
            "music", "album", "concert", "television", "streaming", "netflix", "oscar", "grammy",
            "broadway", "box office"
        ),
        // "Science" handled by [matchesScienceCategory] — not in map
        "Environment" to listOf(
            "climate", "environment", "carbon", "emission", "renewable", "solar", "wind power",
            "wildfire", "conservation", "biodiversity", "pollution", "sustainability", "epa",
            "green energy", "fossil fuel", "deforestation"
        ),
        "Education" to listOf(
            "education", "school", "university", "college", "student", "teacher", "curriculum",
            "campus", "tuition", "scholarship", "learning", "classroom", "degree", "academic"
        ),
        "World News" to listOf(
            "global", "international", "worldwide", "overseas", "foreign", "diplomat", "diplomatic",
            "summit", "united nations", "nato", "geopolit", "embassy", "sanction", "conflict",
            "middle east", "european union", "asia-pacific", "africa", "europe", "asia"
        )
    )

    fun matchesDiscoverCategory(categoryName: String, article: Article): Boolean {
        val text = "${article.title} ${article.summary}".lowercase(Locale.US)
        if (text.isBlank()) return false

        val phrase = categoryName.trim().lowercase(Locale.US)
        // Science: must not use naive "science" substring (matches "science fiction" in movie pieces).
        if (phrase == "science") {
            return matchesScienceCategory(text)
        }
        if (phrase.length >= 4 && phrase in text) return true

        val terms = TERMS[categoryName] ?: fallbackTerms(categoryName)
        if (terms.isEmpty()) return true
        return terms.any { termMatches(text, it) }
    }

    /**
     * Blocks entertainment/film hits that only share "science" via sci-fi genre or loose API matches.
     */
    private fun matchesScienceCategory(text: String): Boolean {
        if (STRONG_SCIENCE_PATTERN.containsMatchIn(text)) return true
        if (SCIENCE_SUPPORT_TERMS.any { termMatches(text, it) }) return true

        val looksLikeFilmOrCasting = ENTERTAINMENT_FILM_PATTERN.containsMatchIn(text)
        val looksLikeSciFiGenre = SCI_FI_GENRE_PATTERN.containsMatchIn(text)
        if (looksLikeFilmOrCasting || looksLikeSciFiGenre) return false

        if (SCIENCE_WORD_NOT_FICTION.containsMatchIn(text)) {
            if (SCI_FI_GENRE_PATTERN.containsMatchIn(text)) return false
            return true
        }
        return false
    }

    private fun fallbackTerms(name: String): List<String> =
        name.split(Regex("[\\s&]+")).map { it.lowercase(Locale.US) }
            .filter { it.length >= 4 && it !in GENERIC }

    private fun termMatches(text: String, term: String): Boolean {
        val t = term.trim().lowercase(Locale.US)
        if (t.isEmpty()) return false
        if (t.any { it.isWhitespace() }) return t in text
        if (t.length <= 3) {
            return Regex("\\b${Regex.escape(t)}\\b").containsMatchIn(text)
        }
        return t in text
    }
}
