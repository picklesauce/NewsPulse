package com.example.newspulse.domain

import com.example.newspulse.domain.model.Interest

interface InterestsCatalogRepository {
    fun getAllInterests(): List<Interest>
}
