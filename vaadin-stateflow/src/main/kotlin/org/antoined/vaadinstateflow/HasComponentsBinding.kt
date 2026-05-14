package org.antoined.vaadinstateflow

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Text
import kotlinx.coroutines.flow.StateFlow
import org.antoined.vaadinstateflow.core.flowObserver

@VaadinDsl
public fun <FLOW_T> (@VaadinDsl HasComponents).withFlow(
    flow: StateFlow<Iterable<FLOW_T>>,
    fct: (value: FLOW_T) -> Component = { Text(it.toString()) }
) {
    (this as Component).flowObserver().observe(flow) { items ->
        removeAll()
        items.forEach { value -> add(fct(value)) }
    }
}

