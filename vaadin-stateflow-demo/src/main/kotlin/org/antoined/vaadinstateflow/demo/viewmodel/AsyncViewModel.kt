package org.antoined.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.viewmodel.ViewModel

class AsyncViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _data = MutableStateFlow<List<Person>>(emptyList())
    val data: StateFlow<List<Person>> = _data.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadData() {
        scope.launch {
            _loading.value = true
            _error.value = null
            try {
                delay(2000) // simulate network call
                _data.value = listOf(
                    Person("Alice", 30, "alice@example.com"),
                    Person("Bob", 25, "bob@example.com"),
                    Person("Charlie", 35, "charlie@example.com"),
                    Person("Diana", 28, "diana@example.com")
                )
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun simulateError() {
        scope.launch {
            _loading.value = true
            _error.value = null
            delay(1500)
            _error.value = "Simulated network error"
            _loading.value = false
        }
    }
}
