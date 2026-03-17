package org.antoined.vaadinstateflow.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.UIStateFlow

/**
 * Base class for ViewModels in the StateFlow MVVM pattern.
 *
 * Provides a [CoroutineScope] for launching async operations.
 * The scope is cancelled when [onDetach] is called (tied to the Vaadin component lifecycle).
 */
abstract class ViewModel {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Called when the associated view is attached to the UI. */
    open fun onAttach() {}

    /** Wraps a [StateFlow] into a [UIStateFlow] bound to this ViewModel's [scope]. */
    fun <T> StateFlow<T>.asUIStateFlow(): UIStateFlow<T> = UIStateFlow(this, scope)

    /** Derives a new [UIStateFlow] by transforming each value of the source flow. */
    fun <T, R> StateFlow<T>.deriveState(transform: (T) -> R): UIStateFlow<R> =
        asUIStateFlow().deriveState(transform)

    /** Called when the associated view is detached. Cancels the coroutine scope. */
    open fun onDetach() {
        scope.cancel()
    }
}
