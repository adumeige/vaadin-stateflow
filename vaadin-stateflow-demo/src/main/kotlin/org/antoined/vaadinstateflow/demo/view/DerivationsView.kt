package org.antoined.vaadinstateflow.demo.view

import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.antoined.vaadinstateflow.binding.bindValue
import org.antoined.vaadinstateflow.core.toDataProvider
import org.antoined.vaadinstateflow.demo.model.Product
import org.antoined.vaadinstateflow.demo.service.RandomProviderService
import org.antoined.vaadinstateflow.demo.viewmodel.DerivationsViewModel
import org.antoined.vaadinstateflow.viewmodel.StateFlowView

@Route("derivations", layout = MainLayout::class)
@PageTitle("Derivations")
class DerivationsView(service: RandomProviderService) :
    StateFlowView<DerivationsViewModel>({ DerivationsViewModel(service) }) {

    val root = ui {
        horizontalLayout {
            verticalLayout {
                textField { bindValue(viewModel.nameFlow) }
                textField { bindValue(viewModel.personFlow.deriveState { it.email }) }
            }

            grid<Product> {
                width = "100%"
                dataProvider = viewModel.hotProducts.toDataProvider(observer)
                addColumn { it.name }
                addColumn { it.price }
                addColumn { it.quantity }
            }
        }
    }
}
