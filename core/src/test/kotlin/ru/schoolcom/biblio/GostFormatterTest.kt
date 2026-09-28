package ru.schoolcom.biblio

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.schoolcom.biblio.format.GostFormatter
import ru.schoolcom.biblio.format.Styles
import ru.schoolcom.biblio.model.Book
import ru.schoolcom.biblio.model.ChapterInBook
import ru.schoolcom.biblio.model.JournalArticle
import ru.schoolcom.biblio.model.LegalAct
import ru.schoolcom.biblio.model.Person
import ru.schoolcom.biblio.model.Thesis
import ru.schoolcom.biblio.model.ThesisKind
import ru.schoolcom.biblio.model.WebResource
import java.time.LocalDate

class GostFormatterTest {
    private val gost = GostFormatter(Styles.GOST)
    private val hse = GostFormatter(Styles.HSE)

    private val ivanov = Person("Иванов", "Иван", "Иванович")
    private val petrov = Person("Петров", "П.", "П.")
    private val sidorov = Person("Сидоров", "Сергей")
    private val kuznetsov = Person("Кузнецов", "К.", "К.")
    private val orlov = Person("Орлов", "О.", "О.")

    @Test
    fun bookWithOneAuthor() {
        val book = Book(
            authors = listOf(ivanov),
            title = "Экономическая теория",
            subtitle = "учебник для вузов",
            edition = "2-е изд., перераб. и доп.",
            city = "Москва",
            publisher = "Юрайт",
            year = 2020,
            pages = 250,
            isbn = "978-5-534-00000-0",
        )
        assertEquals(
            "Иванов, И. И. Экономическая теория : учебник для вузов / И. И. Иванов. – " +
                "2-е изд., перераб. и доп. – Москва : Юрайт, 2020. – 250 с. – ISBN 978-5-534-00000-0. – " +
                "Текст : непосредственный.",
            gost.format(book),
        )
        assertEquals(
            "Иванов, И. И. Экономическая теория : учебник для вузов / И. И. Иванов. – " +
                "2-е изд., перераб. и доп. – Москва : Юрайт, 2020. – 250 с. – ISBN 978-5-534-00000-0.",
            hse.format(book),
        )
    }

    @Test
    fun bookWithThreeAuthorsStartsWithFirstAuthor() {
        val book = Book(
            authors = listOf(ivanov, petrov, sidorov),
            title = "Менеджмент",
            city = "Санкт-Петербург",
            publisher = "Питер",
            year = 2019,
            pages = 320,
        )
        assertEquals(
            "Иванов, И. И. Менеджмент / И. И. Иванов, П. П. Петров, С. Сидоров. – " +
                "Санкт-Петербург : Питер, 2019. – 320 с.",
            hse.format(book),
        )
    }

    @Test
    fun bookWithFourAuthorsStartsWithTitle() {
        val book = Book(
            authors = listOf(ivanov, petrov, sidorov, kuznetsov),
            title = "Статистика",
            city = "Москва",
            publisher = "ИНФРА-М",
            year = 2021,
            pages = 400,
        )
        assertEquals(
            "Статистика / И. И. Иванов, П. П. Петров, С. Сидоров, К. К. Кузнецов. – " +
                "Москва : ИНФРА-М, 2021. – 400 с.",
            hse.format(book),
        )
    }

    @Test
    fun bookWithFiveAuthorsUsesEtAl() {
        val book = Book(
            authors = listOf(ivanov, petrov, sidorov, kuznetsov, orlov),
            title = "Статистика",
            editors = listOf(Person("Смирнов", "А.", "А.")),
            city = "Москва",
            publisher = "ИНФРА-М",
            year = 2021,
            pages = 400,
        )
        assertEquals(
            "Статистика / И. И. Иванов, П. П. Петров, С. Сидоров [и др.] ; редактор А. А. Смирнов. – " +
                "Москва : ИНФРА-М, 2021. – 400 с.",
            hse.format(book),
        )
    }

    @Test
    fun englishBook() {
        val book = Book(
            authors = listOf(Person("Smith", "John", "R."), Person("Brown", "Anna")),
            title = "Behavioral Economics",
            edition = "3rd ed.",
            city = "London",
            publisher = "Routledge",
            year = 2018,
            pages = 312,
        )
        assertEquals(
            "Smith, J. R. Behavioral Economics / J. R. Smith, A. Brown. – 3rd ed. – London : Routledge, 2018. – 312 p.",
            hse.format(book),
        )
    }

    @Test
    fun electronicBook() {
        val book = Book(
            authors = listOf(ivanov),
            title = "Право",
            city = "Москва",
            publisher = "Юрайт",
            year = 2023,
            pages = 200,
            url = "https://urait.ru/bcode/123",
            accessDate = LocalDate.of(2026, 9, 1),
        )
        assertEquals(
            "Иванов, И. И. Право / И. И. Иванов. – Москва : Юрайт, 2023. – 200 с. – Текст : электронный. – " +
                "URL: https://urait.ru/bcode/123 (дата обращения: 01.09.2026).",
            gost.format(book),
        )
    }

    @Test
    fun journalArticle() {
        val article = JournalArticle(
            authors = listOf(ivanov, petrov),
            title = "Цифровая экономика",
            journal = "Вопросы экономики",
            year = 2020,
            issue = "5",
            pages = "10-25",
            doi = "10.32609/0042-8736-2020-5-10-25",
        )
        assertEquals(
            "Иванов, И. И. Цифровая экономика / И. И. Иванов, П. П. Петров. – Текст : непосредственный // " +
                "Вопросы экономики. – 2020. – № 5. – С. 10–25. – DOI 10.32609/0042-8736-2020-5-10-25.",
            gost.format(article),
        )
        assertEquals(
            "Иванов, И. И. Цифровая экономика / И. И. Иванов, П. П. Петров // " +
                "Вопросы экономики. – 2020. – № 5. – С. 10–25. – DOI 10.32609/0042-8736-2020-5-10-25.",
            hse.format(article),
        )
    }

