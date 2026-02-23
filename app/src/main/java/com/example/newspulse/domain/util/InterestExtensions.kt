package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

fun Iterable<Interest>.filterByType(type: InterestType): List<Interest> = filter { it.type == type }
