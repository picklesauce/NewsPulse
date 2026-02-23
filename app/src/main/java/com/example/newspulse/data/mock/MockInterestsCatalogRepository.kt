package com.example.newspulse.data.mock

import com.example.newspulse.domain.InterestsCatalogRepository
import com.example.newspulse.domain.model.Interest

class MockInterestsCatalogRepository : InterestsCatalogRepository {
    override fun getAllInterests(): List<Interest> = MockDB.interests
}
