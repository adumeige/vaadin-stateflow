package org.antoined.vaadinstateflow.demo.model

data class Product(
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
) {
    companion object {
        private val PRODUCT_NAMES = listOf(
            "Widget", "Gadget", "Sprocket", "Bolt", "Gear",
            "Sensor", "Module", "Cable", "Adapter", "Filter"
        )

        fun random() = Product(
            name = PRODUCT_NAMES.random(),
            price = (1..10000).random() / 100.0,
            quantity = (1..500).random()
        )
    }
}

data class Provider(
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val products: List<Product> = emptyList(),
    val customers: List<Person> = emptyList()
) {
    companion object {
        private val COMPANY_NAMES = listOf(
            "Acme Corp", "Globex", "Initech", "Umbrella Inc", "Soylent",
            "Stark Industries", "Wayne Enterprises", "Cyberdyne", "Oscorp", "Aperture Science"
        )
        private val STREETS = listOf(
            "Main St", "Oak Ave", "Elm Rd", "Park Blvd", "Cedar Ln",
            "Maple Dr", "Pine St", "River Rd", "Lake Ave", "Hill St"
        )
        private val CITIES = listOf(
            "Springfield", "Shelbyville", "Ogdenville", "North Haverbrook",
            "Capital City", "Brockway", "Cypress Creek", "Waverly Hills"
        )
        private val COUNTRIES = listOf(
            "US", "UK", "DE", "FR", "JP", "CA", "AU", "NL", "SE", "CH"
        )

        fun random() = Provider(
            name = COMPANY_NAMES.random(),
            address = "${(1..999).random()} ${STREETS.random()}",
            city = CITIES.random(),
            country = COUNTRIES.random(),
            products = (1..(2..6).random()).map { Product.random() },
            customers = (1..(1..5).random()).map { Person.random() }
        )
    }
}
