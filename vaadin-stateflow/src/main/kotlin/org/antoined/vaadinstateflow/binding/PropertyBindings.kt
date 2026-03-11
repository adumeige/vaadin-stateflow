package org.antoined.vaadinstateflow.binding

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasEnabled
import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.HasValue
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.flowObserver

/**
 * One-way binding extensions: StateFlow → Vaadin component properties.
 *
 * Each binding auto-collects from the flow and updates the component via UI.access().
 * The binding lifecycle is tied to the component's attach/detach lifecycle.
 */

// --- Text ---

fun HasText.bindText(flow: StateFlow<String>) {
    val component = this as Component
    component.flowObserver().observe(flow) { text = it }
}

fun <T> HasText.bindText(flow: StateFlow<T>, transform: (T) -> String) {
    val component = this as Component
    component.flowObserver().observe(flow) { text = transform(it) }
}

// --- Value ---

@Suppress("UNCHECKED_CAST")
fun <E : HasValue.ValueChangeEvent<T>, T> HasValue<E, T>.bindValue(flow: StateFlow<T>) {
    val component = this as Component
    component.flowObserver().observe(flow) { value = it }
}

// --- Visibility ---

fun Component.bindVisible(flow: StateFlow<Boolean>) {
    flowObserver().observe(flow) { isVisible = it }
}

fun <T> Component.bindVisible(flow: StateFlow<T>, transform: (T) -> Boolean) {
    flowObserver().observe(flow) { isVisible = transform(it) }
}

// --- Enabled ---

fun HasEnabled.bindEnabled(flow: StateFlow<Boolean>) {
    val component = this as Component
    component.flowObserver().observe(flow) { isEnabled = it }
}

fun <T> HasEnabled.bindEnabled(flow: StateFlow<T>, transform: (T) -> Boolean) {
    val component = this as Component
    component.flowObserver().observe(flow) { isEnabled = transform(it) }
}

// --- CSS Class ---

fun Component.bindClassName(flow: StateFlow<String?>) {
    var previousClass: String? = null
    flowObserver().observe(flow) { newClass ->
        previousClass?.let { element.classList.remove(it) }
        newClass?.let { element.classList.add(it) }
        previousClass = newClass
    }
}

fun Component.bindClassNames(flow: StateFlow<Set<String>>) {
    var previousClasses = emptySet<String>()
    flowObserver().observe(flow) { newClasses ->
        val toRemove = previousClasses - newClasses
        val toAdd = newClasses - previousClasses
        toRemove.forEach { element.classList.remove(it) }
        toAdd.forEach { element.classList.add(it) }
        previousClasses = newClasses
    }
}

// --- Generic ---

fun <T> Component.bindProperty(flow: StateFlow<T>, setter: Component.(T) -> Unit) {
    flowObserver().observe(flow) { setter(it) }
}
