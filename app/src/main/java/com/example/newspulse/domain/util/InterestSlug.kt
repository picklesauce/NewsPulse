package com.example.newspulse.domain.util

import java.util.Locale

/**
 * Stable `interests.id` values for catalog rows and Discover topics (`int_*` in Supabase).
 * Must match [com.example.newspulse.data.SupabaseInterestsCatalogRepository] insert logic.
 */
object InterestSlug {
    fun stableIdForName(name: String): String =
        "int_" + name.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "topic" }
}
