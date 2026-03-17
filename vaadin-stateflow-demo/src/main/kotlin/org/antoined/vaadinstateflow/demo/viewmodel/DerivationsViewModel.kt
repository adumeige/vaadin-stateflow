package org.antoined.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.UIStateFlow
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.demo.model.Product
import org.antoined.vaadinstateflow.demo.service.RandomProviderService
import org.antoined.vaadinstateflow.viewmodel.ViewModel

class DerivationsViewModel(service: RandomProviderService) : ViewModel() {

    val personFlow: StateFlow<Person> = service.person
    val nameFlow: StateFlow<String> = service.person.deriveState { it.name }
    val hotProducts: UIStateFlow<List<Product>> = service.hotProducts.asUIStateFlow()
}