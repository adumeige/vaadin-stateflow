package org.antoined.vaadinstateflow.form

import com.vaadin.flow.component.HasValue
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Declarative description of a form: which bean properties to expose,
 * with their labels and (optional) per-field overrides (item labelers,
 * custom item lists).
 *
 * Combine with [renderForm] to materialise the form as Vaadin components
 * wired to a [kotlinx.coroutines.flow.StateFlow] via a [FormBinding].
 *
 * ```kotlin
 * val schema = formSchema<MyConfig> {
 *     field(MyConfig::displayName) { label = "Display name" }
 *     field(MyConfig::mode)        { label = "Mode" ; itemLabel { it.label } }
 *     field(MyConfig::tags)        { label = "Tags" }      // Set<Enum> → CheckboxGroup
 *     field(MyConfig::enabled)     { label = "Enabled" }   // Boolean   → Checkbox
 * }
 * ```
 */
class FormSchema<T : Any> internal constructor(
    val beanClass: Class<T>,
    val fields: List<FieldSpec<T, *>>,
)

/**
 * One field in a [FormSchema]. The widget the renderer picks is derived
 * from [property]'s return type (see [renderForm]'s dispatch table) —
 * unless [widget] is supplied, in which case auto-dispatch is bypassed
 * and the caller-supplied widget is bound directly.
 */
class FieldSpec<T : Any, V> internal constructor(
    val property: KMutableProperty1<T, V>,
    val label: String,
    val itemLabel: ((Any) -> String)?,
    val items: List<V>?,
    val widget: (() -> HasValue<*, *>)?,
)

/**
 * Mutable builder for a [FieldSpec]. Set [label] or [items] directly;
 * use the typed [itemLabel] extensions below to install an item-label
 * generator for enum / set fields.
 */
class FieldSpecBuilder<T : Any, V> internal constructor(
    internal val property: KMutableProperty1<T, V>,
) {
    /**
     * Display label for the field. Defaults to the property name split
     * on camel-case boundaries and title-cased — `displayEventTypes`
     * → `Display Event Types`, `URLPath` → `URL Path`.
     */
    var label: String = property.name.humanizeCamelCase()

    /**
     * Override for the selectable item set. Normally derived from the
     * property type (enum's constants, Set<E>'s element constants).
     */
    var items: List<V>? = null

    /**
     * Raw item-label function — receives the widget's *item* type (E for
     * a `KMutableProperty1<T, E>` or `KMutableProperty1<T, Set<E>>`). Set via the
     * typed extension functions below; not meant to be set directly.
     */
    internal var rawItemLabel: ((Any) -> String)? = null

    /**
     * Factory for a custom widget. When set, the renderer skips its
     * type-based dispatch (Checkbox / RadioButtonGroup / …) and uses
     * the factory's output as the field, binding it by property name.
     *
     * Must be a factory — one widget instance can't be reused across
     * multiple form renders, and schemas typically live at file scope.
     *
     * The factory's value type must match the property's: e.g. a
     * `ComboBox<MyEnum>` for a `var mode: MyEnum?` property. Mismatch
     * surfaces as a `Binder` exception at render time.
     *
     * For a no-arg widget class, a constructor reference works:
     * `widget = ::TextArea`. For configured widgets, use a lambda:
     * `widget = { ComboBox<MyEnum>().apply { setItems(MyEnum.entries) } }`.
     */
    var widget: (() -> HasValue<*, *>)? = null

    internal fun build(): FieldSpec<T, V> = FieldSpec(property, label, rawItemLabel, items, widget)
}

/**
 * Set the per-item label for an enum-typed field whose property is
 * non-null (e.g. `var variant: AgentUiVariant`). The lambda receives the
 * enum value directly.
 */
@JvmName("itemLabelNonNull")
fun <T : Any, V : Any> FieldSpecBuilder<T, V>.itemLabel(fn: (V) -> String) {
    @Suppress("UNCHECKED_CAST")
    rawItemLabel = { fn(it as V) }
}

/**
 * Set the per-item label for an enum-typed field whose property is
 * nullable (e.g. `var density: DensityClass?`). The lambda still receives
 * the *non-null* enum element (null isn't a selectable item).
 */
@JvmName("itemLabelNullable")
fun <T : Any, V : Any> FieldSpecBuilder<T, V?>.itemLabel(fn: (V) -> String) {
    @Suppress("UNCHECKED_CAST")
    rawItemLabel = { fn(it as V) }
}

/**
 * Set the per-item label for a [Set]-typed field (renders as a
 * CheckboxGroup). The lambda receives an *element* of the set, not the
 * set itself.
 */
@JvmName("itemLabelSet")
fun <T : Any, E : Any> FieldSpecBuilder<T, Set<E>>.itemLabel(fn: (E) -> String) {
    @Suppress("UNCHECKED_CAST")
    rawItemLabel = { fn(it as E) }
}

