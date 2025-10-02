package com.objmobile.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

interface VpnStatusUi {
    @Composable
    fun Show()
}

class TextStatus(private val statusText: String) : VpnStatusUi {
    @Composable
    override fun Show() {
        Text(text = "Status: $statusText")
    }
}