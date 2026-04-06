package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType

//creater interest from topic/ string name
fun String.toInterest(idx: Int, type: InterestType = InterestType.Topic): Interest =
    Interest(id = "i-$idx-$this", type = type, name = this)
