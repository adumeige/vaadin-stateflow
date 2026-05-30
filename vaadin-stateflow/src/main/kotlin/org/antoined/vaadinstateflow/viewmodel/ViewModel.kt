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

    /**
     * Whether [onDetach] should cancel [scope]. Default `true` matches the
     * common case where the ViewModel and its view share a lifecycle
     * (one VM per view instance).
     *
     * Override to `false` when the VM outlives the view — typically
     * `@VaadinSessionScope` or any singleton VM that survives page
     * reloads. With the default behaviour, the VM's `scope` would be
     * cancelled on detach but the VM instance would be reused on the
     * next attach, leaving every derived StateFlow (`reflow`, `stateIn`)
     * silently dead. VMs that opt out are responsible for cancelling
     * [scope] themselves at the appropriate point (e.g. `@PreDestroy`).
     */
    protected open val cancelScopeOnDetach: Boolean = true

    /** Called when the associated view is attached to the UI. */
    open fun onAttach() {}

    /** Wraps a [StateFlow] into a [UIStateFlow] bound to this ViewModel's [scope]. */
    fun <T> StateFlow<T>.asUIStateFlow(): UIStateFlow<T> = UIStateFlow(this, scope)

    /** Derives a new [UIStateFlow] by transforming each value of the source flow. */
    fun <T, R> StateFlow<T>.deriveState(transform: (T) -> R): UIStateFlow<R> =
        asUIStateFlow().deriveState(transform)

    /**
     * Called when the associated view is detached. Cancels the coroutine
     * scope by default; see [cancelScopeOnDetach] to opt out for VMs whose
     * lifecycle is wider than the view's.
     */
    open fun onDetach() {
        if (cancelScopeOnDetach) scope.cancel()
    }
}