/** Builder for a [FormSchema]; see [formSchema] for usage. */
class FormSchemaBuilder<T : Any> @PublishedApi internal constructor(
    @PublishedApi internal val beanClass: Class<T>,
) {
    @PublishedApi
    internal val fields = mutableListOf<FieldSpec<T, *>>()

    /**
     * Add a field bound to [property]. The optional [block] lets you
     * override the label, items, or per-item label.
     */
    fun <V> field(
        property: KMutableProperty1<T, V>,
        block: FieldSpecBuilder<T, V>.() -> Unit = {},
    ) {
        val builder = FieldSpecBuilder(property)
        builder.block()
        fields += builder.build()
    }

    /**
     * Bulk-add fields with default settings. Use cases:
     *
     * - `fields()` — no args — adds *every* public mutable property of
     *   [T]. Order follows the primary-constructor declaration order
     *   (so for a `data class`, the order the developer wrote in source).
     * - `fields(T::a, T::b)` — adds exactly those properties in the
     *   order given.
     *
     * Properties already added via [field] (or a previous [fields] call)
     * are skipped, so this composes cleanly with per-field overrides:
     *
     * ```kotlin
     * formSchema<MyConfig> {
     *     field(MyConfig::variant) { itemLabel { it.displayName } }
     *     fields()  // fills in everything else with defaults
     * }
     * ```
     *
     * The rendered form's field order is whatever order they're added
     * here — so mixing `field(...)` calls with `fields()` lets you put
     * specific properties first and fill the rest after.
     */
    fun fields(vararg props: KMutableProperty1<T, *>) {
        val candidates = if (props.isEmpty()) allPublicMutableProperties() else props.toList()
        addEachUnlessAlreadyPresent(candidates)
    }

    /**
     * Add every public mutable property of [T] *except* the ones listed.
     * Mirror of [fields] for the common "everything but a few internals"
     * shape — e.g. omitting `id`, `createdAt`, audit fields.
     *
     * ```kotlin
     * formSchema<MyConfig> {
     *     exceptFields(MyConfig::id, MyConfig::createdAt)
     * }
     * ```
     *
     * As with [fields], properties already added via [field] or a prior
     * `fields(...)` / `exceptFields(...)` call are skipped.
     */
    fun exceptFields(vararg props: KMutableProperty1<T, *>) {
        val excluded = props.toSet()
        val candidates = allPublicMutableProperties().filterNot { it in excluded }
        addEachUnlessAlreadyPresent(candidates)
    }

    /**
     * Enumerate the bean's public mutable properties in a stable order.
     *
     * `KClass.memberProperties` itself returns properties in an order
     * Kotlin doesn't contractually guarantee, so for a `data class` (or
     * any class with a primary constructor) we order by primary-ctor
     * parameter declaration first, then append any extra body-level
     * `var`s in `memberProperties` order. Net result: the field order
     * matches what the developer reading the source would expect.
     */
    private fun allPublicMutableProperties(): List<KMutableProperty1<T, *>> {
        val all = beanClass.kotlin.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .filterIsInstance<KMutableProperty1<T, *>>()
        val ctor = beanClass.kotlin.primaryConstructor ?: return all
        val byName = all.associateBy { it.name }
        val ordered = ctor.parameters.mapNotNull { p -> p.name?.let { byName[it] } }
        val orderedSet = ordered.toHashSet()
        // Body-level `var`s (not in the primary ctor) get appended at
        // the end, preserving memberProperties' order between them.
        return ordered + all.filterNot { it in orderedSet }
    }

    private fun addEachUnlessAlreadyPresent(candidates: List<KMutableProperty1<T, *>>) {
        val alreadyAdded = fields.mapTo(mutableSetOf()) { it.property }
        candidates.forEach { prop ->
            if (prop !in alreadyAdded) {
                @Suppress("UNCHECKED_CAST")
                field(prop as KMutableProperty1<T, Any?>)
                alreadyAdded += prop
            }
        }
    }

    @PublishedApi
    internal fun build(): FormSchema<T> = FormSchema(beanClass, fields.toList())
}

/**
 * DSL entry point — declare a form schema for bean class [T].
 *
 * The schema is data; render it with [renderForm] inside any
 * Karibu/Vaadin layout. One schema can be reused across views.
 */
inline fun <reified T : Any> formSchema(
    block: FormSchemaBuilder<T>.() -> Unit,
): FormSchema<T> = FormSchemaBuilder(T::class.java).apply(block).build()

/**
 * Split a camelCase / PascalCase identifier on word boundaries and
 * title-case it. Handles ordinary case transitions (`displayName` →
 * `Display Name`) and acronym boundaries (`URLPath` → `URL Path`,
 * `parseHTMLString` → `parse HTML String`). Internal — exposed only
 * for the [FieldSpecBuilder.label] default.
 */
@PublishedApi
internal fun String.humanizeCamelCase(): String =
    replace(Regex("([a-z\\d])([A-Z])"), "$1 $2")
        .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
