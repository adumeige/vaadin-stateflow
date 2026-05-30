package org.antoined.vaadinstateflow.form

import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.responsiveSteps
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.checkbox.CheckboxGroup
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import com.vaadin.flow.data.binder.Binder
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter

/**
 * Render [schema] as a Vaadin `FormLayout` inside the receiver, with all
 * fields auto-bound to [flow] through a [FormBinding]. Every bound-field
 * change auto-commits; the resulting bean is handed to [onUpdate].
 *
 * Widget dispatch (read from each [FieldSpec.property]'s return type):
 *
 * | Property type     | Widget                            |
 * |-------------------|-----------------------------------|
 * | `Boolean`         | `Checkbox`                        |
 * | enum `E` (or `E?`)| `RadioButtonGroup<E>` with entries|
 * | `Set<E>` (enum E) | `CheckboxGroup<E>` with entries   |
 * | `String`          | `TextField`                       |
 * | `Int`             | `IntegerField`                    |
 * | `Double`          | `NumberField`                     |
 *
 * Anything else falls through to `error(...)`. To override per-field —
 * e.g. `ComboBox` for a large enum, `TextArea` for a multi-line string,
 * a fully custom component — set [FieldSpecBuilder.widget] in the
 * schema. The factory runs once per `renderForm` call, so the schema
 * itself can stay file-scoped.
 *
 * @param beanFactory supplies the bean instance written to on each
 *   commit. Defaults to a reflective `flow.value.copy()` — works for any
 *   Kotlin `data class`, which is the common case. Override only for
 *   non-data-class beans or when the snapshot semantics need tweaking.
 */
fun <T : Any> HasComponents.renderForm(
    schema: FormSchema<T>,
    flow: StateFlow<T>,
    onUpdate: (T) -> Unit,
    beanFactory: () -> T = { flow.value.dataClassCopy() },
): FormBinding<T> {
    val binder = BeanValidationBinder(schema.beanClass)
    val hostComponent = this as Component
    val formBinding = binder.bindToStateFlow(
        flow = flow,
        beanFactory = beanFactory,
        component = hostComponent,
        onCommit = onUpdate,
    )
    // Auto-commit on every bound-field change so every tweak propagates
    // immediately, not just when something else triggers commit().
    binder.addValueChangeListener { formBinding.commit() }

    formLayout {
        responsiveSteps { "0"(1, top) }
        schema.fields.forEach { spec ->
            renderField(spec, binder, this)
        }
    }
    return formBinding
}

fun <T : Any> HasComponents.renderForm(
    flow: StateFlow<T>,
    onUpdate: (T) -> Unit,
    beanFactory: () -> T = { flow.value.dataClassCopy() },
    schemaBuilder: () -> FormSchema<T>,
): FormBinding<T> = renderForm(schemaBuilder(), flow, onUpdate, beanFactory)

@Suppress("UNCHECKED_CAST")
private fun <T : Any, V> renderField(
    spec: FieldSpec<T, V>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    // Caller-supplied widget overrides type-based dispatch. The widget
    // is fully owned by the caller (label, items, theme, listeners) —
    // we just bind it by property name and attach it.
    spec.widget?.let { factory ->
        renderCustom(spec, factory.invoke(), binder, layout)
        return
    }

    val propType = spec.property.returnType
    val classifier = propType.classifier as? KClass<*>
    val javaType = classifier?.java

    when {
        javaType == Boolean::class.java || javaType == java.lang.Boolean::class.java ->
            renderBoolean(spec as FieldSpec<T, Boolean>, binder, layout)

        javaType?.isEnum == true ->
            renderEnum(spec as FieldSpec<T, Enum<*>?>, binder, layout, javaType as Class<Enum<*>>)

        classifier == Set::class -> {
            val elementClassifier = propType.arguments.firstOrNull()?.type?.classifier as? KClass<*>
            val elementJava = elementClassifier?.java
            require(elementJava?.isEnum == true) {
                "renderForm: only Set<Enum> is supported for set-typed fields; got $propType for ${spec.property.name}"
            }
            renderEnumSet(
                spec as FieldSpec<T, Set<Enum<*>>>,
                binder,
                layout,
                elementJava as Class<Enum<*>>,
            )
        }

        javaType == String::class.java ->
            renderString(spec as FieldSpec<T, String>, binder, layout)

        javaType == Int::class.java || javaType == Integer::class.java ->
            renderInteger(spec as FieldSpec<T, Int>, binder, layout)

        javaType == Double::class.java || javaType == java.lang.Double::class.java ->
            renderNumber(spec as FieldSpec<T, Double>, binder, layout)

        else -> error(
            "renderForm: unsupported field type $propType for ${spec.property.name}. " +
                    "Supported: Boolean, Enum, Set<Enum>, String, Int, Double."
        )
    }
}

