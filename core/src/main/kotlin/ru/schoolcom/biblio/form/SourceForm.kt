package ru.schoolcom.biblio.form

import ru.schoolcom.biblio.model.Book
import ru.schoolcom.biblio.model.ChapterInBook
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Person
import ru.schoolcom.biblio.model.Source
import ru.schoolcom.biblio.model.Thesis
import ru.schoolcom.biblio.model.ThesisKind
import ru.schoolcom.biblio.model.WebResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class FieldKind { TEXT, PEOPLE, NUMBER, DATE, URL }

/**
 * Поле формы. Экран строит форму по этому описанию, поэтому набор полей
 * для каждого типа источника задаётся здесь, а не в UI.
 */
data class Field(
    val key: String,
    val label: String,
    val kind: FieldKind = FieldKind.TEXT,
    val required: Boolean = false,
    val hint: String = "",
)

enum class SourceType(val title: String, val fields: List<Field>) {
    BOOK(
        "Книга",
        listOf(
            Field("authors", "Авторы", FieldKind.PEOPLE, hint = "Иванов И. И.; Петров П. П."),
            Field("title", "Заглавие", required = true),
            Field("subtitle", "Подзаголовок", hint = "учебник для вузов"),
            Field("editors", "Редакторы", FieldKind.PEOPLE),
            Field("edition", "Издание", hint = "2-е изд., перераб. и доп."),
            Field("volume", "Том", hint = "Т. 2"),
            Field("city", "Город", required = true, hint = "Москва"),
            Field("publisher", "Издательство", required = true),
            Field("year", "Год", FieldKind.NUMBER, required = true),
            Field("pages", "Количество страниц", FieldKind.NUMBER),
            Field("isbn", "ISBN"),
            Field("url", "URL (для электронной книги)", FieldKind.URL),
            Field("accessDate", "Дата обращения", FieldKind.DATE, hint = "дд.мм.гггг"),
        ),
    ),
    ARTICLE(
        "Статья в журнале",
        listOf(
            Field("authors", "Авторы", FieldKind.PEOPLE, hint = "Иванов И. И.; Петров П. П."),
            Field("title", "Заглавие статьи", required = true),
            Field("journal", "Журнал", required = true),
            Field("year", "Год", FieldKind.NUMBER, required = true),
            Field("volume", "Том"),
            Field("issue", "Номер", hint = "5"),
            Field("pages", "Страницы", required = true, hint = "10-25"),
            Field("doi", "DOI"),
            Field("url", "URL", FieldKind.URL),
            Field("accessDate", "Дата обращения", FieldKind.DATE, hint = "дд.мм.гггг"),
        ),
    ),
    CHAPTER(
        "Статья в сборнике / глава",
        listOf(
            Field("authors", "Авторы", FieldKind.PEOPLE),
            Field("title", "Заглавие статьи", required = true),
            Field("bookTitle", "Название сборника", required = true),
            Field("bookSubtitle", "Сведения о сборнике", hint = "материалы международной конференции"),
            Field("editors", "Редакторы сборника", FieldKind.PEOPLE),
            Field("city", "Город", required = true),
            Field("publisher", "Издательство", required = true),
            Field("year", "Год", FieldKind.NUMBER, required = true),
            Field("pages", "Страницы", required = true, hint = "10-25"),
            Field("doi", "DOI"),
            Field("url", "URL", FieldKind.URL),
            Field("accessDate", "Дата обращения", FieldKind.DATE, hint = "дд.мм.гггг"),
        ),
    ),
    WEB(
        "Сайт / страница сайта",
        listOf(
            Field("authors", "Авторы", FieldKind.PEOPLE),
            Field("title", "Заглавие страницы или сайта", required = true),
            Field("siteName", "Название сайта (если описываете страницу)"),
            Field("city", "Город"),
            Field("year", "Год", FieldKind.NUMBER),
            Field("url", "URL", FieldKind.URL, required = true),
            Field("accessDate", "Дата обращения", FieldKind.DATE, required = true, hint = "дд.мм.гггг"),
        ),
    ),
    THESIS(
        "Диссертация / автореферат",
        listOf(
            Field("author", "Автор (полностью)", FieldKind.PEOPLE, required = true, hint = "Иванов Иван Иванович"),
            Field("title", "Тема", required = true),
            Field("kind", "Вид", hint = "диссертация или автореферат"),
            Field("specialty", "Специальность", hint = "5.2.3. Региональная и отраслевая экономика"),
            Field("degree", "Степень", required = true, hint = "кандидата экономических наук"),
            Field("institution", "Организация"),
            Field("city", "Город", required = true),
            Field("year", "Год", FieldKind.NUMBER, required = true),
            Field("pages", "Количество страниц", FieldKind.NUMBER),
        ),
    ),
    LEGAL(
        "Нормативный акт",
        listOf(
            Field("title", "Название", required = true, hint = "Об образовании в Российской Федерации"),
            Field("actType", "Вид акта", required = true, hint = "Федеральный закон"),
            Field("date", "Дата принятия", FieldKind.DATE, hint = "дд.мм.гггг"),
            Field("number", "Номер", hint = "273-ФЗ"),
            Field("revision", "Редакция", hint = "ред. от 04.08.2023"),
            Field("publication", "Где опубликован", hint = "Собрание законодательства Российской Федерации"),
            Field("publicationYear", "Год выпуска", FieldKind.NUMBER),
            Field("publicationIssue", "Номер выпуска", hint = "53, ч. 1"),
            Field("publicationArticle", "Статья", hint = "7598"),
            Field("url", "URL", FieldKind.URL),
            Field("accessDate", "Дата обращения", FieldKind.DATE, hint = "дд.мм.гггг"),
        ),
    ),
}

