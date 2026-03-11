package org.antoined.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.viewmodel.ViewModel
import kotlin.String

class ListViewModel : ViewModel() {

    private val people = MutableStateFlow(
        listOf(
            Person("Alice", 30, "alice@example.com"),
            Person("Bob", 25, "bob@example.com"),
            Person("Charlie", 35, "charlie@example.com")
        )
    )

    private val mutableFilter = MutableStateFlow("")
    val filter: StateFlow<String> = mutableFilter.asStateFlow()

    /** Filtered list derived from people + filter. */
    val filteredPeople: StateFlow<List<Person>> = combine(people, mutableFilter) { people, query ->
        if (query.isBlank()) people
        else people.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(scope, SharingStarted.Eagerly, people.value)

    fun setFilter(query: String) = mutableFilter.update { query }

    fun addPerson(person: Person) {
        people.update { it + person }
    }

    fun addRandomPerson() = addPerson(Person.random())
    fun removePerson(person: Person) = people.update { it - person }
}
