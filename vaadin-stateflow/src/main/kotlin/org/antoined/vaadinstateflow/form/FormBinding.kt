package org.antoined.vaadinstateflow.form

import com.vaadin.flow.component.Component
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.flowObserver

/**
 * Bridges a Vaadin [Binder] with a [StateFlow], providing explicit commit/discard semantics.
 *
 * - When the [flow] emits, [Binder.readBean] populates the form fields.
 * - [commit] validates and writes the bean, invoking [onCommit] on success.
 * - [discard] resets fields to the current flow value.
 */
class FormBinding<T>(
    private val binder: Binder<T>,
    private val flow: StateFlow<T>,
    private val beanFactory: () -> T,
    private val onCommit: (T) -> Unit
) {
    /**
     * Validates the form and, if valid, writes the bean and invokes the commit callback.
     *
     * @return [Result.success] with the committed bean, or [Result.failure] with the validation exception.
     */
    fun commit(): Result<T> {
        val bean = beanFactory()
        return try {
            binder.writeBean(bean)
            onCommit(bean)
            Result.success(bean)
        } catch (e: ValidationException) {
            Result.failure(e)
        }
    }

    /** Resets form fields to the current flow value. */
    fun discard() {
        binder.readBean(flow.value)
    }
}

/**
 * Creates a [FormBinding] that bridges this [Binder] with a [StateFlow].
 *
 * The binder's fields are populated from the flow, and [onCommit] is called
 * when a successful commit occurs.
 *
 * @param flow the state flow providing the bean data
 * @param beanFactory creates a new bean instance for writing (e.g., `{ Person() }`)
 * @param component the component to attach the flow observer to (for lifecycle management)
 * @param onCommit callback invoked with the validated bean on commit
 */
fun <T> Binder<T>.bindToStateFlow(
    flow: StateFlow<T>,
    beanFactory: () -> T,
    component: Component,
    onCommit: (T) -> Unit
): FormBinding<T> {
    // Populate fields whenever the flow emits
    component.flowObserver().observe(flow) { bean ->
        readBean(bean)
    }

    return FormBinding(this, flow, beanFactory, onCommit)
}