/** Результат сборки источника из формы. */
sealed interface FormResult {
    data class Ok(val source: Source, val warnings: List<String>) : FormResult
    data class Invalid(val errors: List<String>) : FormResult
}

object SourceForm {
    private val DATE = DateTimeFormatter.ofPattern("d.M.yyyy")

    /** Собирает источник из значений полей (ключи — [Field.key]). */
    fun build(type: SourceType, values: Map<String, String>): FormResult {
        val v = values.mapValues { it.value.trim() }.filterValues { it.isNotEmpty() }
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        for (f in type.fields) {
            val value = v[f.key]
            if (value == null) {
                if (f.required) warnings += "Не заполнено поле «${f.label}»"
                continue
            }
            when (f.kind) {
                FieldKind.NUMBER -> if (value.toIntOrNull() == null) errors += "«${f.label}»: нужно число"
                FieldKind.DATE -> if (parseDate(value) == null) errors += "«${f.label}»: дата в формате дд.мм.гггг"
                else -> {}
            }
        }
        if (v["url"] != null && v["accessDate"] == null && type != SourceType.WEB) {
            warnings += "Для электронного ресурса укажите дату обращения"
        }
        val titleField = type.fields.first { it.key == "title" }
        if (v["title"] == null) errors += "Заполните поле «${titleField.label}»"
        if (type == SourceType.WEB && v["url"] == null) errors += "Заполните поле «URL»"
        if (type == SourceType.ARTICLE && v["journal"] == null) errors += "Заполните поле «Журнал»"
        if (type == SourceType.CHAPTER && v["bookTitle"] == null) errors += "Заполните поле «Название сборника»"
        if (type == SourceType.THESIS && people(v["author"]).isEmpty()) errors += "Заполните поле «Автор»"
        if (type == SourceType.THESIS && v["degree"] == null) errors += "Заполните поле «Степень»"
        if (type == SourceType.LEGAL && v["actType"] == null) errors += "Заполните поле «Вид акта»"
        if (errors.isNotEmpty()) return FormResult.Invalid(errors)

        val source: Source = when (type) {
            SourceType.BOOK -> Book(
                authors = people(v["authors"]),
                title = v.getValue("title"),
                subtitle = v["subtitle"],
                editors = people(v["editors"]),
                edition = v["edition"],
                volume = v["volume"],
                city = v["city"],
                publisher = v["publisher"],
                year = v["year"]?.toInt(),
                pages = v["pages"]?.toInt(),
                isbn = v["isbn"],
                url = v["url"],
                accessDate = v["accessDate"]?.let(::parseDate),
            )
            SourceType.ARTICLE -> JournalArticle(
                authors = people(v["authors"]),
                title = v.getValue("title"),
                journal = v.getValue("journal"),
                year = v["year"]?.toInt(),
                volume = v["volume"],
                issue = v["issue"],
                pages = v["pages"],
                doi = v["doi"],
                url = v["url"],
                accessDate = v["accessDate"]?.let(::parseDate),
            )
            SourceType.CHAPTER -> ChapterInBook(
                authors = people(v["authors"]),
                title = v.getValue("title"),
                bookTitle = v.getValue("bookTitle"),
                bookSubtitle = v["bookSubtitle"],
                editors = people(v["editors"]),
                city = v["city"],
                publisher = v["publisher"],
                year = v["year"]?.toInt(),
                pages = v["pages"],
                doi = v["doi"],
                url = v["url"],
                accessDate = v["accessDate"]?.let(::parseDate),
            )
            SourceType.WEB -> WebResource(
                authors = people(v["authors"]),
                title = v.getValue("title"),
                siteName = v["siteName"],
                city = v["city"],
                year = v["year"]?.toInt(),
                url = v.getValue("url"),
                accessDate = v["accessDate"]?.let(::parseDate),
            )
            SourceType.THESIS -> Thesis(
                author = people(v["author"]).first(),
                title = v.getValue("title"),
                kind = if (v["kind"]?.lowercase()?.startsWith("автореф") == true) ThesisKind.ABSTRACT else ThesisKind.DISSERTATION,
                specialty = v["specialty"],
                degree = v.getValue("degree"),
                institution = v["institution"],
                city = v["city"],
                year = v["year"]?.toInt(),
                pages = v["pages"]?.toInt(),
            )
            SourceType.LEGAL -> LegalAct(
                title = v.getValue("title"),
                actType = v.getValue("actType"),
                date = v["date"]?.let(::parseDate),
                number = v["number"],
                revision = v["revision"],
                publication = v["publication"],
                publicationYear = v["publicationYear"]?.toInt(),
                publicationIssue = v["publicationIssue"],
                publicationArticle = v["publicationArticle"],
                url = v["url"],
                accessDate = v["accessDate"]?.let(::parseDate),
            )
        }
        return FormResult.Ok(source, warnings)
    }

