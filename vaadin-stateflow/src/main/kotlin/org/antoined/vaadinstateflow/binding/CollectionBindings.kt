package org.antoined.vaadinstateflow.binding

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.virtuallist.VirtualList
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.function.SerializableBiConsumer
import com.vaadin.flow.function.SerializableSupplier
import kotlinx.coroutines.Job
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

/**
 * Binds Grid row membership to [membership] while rendering each row from a
 * canonical per-node [StateFlow].
 *
 * The Grid items are keys. For every visible row, [node] resolves the key to a
 * content flow and [render] rebuilds that row container whenever the node emits.
 * This separates list membership changes from per-row content refreshes.
 */
fun <Key, S> Grid<Key>.bindNodes(
    membership: StateFlow<List<Key>>,
    node: (Key) -> StateFlow<S?>,
    render: HasComponents.(S?) -> Unit,
): Grid.Column<Key> {
    bindDataProvider(membership)
    return addColumn(
        ComponentRenderer<Div, Key>(
            SerializableSupplier { Div() },
            SerializableBiConsumer { container, key ->
                val previous = ComponentUtil.getData(container, NODE_RENDER_JOB_KEY) as? Job
                previous?.cancel()
                container.removeAll()
                val job = container.flowObserver().observe(node(key)) { value ->
                    container.removeAll()
                    container.render(value)
                }
                ComponentUtil.setData(container, NODE_RENDER_JOB_KEY, job)
            },
        ),
    )
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

private const val NODE_RENDER_JOB_KEY = "org.antoined.vaadinstateflow.binding.bindNodes.job"
