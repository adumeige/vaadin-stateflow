package org.antoined.vaadinstateflow.core

import com.vaadin.flow.component.UI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory

/**
 * Bridges Kotlin Flows to Vaadin's UI thread.
 *
 * Collects from a [Flow] within a [CoroutineScope] and dispatches each emission
 * inside [UI.access], ensuring thread-safe UI updates. Requires `@Push` on the app shell.
 */
class FlowObserver(private val ui: UI) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val jobs = mutableListOf<Job>()

    /**
     * Starts observing the given [flow], invoking [action] on the Vaadin UI thread
     * for each emitted value.
     */
    fun <T> observe(flow: Flow<T>, action: (T) -> Unit): Job {
        val job = scope.launch {
            flow.collect { value ->
                ui.access { action(value) }
            }
        }
        jobs.add(job)
        return job
    }

    /**
     * Cancels all active observations and the underlying coroutine scope.
     */
    fun cancel() {
        if (scope.isActive) {
            scope.cancel()
            jobs.clear()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(FlowObserver::class.java)
    }
}
