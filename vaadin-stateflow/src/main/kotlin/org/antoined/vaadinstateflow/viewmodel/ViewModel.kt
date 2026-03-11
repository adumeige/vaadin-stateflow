package org.antoined.vaadinstateflow.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base class for ViewModels in the StateFlow MVVM pattern.
 *
 * Provides a [CoroutineScope] for launching async operations.
 * The scope is cancelled when [onDetach] is called (tied to the Vaadin component lifecycle).
 */
abstract class ViewModel {

    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Called when the associated view is attached to the UI. */
    open fun onAttach() {}

    /** Called when the associated view is detached. Cancels the coroutine scope. */
    open fun onDetach() {
        scope.cancel()
    }
}
