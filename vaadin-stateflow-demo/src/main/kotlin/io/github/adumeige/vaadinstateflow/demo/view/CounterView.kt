package io.github.adumeige.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.h2
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import io.github.adumeige.vaadinstateflow.binding.bindText
import io.github.adumeige.vaadinstateflow.demo.viewmodel.CounterViewModel
import io.github.adumeige.vaadinstateflow.viewmodel.StateFlowView

@Route("counter", layout = MainLayout::class)
@PageTitle("Counter Demo")
class CounterView : StateFlowView<CounterViewModel>(::CounterViewModel) {

    val root = ui {
        verticalLayout {
            h2("Counter Demo")

            horizontalLayout {
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                isSpacing = true

                button("–") { onClick { viewModel.decrement() } }
                span { bindText(viewModel.count) { "Count: $it" } }
                button("+") { onClick { viewModel.increment() } }
            }
        }
    }
}
