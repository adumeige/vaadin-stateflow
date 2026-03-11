package org.antoined.vaadinstateflow.demo.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class Person(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    var name: String = "",

    @field:Min(value = 1, message = "Age must be at least 1")
    var age: Int = 0,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Must be a valid email")
    var email: String = ""
) {
    companion object {
        fun random() = Person(
            name = (1..10).map { ('a'..'z').random() }.joinToString(""),
            age = (1..100).random(),
            email = "" + (1..10).map { ('a'..'z').random() }.joinToString("")
        )
    }
}
