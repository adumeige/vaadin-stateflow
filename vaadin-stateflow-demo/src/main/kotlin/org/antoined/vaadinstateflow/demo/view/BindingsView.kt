package org.antoined.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.antoined.vaadinstateflow.binding.*
import org.antoined.vaadinstateflow.demo.viewmodel.BindingsViewModel
import org.antoined.vaadinstateflow.viewmodel.StateFlowView

@Route("bindings", layout = MainLayout::class)
@PageTitle("Bindings Demo")
class BindingsView : StateFlowView<BindingsViewModel>(::BindingsViewModel) {

    val root = ui {
        verticalLayout {
            h2("Property Binding Demo")

            // Text binding
            textField("Update message") {
                addValueChangeListener { event ->
                    if (event.isFromClient) viewModel.updateMessage(event.value)
                }
            }
            span { bindText(viewModel.message) }

            // Visibility binding
            horizontalLayout {
                button("Toggle Visibility") { onClick { viewModel.toggleVisibility()  }}
            }
            div {
                style.set("padding", "1em")
                style.set("background", "var(--lumo-primary-color-10pct)")
                style.set("border-radius", "var(--lumo-border-radius-m)")
                bindVisible(viewModel.panelVisible)
                span("This panel is conditionally visible")
            }

            // Enabled binding
            horizontalLayout {
                button("Toggle Enabled") { onClick { viewModel.toggleEnabled() } }
                button("Target Button (enable/disable me)") {
                    bindEnabled(viewModel.buttonEnabled)
                }
            }

            // CSS class binding
            horizontalLayout {
                button("Cycle Style") { onClick { viewModel.cycleStyle() } }
            }
            div {
                style.set("padding", "1em")
                bindClassName(viewModel.styleClass)
                span("Styled element")
            }

            // Derived/combined flow
            span("Combined status:")
            span { observer.observe(viewModel.status) { text = it } }
        }
    }
}
