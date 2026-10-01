# vaadin-stateflow

An adapter between Kotlin's `StateFlow` and Vaadin, built for use with [karibu-dsl](https://github.com/mvysny/karibu-dsl).

State lives in a `StateFlow`; bindings collect from it and push updates onto the
Vaadin UI thread (via `UI.access`). Collection is scoped to the component's
attach/detach lifecycle, so bindings clean themselves up.

> **Requires `@Push`** on the application shell — updates are delivered
> asynchronously from background coroutines.

---

## Decision guide (read this first)

Pick the binding by **what you are binding to**. When several apply, prefer the
one nearer the top of each table — it is the more specific / more efficient
choice.

### Binding a single component property to a flow

| You want to drive…           | Use                         | Notes |
|------------------------------|-----------------------------|-------|
| text of a label/span         | `bindText(flow)`            | `flow: StateFlow<String>`; overload with `transform` for non-`String` flows |
| value of a field             | `bindValue(flow)`           | one-way flow → field; see two-way below if the user edits |
| visibility                   | `bindVisible(flow)`         | `StateFlow<Boolean>`, or `transform` overload |
| enabled state                | `bindEnabled(flow)`         | `StateFlow<Boolean>`, or `transform` overload |
| one CSS class                | `bindClassName(flow)`       | `StateFlow<String?>`; swaps the previous class |
| a set of CSS classes         | `bindClassNames(flow)`      | `StateFlow<Set<String>>`; diffs add/remove |
| any other property           | `bindProperty(flow) { … }`  | **escape hatch** — only when nothing above fits |

> **Prefer the named binding over `bindProperty`.** Reach for `bindProperty`
> only for a property with no dedicated helper.

### Binding a collection to a data component

| Component / need                                  | Use                          | Why prefer it |
|---------------------------------------------------|------------------------------|---------------|
| **`Grid`** (default)                              | `bindDataProvider(flow)`     | **Preferred for grids.** Backs the grid with a `ListDataProvider` and `refreshAll()`s in place — keeps sorting/filtering, cheaper than swapping items |
| `Grid` rows whose **content updates per-row**     | `bindNodes(membership, …)`   | Separates list membership from per-row content; each row tracks its own `StateFlow` |
| `ComboBox` / `Select` / `VirtualList`             | `bindItems(flow)`            | These have no data-provider helper; `bindItems` is the right call |
| `Grid` when you explicitly want full `setItems`   | `bindItems(flow)`            | Simpler but replaces the whole list each emit — **prefer `bindDataProvider`** unless you have a reason |
| children of a **layout** (`VerticalLayout`, `Div`, …) | `withFlow(flow) { … }`   | Rebuilds container children from a factory; not for `Grid` |
| you need the `ListDataProvider` object itself     | `flow.asDataProvider(this)`  | Lower-level; `bindDataProvider` wraps this for grids |

> **Grid rule of thumb:** `bindDataProvider` > `bindItems`. Use `bindNodes` only
> when individual rows have live-updating content.

### Letting the user edit (writing back)

| Need                                          | Use                                  | Notes |
|-----------------------------------------------|--------------------------------------|-------|
| a single editable field, you control the write | `bindTwoWay(flow) { onCommit }`     | flow drives display; user edits arrive as `FieldChangeEvent`; **you** decide whether/how to apply |
| a whole form over a bean                       | `renderForm(schema, flow) { … }`     | **Preferred for forms.** Auto-builds a `FormLayout`, dispatches widgets by property type, auto-commits each change |
| manual form commit/discard control             | `binder.bindToStateFlow(…)`          | Returns a `FormBinding`; call `commit()` / `discard()` yourself |

### Deriving a flow from another flow

| Where you are              | Use                          | Returns |
|----------------------------|------------------------------|---------|
| inside a `ViewModel`       | `flow.reflow { … }`          | `UIStateFlow<R>` (scoped, chainable) |
| inside a `StateFlowComponent` / `StateFlowView` | `flow.reflow { … }` | `StateFlow<R>` |

Both use the surrounding scope and `SharingStarted.Eagerly`. Prefer these over a
hand-rolled `map(...).stateIn(...)` so the derived flow is cancelled with its owner.

---

## Maven packages

Releases use the Maven Central coordinates below. Once `0.0.1` is published,
consumers need only this dependency, with no extra repository or credentials:

