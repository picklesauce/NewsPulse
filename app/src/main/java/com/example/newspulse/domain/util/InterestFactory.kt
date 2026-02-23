package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

/**
 * Creates an [Interest] from a topic/name string.
 * @param idx Unique index for stable id generation (e.g. when creating multiple interests)
 * @param type Interest type (defaults to Topic for simple topic strings)
 */
fun String.toInterest(idx: Int, type: InterestType = InterestType.Topic): Interest =
    Interest(id = "i-$idx-$this", type = type, name = this)
