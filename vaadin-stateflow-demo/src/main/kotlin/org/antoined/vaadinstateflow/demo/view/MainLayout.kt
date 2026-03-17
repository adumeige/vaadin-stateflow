package org.antoined.vaadinstateflow.demo.view

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.Layout

@Layout
class MainLayout : AppLayout() {
    init {
        val toggle = DrawerToggle()
        val title = H1("Vaadin StateFlow Demo").apply {
            style.set("font-size", "var(--lumo-font-size-l)")
            style.set("margin", "0")
        }


        val nav = SideNav().apply {
            addItem(SideNavItem("Counter", CounterView::class.java))
            addItem(SideNavItem("Bindings", BindingsView::class.java))
            addItem(SideNavItem("List", ListView::class.java))
            addItem(SideNavItem("Form", FormView::class.java))
            addItem(SideNavItem("Async", AsyncView::class.java))
            addItem(SideNavItem("Derivations", DerivationsView::class.java))
        }

        addToDrawer(Scroller(nav))
        addToNavbar(toggle, title)
    }
}
