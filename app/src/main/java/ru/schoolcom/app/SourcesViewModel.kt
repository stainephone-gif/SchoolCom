package ru.schoolcom.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ru.schoolcom.biblio.form.FormResult
import ru.schoolcom.biblio.form.SourceForm
import ru.schoolcom.biblio.form.SourceType
import ru.schoolcom.biblio.format.Bibliography
import ru.schoolcom.biblio.format.CitationStyle
import ru.schoolcom.biblio.format.GostFormatter
import ru.schoolcom.biblio.format.InTextCitations
import ru.schoolcom.biblio.format.Ref
import ru.schoolcom.biblio.format.Styles
import ru.schoolcom.biblio.model.Source

/** Одна строка готового списка литературы. */
data class Entry(val source: Source, val text: String, val citation: String)

/**
 * Состояние приложения: список источников, выбранный стиль и форма добавления.
 * Пока хранится в памяти; сохранение списков — этап 2 (см. docs/PLAN.md).
 */
class SourcesViewModel : ViewModel() {
    private val sources = mutableStateListOf<Source>()

    var style by mutableStateOf(Styles.HSE)
        private set

    // --- Форма ---
    var editing by mutableStateOf(false)
        private set
    var formType by mutableStateOf(SourceType.BOOK)
        private set
    val formValues = mutableStateMapOf<String, String>()
    private var editedSource: Source? = null

    fun selectStyle(newStyle: CitationStyle) {
        style = newStyle
    }

    /** Список в порядке, который требует стиль, со ссылкой для текста. */
    fun entries(): List<Entry> {
        val bibliography = Bibliography(style)
        val ordered = bibliography.order(sources)
        val formatter = GostFormatter(style)
        val cite = InTextCitations(style, ordered)
        return ordered.mapIndexed { i, s ->
            val text = formatter.format(s)
            Entry(s, if (style.numbered) "${i + 1}. $text" else text, cite.cite(Ref(s)))
        }
    }

    fun bibliographyText(): String = Bibliography(style).asText(sources)

    fun startAdding() {
        editedSource = null
        formValues.clear()
        editing = true
    }

    fun startEditing(source: Source) {
        editedSource = source
        formType = SourceForm.typeOf(source)
        formValues.clear()
        formValues.putAll(SourceForm.values(source))
        editing = true
    }

    fun cancelEditing() {
        editing = false
    }

    fun selectType(type: SourceType) {
        formType = type
    }

    fun setValue(key: String, value: String) {
        formValues[key] = value
    }

    fun buildForm(): FormResult = SourceForm.build(formType, formValues.toMap())

    fun preview(): String? = (buildForm() as? FormResult.Ok)?.let { GostFormatter(style).format(it.source) }

    /** Сохраняет источник из формы; возвращает ошибки, если сохранить нельзя. */
    fun save(): List<String> {
        return when (val result = buildForm()) {
            is FormResult.Invalid -> result.errors
            is FormResult.Ok -> {
                editedSource?.let { sources.remove(it) }
                sources.add(result.source)
                editing = false
                emptyList()
            }
        }
    }

    fun delete(source: Source) {
        sources.remove(source)
    }
}
