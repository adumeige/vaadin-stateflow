package org.antoined.vaadinstateflow.binding

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.virtuallist.VirtualList
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.asDataProvider
import org.antoined.vaadinstateflow.core.flowObserver

/**
 * Collection binding extensions: StateFlow<List<T>> → data components (Grid, ComboBox, Select).
 */
fun <T> Grid<T>.bindItems(flow: StateFlow<List<T>>) {
    (this as Component).flowObserver().observe(flow) { items ->
        setItems(items)
    }
}

fun <T, C : Collection<T>> Grid<T>.bindDataProvider(flow: StateFlow<C>) {
    val component = this as Component
    dataProvider = flow.asDataProvider(component)
}

fun <T> ComboBox<T>.bindItems(flow: StateFlow<List<T>>) {
    (this as Component).flowObserver().observe(flow) { items ->
        setItems(items)
    }
}

fun <T> Select<T>.bindItems(flow: StateFlow<List<T>>) {
    (this as Component).flowObserver().observe(flow) { items ->
        setItems(items)
    }
}


fun <T> VirtualList<T>.bindItems(flow: StateFlow<List<T>>) {
    (this as Component).flowObserver().observe(flow) { items ->
        setItems(items)
    }
}
