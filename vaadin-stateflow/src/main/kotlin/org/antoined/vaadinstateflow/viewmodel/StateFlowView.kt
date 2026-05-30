package org.antoined.vaadinstateflow.viewmodel

import com.github.mvysny.karibudsl.v10.KComposite
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.antoined.vaadinstateflow.core.flowObserver

abstract class StateFlowComponent(
    private val scope: CoroutineScope
) : KComposite() {
    protected val observer = flowObserver()

    override fun onDetach(detachEvent: com.vaadin.flow.component.DetachEvent) {
        observer.cancel()
        super.onDetach(detachEvent)
    }

    /**
     * Derives a new [StateFlow] by transforming each value of the source flow.
     * Uses the ViewModel's [scope] and [SharingStarted.Eagerly].
     */
    fun <T, R> StateFlow<T>.reflow(transform: (T) -> R): StateFlow<R> =
        map(transform).stateIn(scope, SharingStarted.Eagerly, transform(value))


}

/**
 * Base view class that manages a [ViewModel]'s lifecycle alongside the Vaadin component lifecycle.
 *
 * The ViewModel is created via [viewModelProvider], attached when the view attaches to the UI,
 * and detached (scope cancelled) when the view detaches.
 */
abstract class StateFlowView<VM : ViewModel>(
    private val viewModelProvider: () -> VM,
    val viewModel: VM = viewModelProvider()
//) : Composite<VerticalLayout>() {
) : StateFlowComponent(viewModel.scope) {

//    protected val observer = flowObserver()

    override fun onAttach(attachEvent: com.vaadin.flow.component.AttachEvent) {
        super.onAttach(attachEvent)
        viewModel.onAttach()
    }

    override fun onDetach(detachEvent: com.vaadin.flow.component.DetachEvent) {
        viewModel.onDetach()
        observer.cancel()
        super.onDetach(detachEvent)
    }

//    /**
//     * Derives a new [StateFlow] by transforming each value of the source flow.
//     * Uses the ViewModel's [scope] and [SharingStarted.Eagerly].
//     */
//    fun <T, R> StateFlow<T>.reflow(transform: (T) -> R): StateFlow<R> =
//        map(transform).stateIn(viewModel.scope, SharingStarted.Eagerly, transform(value))

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
