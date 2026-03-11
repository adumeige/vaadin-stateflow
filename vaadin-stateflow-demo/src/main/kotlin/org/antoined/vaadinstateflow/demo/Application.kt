package org.antoined.vaadinstateflow.demo

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@Push
//@Theme("vaadin-stateflow-demo")
class Application : AppShellConfigurator

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
