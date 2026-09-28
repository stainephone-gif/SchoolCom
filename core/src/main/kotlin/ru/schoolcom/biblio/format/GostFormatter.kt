package ru.schoolcom.biblio.format

import ru.schoolcom.biblio.model.Book
import ru.schoolcom.biblio.model.ChapterInBook
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.Language
import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Person
import ru.schoolcom.biblio.model.Source
import ru.schoolcom.biblio.model.Thesis
import ru.schoolcom.biblio.model.ThesisKind
import ru.schoolcom.biblio.model.WebResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Библиографическая запись по ГОСТ Р 7.0.100-2018.
 *
 * Области записи соединяются разделителем «. – »; внутри области используются
 * « : » (сведения, относящиеся к заглавию; издательство), « / » (ответственность),
 * « ; » (последующие сведения об ответственности), « // » (сведения об издании,
 * в котором помещена составная часть).
 */
class GostFormatter(private val style: CitationStyle) {

    /**
     * @param citedPage страница, на которую ссылаются (для подстрочной ссылки);
     *   заменяет объём книги или диапазон страниц статьи.
     * @param footnote запись для подстрочной ссылки: без «Текст : …».
     */
    fun format(source: Source, citedPage: String? = null, footnote: Boolean = false): String {
        val t = Terms.of(source.resolvedLanguage())
        val marker = style.contentTypeMarker && !footnote
        return when (source) {
            is Book -> book(source, t, marker, citedPage)
            is JournalArticle -> article(source, t, marker, citedPage)
            is ChapterInBook -> chapter(source, t, marker, citedPage)
            is WebResource -> web(source, t, marker)
            is Thesis -> thesis(source, t, marker, citedPage)
            is LegalAct -> legalAct(source, t, marker)
        }
    }

    /**
     * Сокращённая запись для повторной подстрочной ссылки (ГОСТ Р 7.0.5-2008):
     * «Иванов, И. И. Название. – С. 25.»
     */
    fun shortForm(source: Source, citedPage: String? = null): String {
        val t = Terms.of(source.resolvedLanguage())
        val authors = source.authorsList
        val heading = if (authors.size in 1..3) authors.first().inverted() else null
        return areas(withHeading(heading, source.title.trim()), citedPage?.let { "${t.page} ${dashes(it)}" })
    }

    private fun book(b: Book, t: Terms, marker: Boolean, citedPage: String?): String = areas(
        titleArea(b.authors, b.title, b.subtitle, b.editors, t),
        b.edition,
        b.volume,
        publication(b.city, b.publisher, b.year),
        if (citedPage != null) "${t.page} ${dashes(citedPage)}" else b.pages?.let { "$it ${t.pagesTotal}" },
        b.isbn?.let { "ISBN $it" },
        contentType(marker, electronic = b.url != null),
        urlArea(b.url, b.accessDate),
    )

    private fun article(a: JournalArticle, t: Terms, marker: Boolean, citedPage: String?): String {
        val host = areas(
            a.journal,
            a.year?.toString(),
            listOfNotNull(a.volume?.let { "${t.volume} $it" }, a.issue?.let { "${t.issue} $it" })
                .joinToString(", ").ifEmpty { null },
            (citedPage ?: a.pages)?.let { "${t.page} ${dashes(it)}" },
            a.doi?.let { "DOI $it" },
            urlArea(a.url, a.accessDate),
        )
        return analytic(titleArea(a.authors, a.title, a.subtitle, emptyList(), t), marker, a.url != null, host)
    }

    private fun chapter(c: ChapterInBook, t: Terms, marker: Boolean, citedPage: String?): String {
        val hostTitle = buildString {
            append(c.bookTitle)
            c.bookSubtitle?.let { append(" : ").append(it) }
            editorsStatement(c.editors, t)?.let { append(" / ").append(it) }
        }
        val host = areas(
            hostTitle,
            publication(c.city, c.publisher, c.year),
            (citedPage ?: c.pages)?.let { "${t.page} ${dashes(it)}" },
            c.doi?.let { "DOI $it" },
            urlArea(c.url, c.accessDate),
        )
        return analytic(titleArea(c.authors, c.title, null, emptyList(), t), marker, c.url != null, host)
    }

    private fun web(w: WebResource, t: Terms, marker: Boolean): String {
        if (w.siteName == null && w.authors.isEmpty()) {
            // Сайт целиком: «Название : [сайт]. – Москва, 2020. – URL: … – Текст : электронный.»
            return areas(
                "${w.title} : [сайт]",
                publication(w.city, null, w.year),
                urlArea(w.url, w.accessDate),
                contentType(marker, electronic = true),
            )
        }
        // Страница сайта: «Заглавие / Автор. – Текст : электронный // Сайт : [сайт]. – 2020. – URL: …»
        val host = areas(
            w.siteName?.let { "$it : [сайт]" },
            publication(w.city, null, w.year),
            urlArea(w.url, w.accessDate),
        )
        return analytic(titleArea(w.authors, w.title, null, emptyList(), t), marker, electronic = true, host = host)
    }

    private fun thesis(d: Thesis, t: Terms, marker: Boolean, citedPage: String?): String {
        val work = when (d.kind) {
            ThesisKind.DISSERTATION -> "диссертация на соискание ученой степени ${d.degree}"
            ThesisKind.ABSTRACT -> "автореферат диссертации на соискание ученой степени ${d.degree}"
        }
        val titlePart = buildString {
            append(withHeading(d.author.inverted(), d.title))
            d.specialty?.let { append(" : специальность ").append(it) }
            append(" : ").append(work)
            append(" / ").append(d.author.fullName())
            d.institution?.let { append(" ; ").append(it) }
        }
        return areas(
            titlePart,
            publication(d.city, null, d.year),
            if (citedPage != null) "${t.page} ${dashes(citedPage)}" else d.pages?.let { "$it ${t.pagesTotal}" },
            contentType(marker, electronic = d.url != null),
            urlArea(d.url, d.accessDate),
        )
    }

