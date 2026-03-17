package org.antoined.vaadinstateflow.demo.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.demo.model.Product
import org.antoined.vaadinstateflow.demo.model.Provider
import org.springframework.stereotype.Service

@Service
class RandomProviderService {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _provider = MutableStateFlow(Provider.random())
    val provider: StateFlow<Provider> = _provider.asStateFlow()

    private val _hotProducts = MutableStateFlow(randomHotProducts())
    val hotProducts: StateFlow<List<Product>> = _hotProducts.asStateFlow()

    private val _person = MutableStateFlow(Person.random())
    val person: StateFlow<Person> = _person.asStateFlow()

    init {
        scope.launch {
            while (true) {
                delay(3000)
                _provider.value = Provider.random()
            }
        }
        scope.launch {
            while (true) {
                delay(5000)
                _hotProducts.value = randomHotProducts()
            }
        }
        scope.launch {
            while (true) {
                delay(4000)
                _person.value = Person.random()
            }
        }
    }

    private fun randomHotProducts() = (1..(5..30).random()).map { Product.random() }
}
