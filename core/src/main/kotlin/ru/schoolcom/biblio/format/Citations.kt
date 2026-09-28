package ru.schoolcom.biblio.format

import ru.schoolcom.biblio.model.Language
import ru.schoolcom.biblio.model.Source

/** Ссылка на источник, при необходимости — на конкретную страницу. */
data class Ref(val source: Source, val page: String? = null)

/**
 * Затекстовые ссылки по ГОСТ Р 7.0.5-2008: «[1, с. 25]», «[1; 3]»,
 * «[Иванов, 2020, с. 25]», «[Smith et al., 2019, p. 12]».
 *
 * @param bibliography источники в том порядке, в котором они стоят в списке литературы
 *   (см. [Bibliography.order]) — по нему определяются номера.
 */
class InTextCitations(private val style: CitationStyle, private val bibliography: List<Source>) {

    fun cite(vararg refs: Ref): String = cite(refs.toList())

    fun cite(refs: List<Ref>): String =
        refs.joinToString("; ", prefix = "[", postfix = "]") { ref ->
            val label = when (style.inTextMode) {
                InTextMode.NUMBERED -> number(ref.source).toString()
                InTextMode.AUTHOR_YEAR -> authorYear(ref.source)
            }
            if (ref.page != null) "$label, ${pageLabel(ref.source)} ${GostFormatter.dashes(ref.page)}" else label
        }

    private fun number(source: Source): Int {
        val index = bibliography.indexOf(source)
        require(index >= 0) { "Источника нет в списке литературы: ${source.title}" }
        return index + 1
    }

    private fun authorYear(source: Source): String {
        val en = source.resolvedLanguage() == Language.EN
        val names = source.authorsList.map { it.lastName.trim() }
        val who = when {
            names.isEmpty() -> source.title.split(' ').take(3).joinToString(" ") +
                if (source.title.split(' ').size > 3) "…" else ""
            names.size <= 3 -> names.joinToString(", ")
            else -> names.first() + if (en) " et al." else " и др."
        }
        return listOfNotNull(who, source.yearOrNull?.toString()).joinToString(", ")
    }

    private fun pageLabel(source: Source) = if (source.resolvedLanguage() == Language.EN) "p." else "с."
}

/**
 * Подстрочные ссылки (сноски) по ГОСТ Р 7.0.5-2008. Экземпляр помнит предыдущие
 * сноски, поэтому их нужно запрашивать по порядку следования в тексте:
 * первая ссылка — полная запись, та же сразу следом — «Там же» / «Ibid.»,
 * повторная через другие — сокращённая запись.
 */
class Footnotes(style: CitationStyle) {
    private val formatter = GostFormatter(style)
    private val seen = mutableSetOf<Source>()
    private var previous: Source? = null

    fun next(ref: Ref): String {
        val source = ref.source
        val en = source.resolvedLanguage() == Language.EN
        val text = when {
            source == previous -> {
                val same = if (en) "Ibid." else "Там же."
                if (ref.page != null) "$same ${if (en) "P." else "С."} ${GostFormatter.dashes(ref.page)}." else same
            }
            source in seen -> formatter.shortForm(source, ref.page)
            else -> formatter.format(source, citedPage = ref.page, footnote = true)
        }
        seen += source
        previous = source
        return text
    }
}
