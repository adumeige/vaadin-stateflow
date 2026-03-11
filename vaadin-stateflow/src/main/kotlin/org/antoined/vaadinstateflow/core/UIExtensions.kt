package org.antoined.vaadinstateflow.core

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.DetachEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

private val SCOPE_KEY = FlowObserver::class.java.name + ".scope"
private val OBSERVER_KEY = FlowObserver::class.java.name + ".observer"

/**
 * Returns (or creates) a [CoroutineScope] tied to this UI's lifecycle.
 * The scope is automatically cancelled when the UI is detached.
 */
val UI.stateFlowScope: CoroutineScope
    get() {
        var scope = ComponentUtil.getData(this, SCOPE_KEY) as? CoroutineScope
        if (scope == null) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            ComponentUtil.setData(this, SCOPE_KEY, scope)
            addDetachListener { scope.cancel() }
        }
        return scope
    }

/**
 * Returns (or creates) a [FlowObserver] tied to this UI's lifecycle.
 * The observer is automatically cancelled when the UI is detached.
 */
val UI.flowObserver: FlowObserver
    get() {
        var observer = ComponentUtil.getData(this, OBSERVER_KEY) as? FlowObserver
        if (observer == null) {
            observer = FlowObserver(this)
            ComponentUtil.setData(this, OBSERVER_KEY, observer)
            addDetachListener { observer.cancel() }
        }
        return observer
    }

/**
 * Returns (or creates) a [FlowObserver] tied to this component's lifecycle.
 * Uses the component's UI to dispatch updates, and cancels when the component detaches.
 */
fun Component.flowObserver(): FlowObserver {
    var observer = ComponentUtil.getData(this, OBSERVER_KEY) as? FlowObserver
    if (observer == null) {
        val ui = UI.getCurrent()
        observer = FlowObserver(ui)
        ComponentUtil.setData(this, OBSERVER_KEY, observer)
        addDetachListener { observer.cancel() }
    }
    return observer
}
