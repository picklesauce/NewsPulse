package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.ReadingHistoryItem

class ReadingHistoryViewModel(private val model: NewsPulseModel) : ViewModel() {
    val readingHistory: List<ReadingHistoryItem> get() = model.getReadingHistory()
}
