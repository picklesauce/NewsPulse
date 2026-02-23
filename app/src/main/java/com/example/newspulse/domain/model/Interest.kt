package com.example.newspulse.domain.model

data class Interest(
    val id: String,
    val type: InterestType,
    val name: String
) {
    fun isValid(): Boolean = id.isNotBlank() && name.isNotBlank()
}
