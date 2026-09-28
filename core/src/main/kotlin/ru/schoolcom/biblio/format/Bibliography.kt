package ru.schoolcom.biblio.format

import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Source

/**
 * Список литературы: порядок записей и нумерация.
 *
 * Порядок: [нормативные акты по юридической силе, если так требует стиль] →
 * источники на кириллице по алфавиту → источники на латинице по алфавиту.
 */
class Bibliography(private val style: CitationStyle) {
    private val formatter = GostFormatter(style)

    fun order(sources: List<Source>): List<Source> =
        sources
            .map { it to formatter.format(it) }
            .sortedWith(
                compareBy<Pair<Source, String>> { (s, _) -> if (style.legalActsFirst && s is LegalAct) 0 else 1 }
                    .thenBy { (s, _) -> if (style.legalActsFirst && s is LegalAct) legalRank(s) else 0 }
                    .thenBy { (s, _) -> if (style.legalActsFirst && s is LegalAct) s.date?.toEpochDay() ?: 0 else 0 }
                    .thenBy { (_, text) -> if (isCyrillic(text)) 0 else 1 }
                    .thenBy { (_, text) -> collationKey(text) },
            )
            .map { it.first }

    /** Готовые записи в нужном порядке, с номерами, если стиль их требует. */
    fun entries(sources: List<Source>): List<String> =
        order(sources).mapIndexed { i, s ->
            val text = formatter.format(s)
            if (style.numbered) "${i + 1}. $text" else text
        }

    fun asText(sources: List<Source>): String = entries(sources).joinToString("\n")

    private fun isCyrillic(text: String): Boolean =
        text.firstOrNull { it.isLetter() }?.let { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.CYRILLIC } ?: false

    private fun collationKey(text: String): String =
        text.dropWhile { !it.isLetterOrDigit() }.lowercase().replace('ё', 'е')

    /** Юридическая сила: Конституция → кодексы и ФКЗ → ФЗ → указы → постановления → прочее. */
    private fun legalRank(act: LegalAct): Int {
        val kind = (act.actType + " " + act.title).lowercase()
        return when {
            "конституция" in kind -> 0
            "кодекс" in kind || "конституционный закон" in kind -> 1
            "федеральный закон" in kind || "закон" in kind -> 2
            "указ" in kind -> 3
            "постановление" in kind || "распоряжение" in kind -> 4
            else -> 5
        }
    }
}
