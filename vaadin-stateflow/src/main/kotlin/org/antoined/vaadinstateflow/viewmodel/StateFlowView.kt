package org.antoined.vaadinstateflow.viewmodel

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.antoined.vaadinstateflow.core.flowObserver

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
        observer.cancel()
        super.onDetach(detachEvent)
    }

    /** Derives a new [StateFlow] by transforming each value of the source flow.
     *  Uses the ViewModel's [scope] and [SharingStarted.Eagerly]. */
    fun <T, R> StateFlow<T>.reflow(transform: (T) -> R): StateFlow<R> =
        map(transform).stateIn(viewModel.scope, SharingStarted.Eagerly, transform(value))

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
