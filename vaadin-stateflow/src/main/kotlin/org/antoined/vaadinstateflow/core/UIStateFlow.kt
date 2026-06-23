package org.antoined.vaadinstateflow.core

import com.vaadin.flow.component.Component
import com.vaadin.flow.data.provider.ListDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * A [StateFlow] wrapper that carries a [CoroutineScope] for deriving new state flows.
 *
 * Derived flows share the same scope, so they are all cancelled together
 * (e.g. when the ViewModel is detached).
 */
class UIStateFlow<T>(
    private val delegate: StateFlow<T>,
    internal val scope: CoroutineScope
) : StateFlow<T> by delegate {

    fun <R> reflow(transform: (T) -> R): UIStateFlow<R> = UIStateFlow(
        delegate.map(transform).stateIn(scope, SharingStarted.Eagerly, transform(value)),
        scope
    )

    companion object
}

/**
 * Creates a [ListDataProvider] backed by this [StateFlow] of a collection.
 *
 * The data provider is initialized with the current flow value and automatically
 * refreshes whenever the flow emits a new collection. The [FlowObserver] ties collection
 * to the component lifecycle (cancelled on detach) and dispatches via UI.access().
 */
fun <T, C : Collection<T>> StateFlow<C>.asDataProvider(observer: FlowObserver): ListDataProvider<T> {
    val items = ArrayList(value)
    val dataProvider = ListDataProvider(items)
    observer.observe(this) { newItems ->
        items.clear()
        items.addAll(newItems)
        dataProvider.refreshAll()
    }
    return dataProvider
}

/**
 * Creates a [ListDataProvider] backed by this [StateFlow] and scoped to [component].
 */
fun <T, C : Collection<T>> StateFlow<C>.asDataProvider(component: Component): ListDataProvider<T> =
    asDataProvider(component.flowObserver())

/**
 * Compatibility alias for the original UIStateFlow-specific API.
 */
fun <T> UIStateFlow<List<T>>.toDataProvider(observer: FlowObserver): ListDataProvider<T> =
    asDataProvider(observer)
