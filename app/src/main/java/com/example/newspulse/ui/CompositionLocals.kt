package com.example.newspulse.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModelProvider

object CompositionLocals {
    val LocalViewModelFactory = compositionLocalOf<ViewModelProvider.Factory> {
        error("ViewModelFactory not provided")
    }
}
