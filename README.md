# vaadin-stateflow
Library providing an adapter between Kotlin's flows and Vaadin. Built for compatibility with karibu-dsl.

## DataProvider

`StateFlow<Collection<T>>` can be exposed as a Vaadin `ListDataProvider`:

```kotlin
grid<Person> {
    dataProvider = viewModel.people.asDataProvider(this)
}
```

For grids, the binding helper is equivalent:

```kotlin
grid<Person> {
    bindDataProvider(viewModel.people)
}
```
