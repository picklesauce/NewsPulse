package com.example.newspulse.domain

/**
 * Names of the fixed Discover grid on ExploreScreen. Used to split followed topics into
 * "categories" (these) vs user-specific interests in the Home feed UI. DB storage is unchanged.
 */
object DiscoverCategories {
    val NAMES: Set<String> = setOf(
        "Technology",
        "Finance",
        "Politics",
        "Artificial Intelligence",
        "Cryptocurrency",
        "Space",
        "Health & Wellness",
        "Business",
        "Sports",
        "Entertainment",
        "Science",
        "Environment",
        "Education",
        "World News"
    )
}
