package ru.schoolcom.biblio.format

import ru.schoolcom.biblio.model.Book
import ru.schoolcom.biblio.model.ChapterInBook
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Person
import ru.schoolcom.biblio.model.Source
import ru.schoolcom.biblio.model.Thesis
import ru.schoolcom.biblio.model.WebResource

/** Авторы источника (без редакторов). */
internal val Source.authorsList: List<Person>
    get() = when (this) {
        is Book -> authors
        is JournalArticle -> authors
        is ChapterInBook -> authors
        is WebResource -> authors
        is Thesis -> listOf(author)
        is LegalAct -> emptyList()
    }

internal val Source.yearOrNull: Int?
    get() = when (this) {
        is Book -> year
        is JournalArticle -> year
        is ChapterInBook -> year
        is WebResource -> year
        is Thesis -> year
        is LegalAct -> date?.year ?: publicationYear
    }
