package ru.schoolcom.biblio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.schoolcom.biblio.form.FormResult
import ru.schoolcom.biblio.form.SourceForm
import ru.schoolcom.biblio.form.SourceType
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.Person

class PersonAndFormTest {
    @Test
    fun parsesCommonNameForms() {
        assertEquals(Person("Иванов", "Иван", "Иванович"), Person.parse("Иванов Иван Иванович"))
        assertEquals(Person("Иванов", "И.", "И."), Person.parse("Иванов И.И."))
        assertEquals(Person("Иванов", "И.", "И."), Person.parse("Иванов И. И."))
        assertEquals(Person("Иванов", "И.", "И."), Person.parse("И. И. Иванов"))
        assertEquals(Person("Иванов", "И.", "И."), Person.parse("Иванов, И. И."))
        assertEquals(Person("Smith", "John", "R."), Person.parse("Smith, John R."))
        assertEquals(Person("Smith", "John", ""), Person.parse("John Smith"))
    }

    @Test
    fun initials() {
        assertEquals("Иванов, И. И.", Person("Иванов", "Иван", "Иванович").inverted())
        assertEquals("Ж.-П. Сартр", Person("Сартр", "Жан-Поль").direct())
        assertEquals("Иванов", Person("Иванов").inverted())
    }

    @Test
    fun buildsArticleFromForm() {
        val result = SourceForm.build(
            SourceType.ARTICLE,
            mapOf(
                "authors" to "Иванов И. И.; Петров П. П.",
                "title" to "Цифровая экономика",
                "journal" to "Вопросы экономики",
                "year" to "2020",
                "issue" to "5",
                "pages" to "10-25",
            ),
        )
        assertTrue(result is FormResult.Ok)
        val article = (result as FormResult.Ok).source as JournalArticle
        assertEquals(listOf(Person("Иванов", "И.", "И."), Person("Петров", "П.", "П.")), article.authors)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun reportsErrorsAndWarnings() {
        val invalid = SourceForm.build(SourceType.BOOK, mapOf("year" to "двадцатый"))
        assertTrue(invalid is FormResult.Invalid)
        assertEquals(
            listOf("«Год»: нужно число", "Заполните поле «Заглавие»"),
            (invalid as FormResult.Invalid).errors,
        )

        val warned = SourceForm.build(SourceType.BOOK, mapOf("title" to "Право", "url" to "https://example.org"))
        assertTrue(warned is FormResult.Ok)
        assertTrue((warned as FormResult.Ok).warnings.contains("Для электронного ресурса укажите дату обращения"))
    }

    @Test
    fun formValuesRoundTrip() {
        val article = JournalArticle(
            authors = listOf(Person("Smith", "John", "R."), Person("Иванов", "Иван", "Иванович")),
            title = "Market Design",
            journal = "AER",
            year = 2019,
            pages = "45-60",
            url = "https://example.org",
            accessDate = java.time.LocalDate.of(2026, 9, 1),
        )
        val type = SourceForm.typeOf(article)
        val rebuilt = SourceForm.build(type, SourceForm.values(article))
        assertEquals(article, (rebuilt as FormResult.Ok).source)
    }
}
