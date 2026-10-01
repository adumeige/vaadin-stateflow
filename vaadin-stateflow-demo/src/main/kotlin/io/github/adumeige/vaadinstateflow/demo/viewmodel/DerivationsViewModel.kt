package io.github.adumeige.vaadinstateflow.demo.viewmodel

import kotlinx.coroutines.flow.StateFlow
import io.github.adumeige.vaadinstateflow.core.UIStateFlow
import io.github.adumeige.vaadinstateflow.demo.model.Person
import io.github.adumeige.vaadinstateflow.demo.model.Product
import io.github.adumeige.vaadinstateflow.demo.service.RandomProviderService
import io.github.adumeige.vaadinstateflow.viewmodel.ViewModel

class DerivationsViewModel(service: RandomProviderService) : ViewModel() {

    val personFlow: StateFlow<Person> = service.person
    val nameFlow: StateFlow<String> = service.person.reflow { it.name }
    val hotProducts: UIStateFlow<List<Product>> = service.hotProducts.asUIStateFlow()
}