    private fun legalAct(l: LegalAct, t: Terms, marker: Boolean): String {
        val titlePart = buildString {
            append(l.title).append(" : ").append(l.actType)
            l.date?.let { append(" от ").append(date(it)) }
            l.number?.let { append(" № ").append(it) }
            l.revision?.let { append(" (").append(it).append(")") }
        }
        if (l.publication == null) {
            return areas(titlePart, contentType(marker, electronic = l.url != null), urlArea(l.url, l.accessDate))
        }
        val isOnlineDatabase = l.publicationYear == null && l.publicationIssue == null && l.publicationArticle == null
        val host = areas(
            if (isOnlineDatabase) "${l.publication} : [сайт]" else l.publication,
            l.publicationYear?.toString(),
            l.publicationIssue?.let { "${t.issue} $it" },
            l.publicationArticle?.let { "Ст. $it" },
            urlArea(l.url, l.accessDate),
        )
        return analytic(titlePart, marker, l.url != null, host)
    }

    // --- Составные части записи ---

    /**
     * Заголовок (первый автор) и область заглавия. При 1–3 авторах запись
     * начинается с фамилии первого автора, при 4 и более — с заглавия.
     */
    internal fun titleArea(
        authors: List<Person>,
        title: String,
        subtitle: String?,
        editors: List<Person>,
        t: Terms,
    ): String {
        val heading = if (authors.size in 1..3) authors.first().inverted() else null
        val responsibility = listOfNotNull(authorsStatement(authors, t), editorsStatement(editors, t))
        return buildString {
            append(withHeading(heading, title.trim()))
            subtitle?.takeIf { it.isNotBlank() }?.let { append(" : ").append(it.trim()) }
            if (responsibility.isNotEmpty()) append(" / ").append(responsibility.joinToString(" ; "))
        }
    }

    private fun authorsStatement(authors: List<Person>, t: Terms): String? {
        if (authors.isEmpty()) return null
        val listed = if (authors.size <= style.listAllAuthorsUpTo) authors else authors.take(style.authorsBeforeEtAl)
        val names = listed.joinToString(", ") { it.direct() }
        return if (listed.size < authors.size) "$names ${t.etAl}" else names
    }

    private fun editorsStatement(editors: List<Person>, t: Terms): String? {
        if (editors.isEmpty()) return null
        val word = if (editors.size == 1) t.editor else t.editors
        return "$word ${editors.joinToString(", ") { it.direct() }}"
    }

    /** Составная часть: «Заглавие / Автор. – Текст : электронный // Издание. – 2020. – С. 1–10.» */
    private fun analytic(titlePart: String, marker: Boolean, electronic: Boolean, host: String): String {
        val part = contentType(marker, electronic)?.let { joinAreas(titlePart, it) } ?: titlePart
        return finish("$part // ${host.removeSuffix(".")}")
    }

    private fun publication(city: String?, publisher: String?, year: Int?): String? {
        val place = listOfNotNull(city?.trim()?.ifEmpty { null }, publisher?.trim()?.ifEmpty { null })
            .joinToString(" : ")
        return listOfNotNull(place.ifEmpty { null }, year?.toString()).joinToString(", ").ifEmpty { null }
    }

    private fun contentType(marker: Boolean, electronic: Boolean): String? = when {
        !marker -> null
        electronic -> "Текст : электронный"
        else -> "Текст : непосредственный"
    }

    private fun urlArea(url: String?, accessDate: LocalDate?): String? {
        if (url.isNullOrBlank()) return null
        return if (accessDate != null) "URL: ${url.trim()} (дата обращения: ${date(accessDate)})" else "URL: ${url.trim()}"
    }

    private fun withHeading(heading: String?, title: String): String = when {
        heading == null -> title
        heading.endsWith(".") -> "$heading $title"
        else -> "$heading. $title"
    }

    /** Соединяет непустые области разделителем «. – » и ставит точку в конце. */
    private fun areas(vararg parts: String?): String =
        finish(parts.filterNot { it.isNullOrBlank() }.map { it!!.trim() }.reduceOrNull(::joinAreas) ?: "")

    private fun joinAreas(left: String, right: String): String =
        if (left.endsWith(".")) "$left – $right" else "$left. – $right"

    private fun finish(text: String): String = if (text.isEmpty() || text.endsWith(".")) text else "$text."

    internal class Terms(
        val etAl: String,
        val pagesTotal: String,
        val page: String,
        val volume: String,
        val issue: String,
        val editor: String,
        val editors: String,
    ) {
        companion object {
            private val RU = Terms("[и др.]", "с.", "С.", "Т.", "№", "редактор", "редакторы")
            private val EN = Terms("[et al.]", "p.", "P.", "Vol.", "No.", "ed. by", "ed. by")

            fun of(language: Language) = if (language == Language.RU) RU else EN
        }
    }

    companion object {
        private val DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        fun date(d: LocalDate): String = d.format(DATE)

        /** «10-25», «10 — 25» → «10–25». */
        fun dashes(pages: String): String = pages.trim().replace(Regex("\\s*[-‐‑‒–—−]\\s*"), "–")
    }
}
