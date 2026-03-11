package org.antoined.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.column
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.progressBar
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.header2
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.antoined.vaadinstateflow.binding.bindItems
import org.antoined.vaadinstateflow.binding.bindText
import org.antoined.vaadinstateflow.binding.bindVisible
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.demo.viewmodel.AsyncViewModel
import org.antoined.vaadinstateflow.viewmodel.StateFlowView

@Route("async", layout = MainLayout::class)
@PageTitle("Async Demo")
class AsyncView : StateFlowView<AsyncViewModel>(::AsyncViewModel) {

    val root = ui {
        verticalLayout {
            h2("Async Loading Demo")

            // Loading indicator
            progressBar {
                isIndeterminate = true
                bindVisible(viewModel.loading)
            }

            // Error display
            span {
                style.set("color", "var(--lumo-error-color)")
                bindText(viewModel.error) { it ?: "" }
                bindVisible(viewModel.error) { it != null }
            }

            // Data grid
            grid(Person::class.java) {
                column(Person::name) { header2 = "Name" }
                column(Person::age) { header2 = "Age" }
                column(Person::email) { header2 = "Email" }
                bindItems(viewModel.data)
            }

            // Action buttons
            horizontalLayout {
                button("Load Data") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    addClickListener { viewModel.loadData() }
                }
                button("Simulate Error") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    addClickListener { viewModel.simulateError() }
                }
            }
        }
    }
}
