package org.antoined.vaadinstateflow.viewmodel

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import org.antoined.vaadinstateflow.core.FlowObserver
import org.antoined.vaadinstateflow.core.flowObserver
import org.atmosphere.config.service.Get

/**
 * Base view class that manages a [ViewModel]'s lifecycle alongside the Vaadin component lifecycle.
 *
 * The ViewModel is created via [viewModelProvider], attached when the view attaches to the UI,
 * and detached (scope cancelled) when the view detaches.
 */
abstract class StateFlowView<VM : ViewModel>(
    private val viewModelProvider: () -> VM

//) : Composite<VerticalLayout>() {
) : KComposite() {
    val viewModel: VM = viewModelProvider()
    protected val observer = flowObserver()


//    protected  var observer: FlowObserver
//        private set

    override fun onAttach(attachEvent: com.vaadin.flow.component.AttachEvent) {
        super.onAttach(attachEvent)
//        observer = flowObserver()
        viewModel.onAttach()
//        initView()
    }

    override fun onDetach(detachEvent: com.vaadin.flow.component.DetachEvent) {
        viewModel.onDetach()
        super.onDetach(detachEvent)
    }

    /** Called after the ViewModel is created and attached. Build your UI here. */
//    protected abstract fun initView()
}

/**
 * Karibu-DSL style: attach a ViewModel to any component.
 *
 * Creates the ViewModel, wires lifecycle, and executes [block] with the ViewModel as receiver.
 */
fun <VM : ViewModel> HasElement.withViewModel(
    provider: () -> VM,
    block: VM.() -> Unit
) {
    val component = this as Component
    val vm = provider()

    component.addAttachListener {
        vm.onAttach()
        vm.block()
    }
    component.addDetachListener {
        vm.onDetach()
    }
}
