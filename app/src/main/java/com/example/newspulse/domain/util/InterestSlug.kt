package com.example.newspulse.domain.util

import java.util.Locale

object InterestSlug {
    fun stableIdForName(name: String): String =
        "int_" + name.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "topic" }
}
