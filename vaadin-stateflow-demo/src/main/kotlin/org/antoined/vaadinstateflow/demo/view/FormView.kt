package org.antoined.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.integerField
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.antoined.vaadinstateflow.binding.bindEnabled
import org.antoined.vaadinstateflow.binding.bindText
import org.antoined.vaadinstateflow.demo.model.Person
import org.antoined.vaadinstateflow.demo.viewmodel.FormViewModel
import org.antoined.vaadinstateflow.form.bindToStateFlow
import org.antoined.vaadinstateflow.viewmodel.StateFlowView

@Route("form", layout = MainLayout::class)
@PageTitle("Form Demo")
class FormView : StateFlowView<FormViewModel>(::FormViewModel) {

    val root = ui {
        verticalLayout {
            h2("Form Binding Demo")

            // Binder with JSR 303 validation
            val binder = BeanValidationBinder(Person::class.java)

            formLayout {
                val nameField = textField("Name")
                val ageField = integerField("Age")
                val emailField = textField("Email")

                binder.forField(nameField).bind("name")
                binder.forField(ageField).bind("age")
                binder.forField(emailField).bind("email")
            }

            // Bridge binder to StateFlow
            val formBinding = binder.bindToStateFlow(
                flow = viewModel.person,
                beanFactory = { Person() },
                component = this@FormView,
                onCommit = { viewModel.onPersonCommitted(it) }
            )

            // Commit / Discard buttons
            horizontalLayout {
                button("Save") {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    onClick { formBinding.commit() }
                    bindEnabled(viewModel.saving) { !it }
                }
                button("Discard") { onClick { formBinding.discard() } }
            }

            // Status
            span { bindText(viewModel.lastSaved) { saved ->
                if (saved != null) "Last saved: ${saved.name}, ${saved.age}, ${saved.email}"
                else "Not saved yet"
            } }
        }
    }
}
