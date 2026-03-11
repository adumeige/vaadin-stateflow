package org.antoined.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.antoined.vaadinstateflow.viewmodel.ViewModel

class BindingsViewModel : ViewModel() {

    private val mutableMessage = MutableStateFlow("Hello, StateFlow!")
    val message: StateFlow<String> = mutableMessage.asStateFlow()

    private val mutablePanelVisible = MutableStateFlow(true)
    val panelVisible: StateFlow<Boolean> = mutablePanelVisible.asStateFlow()

    private val mutableButtonEnabled = MutableStateFlow(true)
    val buttonEnabled: StateFlow<Boolean> = mutableButtonEnabled.asStateFlow()

    private val mutableStyleClass = MutableStateFlow<String?>(null)
    val styleClass: StateFlow<String?> = mutableStyleClass.asStateFlow()

    // Derived: combines message + visibility into a status string
    val status = combine(message, panelVisible) { msg, visible ->
        "Message: \"$msg\" | Panel visible: $visible"
    }

    fun updateMessage(msg: String) {
        mutableMessage.update { msg }
    }

    fun toggleVisibility() {
        mutablePanelVisible.update { !mutablePanelVisible.value }
    }

    fun toggleEnabled() {
        mutableButtonEnabled.update { !mutableButtonEnabled.value }
    }

    fun cycleStyle() {
        mutableStyleClass.value = when (mutableStyleClass.value) {
            null -> "highlight"
            "highlight" -> "dimmed"
            else -> null
        }
    }
}