// ---- custom widget passthrough -----------------------------------------

@Suppress("UNCHECKED_CAST")
private fun <T : Any> renderCustom(
    spec: FieldSpec<T, *>,
    widget: HasValue<*, *>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    binder.forField(widget as HasValue<*, Any?>).bind(spec.property.name)
    // All Vaadin form fields are Components; cast to attach to the layout.
    layout.add(widget as Component)
}

// ---- per-type renderers -------------------------------------------------

private fun <T : Any> renderBoolean(
    spec: FieldSpec<T, Boolean>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    val widget = Checkbox(spec.label)
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

private fun <T : Any> renderEnum(
    spec: FieldSpec<T, Enum<*>?>,
    binder: Binder<T>,
    layout: HasComponents,
    enumClass: Class<Enum<*>>,
) {
    val widget = RadioButtonGroup<Enum<*>>().apply {
        label = spec.label
        setItems(*enumClass.enumConstants)
    }
    spec.itemLabel?.let { fn ->
        widget.itemLabelGenerator = ItemLabelGenerator { fn(it) }
    }
    // bind-by-name dodges the variance dance: the karibu
    // `bind(KMutableProperty1<BEAN, out FIELDVALUE?>)` overload can't be
    // satisfied with `Enum<*>` wildcards without a chain of unchecked
    // casts that the compiler still rejects (it routes to `bind(String)`
    // instead). [BeanValidationBinder] already knows `schema.beanClass`,
    // so name-based reflection produces the correct getter/setter.
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

private fun <T : Any> renderEnumSet(
    spec: FieldSpec<T, Set<Enum<*>>>,
    binder: Binder<T>,
    layout: HasComponents,
    enumClass: Class<Enum<*>>,
) {
    val widget = CheckboxGroup<Enum<*>>().apply {
        label = spec.label
        setItems(*enumClass.enumConstants)
    }
    spec.itemLabel?.let { fn ->
        widget.itemLabelGenerator = ItemLabelGenerator { fn(it) }
    }
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

private fun <T : Any> renderString(
    spec: FieldSpec<T, String>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    val widget = TextField(spec.label)
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

private fun <T : Any> renderInteger(
    spec: FieldSpec<T, Int>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    val widget = IntegerField(spec.label)
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

private fun <T : Any> renderNumber(
    spec: FieldSpec<T, Double>,
    binder: Binder<T>,
    layout: HasComponents,
) {
    val widget = NumberField(spec.label)
    binder.forField(widget).bind(spec.property.name)
    layout.add(widget)
}

// ---- data-class .copy() default ----------------------------------------

/**
 * Reflective fallback for `bean.copy()` — Kotlin has no `Copyable<T>`
 * interface and the synthetic `copy()` of a `data class` isn't reachable
 * through the type system. We invoke the no-override `copy()` via
 * `callBy` with only the receiver bound, so every other parameter falls
 * back to its default (the current property value), yielding a fresh
 * instance with the same field values.
 *
 * Throws when called on a non-data class — the call site should supply
 * an explicit `beanFactory` in that case.
 */
@Suppress("UNCHECKED_CAST")
private fun <T : Any> T.dataClassCopy(): T {
    val klass: KClass<out T> = this::class
    require(klass.isData) {
        "renderForm: default beanFactory relies on data-class copy(); " +
                "${klass.qualifiedName ?: klass.simpleName} is not a data class — " +
                "supply an explicit beanFactory."
    }
    val copyFn = klass.functions.first { it.name == "copy" }
    val instance = copyFn.instanceParameter
        ?: error("data-class copy() missing instance parameter on $klass")
    return copyFn.callBy(mapOf(instance to this)) as T
}