```xml
<dependency>
    <groupId>io.github.adumeige.vaadin-stateflow</groupId>
    <artifactId>vaadin-stateflow</artifactId>
    <version>0.0.1</version>
</dependency>
```

The same signed artifacts are mirrored to
[GitHub Packages](https://github.com/adumeige/vaadin-stateflow/packages) and attached
to [GitHub Releases](https://github.com/adumeige/vaadin-stateflow/releases).
GitHub Packages still requires a repository declaration and authenticated Maven
downloads; Maven Central is the recommended source for consumers.

The new Maven group replaces `org.antoined`. Kotlin package names and imports are
unchanged. Pushes and pull requests build and test the library and demo; they do
not publish snapshots or releases.

### Publishing a release

Configure these repository secrets in **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `CENTRAL_USERNAME` | Sonatype Central Portal token username |
| `CENTRAL_PASSWORD` | Sonatype Central Portal token password |
| `GPG_PRIVATE_KEY` | Full ASCII-armored exported private signing key |
| `GPG_PASSPHRASE` | Signing key passphrase |

The `io.github.adumeige` namespace must be verified in Central, and the public
signing key must be on a supported keyserver, such as `keyserver.ubuntu.com`.
The existing account token and signing key used for other projects can be reused.
GitHub publishing uses the built-in `GITHUB_TOKEN`; no additional token secret is needed.

1. Merge the release changes into `main` and check that CI passes.
2. Open **Actions → Build and publish StateFlow → Run workflow**.
3. Select `main` and enter a new release version, initially `0.0.1`.
4. The workflow creates a release commit with versioned POMs, builds and signs
   the parent POM and library once, and automatically publishes to Central.
   It waits up to an hour for publication; no portal **Publish** click is required.
5. It creates an annotated `v<version>` tag and draft GitHub Release, mirrors and
   verifies the same signed artifacts in GitHub Packages, attaches downloadable
   artifacts, and makes the release public with generated release notes.

The demo is tested but excluded from publication. The library release includes
Kotlin sources and a Dokka-generated API documentation JAR, plus signatures.
The tag points to the versioned release commit; `main` keeps its snapshot version.
Published versions are immutable, so choose a new version for each release.

### Recovering an interrupted release

Central and GitHub publication is sequential. If Central succeeds but the GitHub
job fails, use **Re-run failed jobs** on that same Actions run. The retained bundle
and source allow a retry for 90 days without rebuilding or republishing to Central.
The mirror skips identical files already uploaded and rejects conflicting ones.
The GitHub Release stays a draft until its packages and release assets succeed;
its tag may already be visible.

Do not rerun all jobs or start another run for a version already published to
Central. If the Central job itself fails or times out, check the deployment in
[Sonatype Central Portal](https://central.sonatype.com/publishing/deployments)
first: publication may have continued after the runner stopped. The saved bundle
allows manual recovery without rebuilding.

To verify the release artifacts locally without signing or publishing:

```bash
mvn -Pcentral-release verify -pl vaadin-stateflow -am -Dgpg.skip=true
```

A local `-Pcentral-release deploy` stages for manual approval in Central by default;
the workflow explicitly enables automatic publication.

## Setup

```kotlin
@Push          // required — bindings deliver updates asynchronously
@Theme("my-theme")
class AppShell : AppShellConfigurator
```

Coordinates: `io.github.adumeige.vaadin-stateflow:vaadin-stateflow` (depends on `kotlinx-coroutines-core`;
Vaadin and `karibu-dsl` are expected on the app classpath).

---

## Recipes

All examples assume a karibu-dsl view and a ViewModel exposing `StateFlow`s.

### Single-value bindings

```kotlin
span { bindText(viewModel.title) }
span { bindText(viewModel.count) { "Items: $it" } }      // transform overload

textField { bindValue(viewModel.name) }                  // one-way flow → field

button("Save") { bindEnabled(viewModel.canSave) }
button("Save") { bindEnabled(viewModel.errors) { it.isEmpty() } }

div { bindVisible(viewModel.loading) }
div { bindClassName(viewModel.status) }                  // StateFlow<String?>
div { bindClassNames(viewModel.cssClasses) }             // StateFlow<Set<String>>

// Escape hatch — only when no named binding fits:
icon { bindProperty(viewModel.color) { style.set("color", it) } }
```

### Collections

```kotlin
// Grid — preferred:
grid<Person> {
    bindDataProvider(viewModel.people)
}

// equivalently, if you need the provider object (e.g. to set a sort):
grid<Person> {
    dataProvider = viewModel.people.asDataProvider(this)
}

// ComboBox / Select / VirtualList:
comboBox<Tag> { bindItems(viewModel.tags) }

// Layout children from a flow:
verticalLayout {
    withFlow(viewModel.messages) { msg -> Span(msg.text) }
}

// Rows with per-row live content:
grid<NodeId> {
    bindNodes(
        membership = viewModel.visibleNodes,      // StateFlow<List<NodeId>>
        node = { id -> viewModel.nodeState(id) }, // NodeId -> StateFlow<NodeState?>
        render = { state -> span(state?.label ?: "…") },
    )
}
```

### Two-way field

```kotlin
textField {
    bindTwoWay(viewModel.query) { event ->
        // event.newValue is what the user typed; you decide what to do.
        viewModel.onQueryChanged(event.newValue)
    }
}
```

### Forms

Declare a schema once (it is plain data, reusable across views), then render it.
Widgets are chosen from each property's type:

| Property type        | Widget                          |
|----------------------|---------------------------------|
| `Boolean`            | `Checkbox`                      |
| enum `E` / `E?`      | `RadioButtonGroup<E>`           |
| `Set<E>` (enum `E`)  | `CheckboxGroup<E>`              |
| `String`             | `TextField`                     |
| `Int`                | `IntegerField`                  |
| `Double`             | `NumberField`                   |

```kotlin
data class Profile(var name: String = "", var role: Role = Role.USER, var active: Boolean = true)

val profileSchema = formSchema<Profile> {
    field(Profile::name)   { label = "Full name" }
    field(Profile::role)   { label = "Role"; itemLabel { it.displayName } }
    field(Profile::active) { label = "Active" }
    // override a widget per field:
    // field(Profile::role) { widget = { ComboBox<Role>() } }
}

// In the view — auto-commits every change back to onUpdate:
renderForm(profileSchema, viewModel.profile) { updated ->
    viewModel.save(updated)
}
```

Need explicit commit/discard instead of auto-commit? Bind the `Binder` directly:

```kotlin
val binding = binder.bindToStateFlow(
    flow = viewModel.profile,
    beanFactory = { Profile() },
    component = this,
    onCommit = { viewModel.save(it) },
)
// later:
binding.commit()   // Result<Profile> — validates, writes, calls onCommit
binding.discard()  // reset fields to the current flow value
```

### Derived flows

```kotlin
class MyViewModel : ViewModel() {
    val items = MutableStateFlow<List<Item>>(emptyList())
    val count = items.reflow { it.size }               // UIStateFlow<Int>
    val isEmpty = items.reflow { it.isEmpty() }
}

class MyView : StateFlowView<MyViewModel>(::MyViewModel) {
    init {
        val label = viewModel.items.reflow { "${it.size} items" }   // StateFlow<String>
        span { bindText(label) }
    }
}
```

---

## MVVM structure

- **`ViewModel`** — holds state as `StateFlow`s and a `scope` (cancelled on
  detach by default; override `cancelScopeOnDetach` for session-scoped VMs).
  Provides `reflow` / `asUIStateFlow`.
- **`StateFlowView<VM>`** — a `KComposite` that owns a `ViewModel`, wires its
  attach/detach lifecycle, and provides `reflow`.
- **`StateFlowComponent`** — `StateFlowView` without the ViewModel ownership;
  takes a scope and provides `reflow` + a managed `observer`.
- **`withViewModel(provider) { … }`** — attach a ViewModel to any component
  without subclassing, for one-off layouts.

```kotlin
@Route("people")
class PeopleView : StateFlowView<PeopleViewModel>(::PeopleViewModel) {
    init {
        // ...built with karibu-dsl
        grid<Person> { bindDataProvider(viewModel.people) }
    }
}
```

---

## Lifecycle internals (rarely needed directly)

- `FlowObserver` — collects a flow and runs each emission inside `UI.access`;
  `cancel()` stops all observations. Obtained per-component via `Component.flowObserver()`.
- `UI.stateFlowScope` / `UI.flowObserver` — UI-lifecycle-scoped scope/observer,
  auto-cancelled on UI detach.

All the `bind*` helpers acquire a component-scoped `FlowObserver` for you, so you
normally never touch these.