    @Test
    fun englishArticleWithVolume() {
        val article = JournalArticle(
            authors = listOf(Person("Smith", "J."), Person("Doe", "A."), Person("Lee", "K."), Person("Chen", "L."), Person("Wu", "M.")),
            title = "Market Design",
            journal = "American Economic Review",
            year = 2019,
            volume = "109",
            issue = "3",
            pages = "45 – 60",
        )
        assertEquals(
            "Market Design / J. Smith, A. Doe, K. Lee [et al.] // American Economic Review. – 2019. – " +
                "Vol. 109, No. 3. – P. 45–60.",
            hse.format(article),
        )
    }

    @Test
    fun chapterInCollection() {
        val chapter = ChapterInBook(
            authors = listOf(sidorov),
            title = "Рынок труда в регионах",
            bookTitle = "Экономика регионов",
            bookSubtitle = "материалы международной научной конференции",
            editors = listOf(Person("Смирнов", "А.", "А.")),
            city = "Москва",
            publisher = "Издательский дом ВШЭ",
            year = 2022,
            pages = "112-120",
        )
        assertEquals(
            "Сидоров, С. Рынок труда в регионах / С. Сидоров // Экономика регионов : " +
                "материалы международной научной конференции / редактор А. А. Смирнов. – " +
                "Москва : Издательский дом ВШЭ, 2022. – С. 112–120.",
            hse.format(chapter),
        )
    }

    @Test
    fun wholeSite() {
        val site = WebResource(
            title = "Федеральная служба государственной статистики",
            city = "Москва",
            year = 1999,
            url = "https://rosstat.gov.ru",
            accessDate = LocalDate.of(2026, 9, 28),
        )
        assertEquals(
            "Федеральная служба государственной статистики : [сайт]. – Москва, 1999. – " +
                "URL: https://rosstat.gov.ru (дата обращения: 28.09.2026). – Текст : электронный.",
            gost.format(site),
        )
    }

    @Test
    fun pageOnSite() {
        val page = WebResource(
            authors = listOf(petrov),
            title = "Инфляция в 2025 году",
            siteName = "Банк России",
            year = 2026,
            url = "https://cbr.ru/page",
            accessDate = LocalDate.of(2026, 9, 5),
        )
        assertEquals(
            "Петров, П. П. Инфляция в 2025 году / П. П. Петров. – Текст : электронный // Банк России : [сайт]. – " +
                "2026. – URL: https://cbr.ru/page (дата обращения: 05.09.2026).",
            gost.format(page),
        )
    }

    @Test
    fun dissertationAbstract() {
        val thesis = Thesis(
            author = ivanov,
            title = "Развитие малого бизнеса в регионе",
            kind = ThesisKind.ABSTRACT,
            specialty = "5.2.3. Региональная и отраслевая экономика",
            degree = "кандидата экономических наук",
            city = "Москва",
            year = 2024,
            pages = 24,
        )
        assertEquals(
            "Иванов, И. И. Развитие малого бизнеса в регионе : специальность 5.2.3. Региональная и отраслевая " +
                "экономика : автореферат диссертации на соискание ученой степени кандидата экономических наук / " +
                "Иванов Иван Иванович. – Москва, 2024. – 24 с.",
            hse.format(thesis),
        )
    }

    @Test
    fun legalActInOfficialPublication() {
        val act = LegalAct(
            title = "Об образовании в Российской Федерации",
            actType = "Федеральный закон",
            date = LocalDate.of(2012, 12, 29),
            number = "273-ФЗ",
            publication = "Собрание законодательства Российской Федерации",
            publicationYear = 2012,
            publicationIssue = "53, ч. 1",
            publicationArticle = "7598",
        )
        assertEquals(
            "Об образовании в Российской Федерации : Федеральный закон от 29.12.2012 № 273-ФЗ // " +
                "Собрание законодательства Российской Федерации. – 2012. – № 53, ч. 1. – Ст. 7598.",
            hse.format(act),
        )
    }

    @Test
    fun legalActInOnlineDatabase() {
        val act = LegalAct(
            title = "Об образовании в Российской Федерации",
            actType = "Федеральный закон",
            date = LocalDate.of(2012, 12, 29),
            number = "273-ФЗ",
            revision = "ред. от 04.08.2023",
            publication = "КонсультантПлюс",
            url = "https://www.consultant.ru/document/cons_doc_LAW_140174/",
            accessDate = LocalDate.of(2026, 9, 1),
        )
        assertEquals(
            "Об образовании в Российской Федерации : Федеральный закон от 29.12.2012 № 273-ФЗ " +
                "(ред. от 04.08.2023). – Текст : электронный // КонсультантПлюс : [сайт]. – " +
                "URL: https://www.consultant.ru/document/cons_doc_LAW_140174/ (дата обращения: 01.09.2026).",
            gost.format(act),
        )
    }

    @Test
    fun footnoteUsesCitedPageAndNoContentType() {
        val book = Book(authors = listOf(ivanov), title = "Право", city = "Москва", publisher = "Юрайт", year = 2023, pages = 200)
        assertEquals(
            "Иванов, И. И. Право / И. И. Иванов. – Москва : Юрайт, 2023. – С. 25.",
            gost.format(book, citedPage = "25", footnote = true),
        )
    }
}