    fun typeOf(source: Source): SourceType = when (source) {
        is Book -> SourceType.BOOK
        is JournalArticle -> SourceType.ARTICLE
        is ChapterInBook -> SourceType.CHAPTER
        is WebResource -> SourceType.WEB
        is Thesis -> SourceType.THESIS
        is LegalAct -> SourceType.LEGAL
    }

    /** Обратное к [build]: значения полей формы для редактирования источника. */
    fun values(source: Source): Map<String, String> {
        val pairs: List<Pair<String, Any?>> = when (source) {
            is Book -> listOf(
                "authors" to source.authors, "title" to source.title, "subtitle" to source.subtitle,
                "editors" to source.editors, "edition" to source.edition, "volume" to source.volume,
                "city" to source.city, "publisher" to source.publisher, "year" to source.year,
                "pages" to source.pages, "isbn" to source.isbn, "url" to source.url, "accessDate" to source.accessDate,
            )
            is JournalArticle -> listOf(
                "authors" to source.authors, "title" to source.title, "journal" to source.journal,
                "year" to source.year, "volume" to source.volume, "issue" to source.issue, "pages" to source.pages,
                "doi" to source.doi, "url" to source.url, "accessDate" to source.accessDate,
            )
            is ChapterInBook -> listOf(
                "authors" to source.authors, "title" to source.title, "bookTitle" to source.bookTitle,
                "bookSubtitle" to source.bookSubtitle, "editors" to source.editors, "city" to source.city,
                "publisher" to source.publisher, "year" to source.year, "pages" to source.pages,
                "doi" to source.doi, "url" to source.url, "accessDate" to source.accessDate,
            )
            is WebResource -> listOf(
                "authors" to source.authors, "title" to source.title, "siteName" to source.siteName,
                "city" to source.city, "year" to source.year, "url" to source.url, "accessDate" to source.accessDate,
            )
            is Thesis -> listOf(
                "author" to listOf(source.author), "title" to source.title,
                "kind" to if (source.kind == ThesisKind.ABSTRACT) "автореферат" else "диссертация",
                "specialty" to source.specialty, "degree" to source.degree, "institution" to source.institution,
                "city" to source.city, "year" to source.year, "pages" to source.pages,
            )
            is LegalAct -> listOf(
                "title" to source.title, "actType" to source.actType, "date" to source.date,
                "number" to source.number, "revision" to source.revision, "publication" to source.publication,
                "publicationYear" to source.publicationYear, "publicationIssue" to source.publicationIssue,
                "publicationArticle" to source.publicationArticle, "url" to source.url, "accessDate" to source.accessDate,
            )
        }
        return pairs.mapNotNull { (key, value) ->
            val text = when (value) {
                null -> null
                is List<*> -> value.filterIsInstance<Person>().joinToString("; ") { editable(it) }.ifEmpty { null }
                is LocalDate -> value.format(OUT_DATE)
                else -> value.toString()
            }
            text?.let { key to it }
        }.toMap()
    }

    private val OUT_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /** «Иванов, Иван Иванович», «Smith, John R.» — запятая делает разбор однозначным. */
    private fun editable(p: Person): String {
        val given = listOf(p.firstName, p.middleName).filter { it.isNotBlank() }.joinToString(" ")
        return if (given.isEmpty()) p.lastName else "${p.lastName}, $given"
    }

    /** Список людей через «;» или с новой строки: «Иванов И. И.; Петров П. П.». */
    fun people(text: String?): List<Person> =
        text.orEmpty().split(';', '\n').mapNotNull { Person.parse(it) }

    fun parseDate(text: String): LocalDate? =
        try {
            LocalDate.parse(text.trim(), DATE)
        } catch (e: DateTimeParseException) {
            null
        }
}
