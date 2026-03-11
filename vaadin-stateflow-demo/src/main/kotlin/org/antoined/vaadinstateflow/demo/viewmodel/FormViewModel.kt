package org.antoined.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.viewmodel.ViewModel

class FormViewModel : ViewModel() {

    private val _person = MutableStateFlow(Person("Jane Doe", 28, "jane@example.com"))
    val person: StateFlow<Person> = _person.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _lastSaved = MutableStateFlow<Person?>(null)
    val lastSaved: StateFlow<Person?> = _lastSaved.asStateFlow()

    fun onPersonCommitted(person: Person) {
        scope.launch {
            _saving.value = true
            delay(1000) // simulate save
            _person.value = person
            _lastSaved.value = person
            _saving.value = false
        }
    }
}
