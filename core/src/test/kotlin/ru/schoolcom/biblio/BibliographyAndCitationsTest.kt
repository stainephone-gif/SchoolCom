package ru.schoolcom.biblio

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.schoolcom.biblio.format.Bibliography
import ru.schoolcom.biblio.format.Footnotes
import ru.schoolcom.biblio.format.InTextCitations
import ru.schoolcom.biblio.format.InTextMode
import ru.schoolcom.biblio.format.Ref
import ru.schoolcom.biblio.format.Styles
import ru.schoolcom.biblio.model.Book
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Person
import java.time.LocalDate

class BibliographyAndCitationsTest {
    private val smith = Book(authors = listOf(Person("Smith", "J.")), title = "Economics", city = "London", publisher = "Routledge", year = 2018)
    private val yakovlev = Book(authors = listOf(Person("Яковлев", "Я.")), title = "Финансы", city = "Москва", publisher = "Юрайт", year = 2020)
    private val alekseev = JournalArticle(
        authors = listOf(Person("Алексеев", "А."), Person("Борисов", "Б."), Person("Васильев", "В."), Person("Григорьев", "Г.")),
        title = "Ёмкость рынка",
        journal = "Вопросы экономики",
        year = 2021,
        issue = "1",
        pages = "5-15",
    )
    private val zhukov = Book(authors = listOf(Person("Жуков", "Ж.")), title = "Анализ", city = "Москва", publisher = "Наука", year = 2019)
    private val law = LegalAct(title = "О банках и банковской деятельности", actType = "Федеральный закон", date = LocalDate.of(1990, 12, 2), number = "395-1")
    private val constitution = LegalAct(title = "Конституция Российской Федерации", actType = "принята всенародным голосованием 12.12.1993")

    private val all = listOf(smith, yakovlev, law, alekseev, zhukov, constitution)

    @Test
    fun gostOrderIsCyrillicThenLatinAlphabetically() {
        val order = Bibliography(Styles.GOST).order(all)
        // «Ёмкость рынка» (4 автора — запись под заглавием) сортируется как «Емкость»
        assertEquals(listOf(alekseev, zhukov, constitution, law, yakovlev, smith), order)
    }

    @Test
    fun hseOrderPutsLegalActsFirstByLegalForce() {
        val order = Bibliography(Styles.HSE).order(all)
        assertEquals(listOf(constitution, law, alekseev, zhukov, yakovlev, smith), order)
    }

    @Test
    fun entriesAreNumbered() {
        val entries = Bibliography(Styles.HSE).entries(listOf(smith, yakovlev))
        assertEquals("1. Яковлев, Я. Финансы / Я. Яковлев. – Москва : Юрайт, 2020.", entries[0])
        assertEquals("2. Smith, J. Economics / J. Smith. – London : Routledge, 2018.", entries[1])
    }

    @Test
    fun numberedInTextCitations() {
        val order = Bibliography(Styles.HSE).order(all)
        val cite = InTextCitations(Styles.HSE, order)
        assertEquals("[5, с. 25]", cite.cite(Ref(yakovlev, "25")))
        assertEquals("[6, p. 10–12]", cite.cite(Ref(smith, "10-12")))
        assertEquals("[3; 4]", cite.cite(Ref(alekseev), Ref(zhukov)))
    }

    @Test
    fun authorYearInTextCitations() {
        val style = Styles.GOST.copy(inTextMode = InTextMode.AUTHOR_YEAR)
        val cite = InTextCitations(style, all)
        assertEquals("[Яковлев, 2020, с. 25]", cite.cite(Ref(yakovlev, "25")))
        assertEquals("[Алексеев и др., 2021]", cite.cite(Ref(alekseev)))
        assertEquals("[Smith, 2018, p. 3]", cite.cite(Ref(smith, "3")))
    }

    @Test
    fun footnotesUseIbidAndShortForm() {
        val notes = Footnotes(Styles.HSE)
        assertEquals("Яковлев, Я. Финансы / Я. Яковлев. – Москва : Юрайт, 2020. – С. 25.", notes.next(Ref(yakovlev, "25")))
        assertEquals("Там же. С. 30.", notes.next(Ref(yakovlev, "30")))
        assertEquals("Smith, J. Economics / J. Smith. – London : Routledge, 2018. – P. 7.", notes.next(Ref(smith, "7")))
        assertEquals("Ibid.", notes.next(Ref(smith)))
        assertEquals("Яковлев, Я. Финансы. – С. 40.", notes.next(Ref(yakovlev, "40")))
    }
}
