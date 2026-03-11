package org.antoined.vaadinstateflow.binding

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.flowObserver

/**
 * Represents a value change from a UI component, emitted as a commit event.
 */
data class FieldChangeEvent<T>(
    val component: Component,
    val oldValue: T?,
    val newValue: T,
    val isFromClient: Boolean
)

/**
 * Two-way binding with explicit commit.
 *
 * The [flow] drives the displayed value (one-way down). When the user changes
 * the value in the UI, [onCommit] is called with a [FieldChangeEvent].
 * The ViewModel decides whether/how to apply the change.
 */
@Suppress("UNCHECKED_CAST")
fun <E : HasValue.ValueChangeEvent<T>, T> HasValue<E, T>.bindTwoWay(
    flow: StateFlow<T>,
    onCommit: (FieldChangeEvent<T>) -> Unit
) {
    val component = this as Component
    val observer = component.flowObserver()

    // One-way down: flow → component value
    var updatingFromFlow = false
    observer.observe(flow) {
        updatingFromFlow = true
        value = it
        updatingFromFlow = false
    }

    // Commit events: component → ViewModel (only from client interaction)
    addValueChangeListener { event ->
        if (!updatingFromFlow && event.isFromClient) {
            onCommit(
                FieldChangeEvent(
                    component = component,
                    oldValue = event.oldValue,
                    newValue = event.value,
                    isFromClient = true
                )
            )
        }
    }
}
