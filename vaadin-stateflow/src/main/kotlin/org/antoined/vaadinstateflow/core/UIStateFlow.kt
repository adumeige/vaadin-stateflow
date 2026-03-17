package org.antoined.vaadinstateflow.core

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

    fun <R> deriveState(transform: (T) -> R): UIStateFlow<R> = UIStateFlow(
        delegate.map(transform).stateIn(scope, SharingStarted.Eagerly, transform(value)),
        scope
    )

    companion object
}

/**
 * Creates a [ListDataProvider] backed by this [UIStateFlow] of a list.
 *
 * The data provider is initialized with the current flow value and automatically
 * refreshes whenever the flow emits a new list. The [FlowObserver] ties collection
 * to the component lifecycle (cancelled on detach) and dispatches via UI.access().
 */
fun <T> UIStateFlow<List<T>>.toDataProvider(observer: FlowObserver): ListDataProvider<T> {
    val items = ArrayList(value)
    val dataProvider = ListDataProvider(items)
    observer.observe(this) { newItems ->
        items.clear()
        items.addAll(newItems)
        dataProvider.refreshAll()
    }
    return dataProvider
}
