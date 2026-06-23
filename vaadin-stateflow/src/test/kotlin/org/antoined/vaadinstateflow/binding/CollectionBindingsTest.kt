package org.antoined.vaadinstateflow.binding

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.github.mvysny.karibudsl.v10.span
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CollectionBindingsTest {

    @Test
    fun `bindNodes binds key membership and installs component renderer`() {
        UI.setCurrent(UI())
        val membership = MutableStateFlow(listOf(1, 2))
        val nodes = mapOf(
            1 to MutableStateFlow("one"),
            2 to MutableStateFlow("two"),
        )
        val grid = Grid<Int>()

        val column = grid.bindNodes(
            membership = membership,
            node = { nodes.getValue(it) },
        ) { value ->
            span(value ?: "missing")
        }

        assertEquals(listOf(1, 2), grid.dataProvider.fetch(Query()).toList())
        assertIs<ComponentRenderer<*, Int>>(column.renderer)
    }
}
