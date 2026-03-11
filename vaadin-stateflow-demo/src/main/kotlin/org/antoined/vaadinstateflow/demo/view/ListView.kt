package org.antoined.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.column
import com.github.mvysny.karibudsl.v10.componentColumn
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.integerField
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.header2
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.antoined.vaadinstateflow.binding.bindItems
import org.antoined.vaadinstateflow.binding.bindTwoWay
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.demo.viewmodel.ListViewModel
import org.antoined.vaadinstateflow.viewmodel.StateFlowView

@Route("list", layout = MainLayout::class)
@PageTitle("List Demo")
class ListView : StateFlowView<ListViewModel>(::ListViewModel) {

    val root = ui {
        verticalLayout {
            h2("List Binding Demo")
            textField("Filter by name") { bindTwoWay(viewModel.filter) { viewModel.setFilter(it.newValue) } }

            grid(Person::class.java) {
                column(Person::name) { header2 = "Name"; isSortable = true }
                column(Person::age) { header2 = "Age"; isSortable = true }
                column(Person::email) { header2 = "Email"; isSortable = true }

                componentColumn({person -> Button("Remove") { viewModel.removePerson(person) }}) {
                    header2 = "Actions"
                }
                bindItems(viewModel.filteredPeople)
            }

            horizontalLayout {
                val nameField = textField("Name")
                val ageField = integerField("Age")
                val emailField = textField("Email")
                button ("Add Person") {
                    onClick {
                        val person = Person(
                            name = nameField.value ?: "",
                            age = ageField.value ?: 0,
                            email = emailField.value ?: ""
                        )
                        viewModel.addPerson(person)
                        nameField.clear()
                        ageField.clear()
                        emailField.clear()
                    }
                }
                button("Add Random") { onClick { viewModel.addRandomPerson() } }
                defaultVerticalComponentAlignment = FlexComponent.Alignment.END
            }
        }
    }
}
