package ru.schoolcom.biblio.model

import java.time.LocalDate

enum class Language { RU, EN }

/** Общие поля всех источников. */
sealed interface Source {
    val title: String

    /** Язык источника; null — определить по заглавию. */
    val language: Language?

    /** Электронная версия: адрес и дата обращения. */
    val url: String?
    val accessDate: LocalDate?

    fun resolvedLanguage(): Language =
        language ?: if (title.any { it in 'А'..'я' || it == 'Ё' || it == 'ё' }) Language.RU else Language.EN
}

/** Книга, монография, учебник. */
data class Book(
    val authors: List<Person> = emptyList(),
    override val title: String,
    val subtitle: String? = null,
    val editors: List<Person> = emptyList(),
    /** «2-е изд., перераб. и доп.», «3rd ed.» */
    val edition: String? = null,
    /** «Т. 2», «Vol. 2» — вводится как есть. */
    val volume: String? = null,
    val city: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val pages: Int? = null,
    val isbn: String? = null,
    override val language: Language? = null,
    override val url: String? = null,
    override val accessDate: LocalDate? = null,
) : Source

/** Статья в журнале. */
data class JournalArticle(
    val authors: List<Person> = emptyList(),
    override val title: String,
    val subtitle: String? = null,
    val journal: String,
    val year: Int? = null,
    val volume: String? = null,
    val issue: String? = null,
    /** «10-25» или «10». */
    val pages: String? = null,
    val doi: String? = null,
    override val language: Language? = null,
    override val url: String? = null,
    override val accessDate: LocalDate? = null,
) : Source

/** Статья в сборнике, глава в коллективной монографии. */
data class ChapterInBook(
    val authors: List<Person> = emptyList(),
    override val title: String,
    val bookTitle: String,
    /** «материалы международной научной конференции», «сборник статей». */
    val bookSubtitle: String? = null,
    val editors: List<Person> = emptyList(),
    val city: String? = null,
    val publisher: String? = null,
    val year: Int? = null,
    val pages: String? = null,
    val doi: String? = null,
    override val language: Language? = null,
    override val url: String? = null,
    override val accessDate: LocalDate? = null,
) : Source

/** Сайт или страница сайта. */
data class WebResource(
    val authors: List<Person> = emptyList(),
    override val title: String,
    /** Название сайта, если описывается страница сайта. */
    val siteName: String? = null,
    val city: String? = null,
    val year: Int? = null,
    override val url: String,
    override val accessDate: LocalDate? = null,
    override val language: Language? = null,
) : Source

enum class ThesisKind { DISSERTATION, ABSTRACT }

/** Диссертация или автореферат диссертации. */
data class Thesis(
    val author: Person,
    override val title: String,
    val kind: ThesisKind = ThesisKind.DISSERTATION,
    /** «5.2.3. Региональная и отраслевая экономика» */
    val specialty: String? = null,
    /** «кандидата экономических наук» */
    val degree: String,
    val institution: String? = null,
    val city: String? = null,
    val year: Int? = null,
    val pages: Int? = null,
    override val language: Language? = Language.RU,
    override val url: String? = null,
    override val accessDate: LocalDate? = null,
) : Source

/** Нормативный правовой акт. */
data class LegalAct(
    override val title: String,
    /** «Федеральный закон», «постановление Правительства Российской Федерации». */
    val actType: String,
    val date: LocalDate? = null,
    val number: String? = null,
    /** «ред. от 04.08.2023» */
    val revision: String? = null,
    /** Официальное издание: «Собрание законодательства Российской Федерации», или «КонсультантПлюс». */
    val publication: String? = null,
    val publicationYear: Int? = null,
    val publicationIssue: String? = null,
    val publicationArticle: String? = null,
    override val language: Language? = Language.RU,
    override val url: String? = null,
    override val accessDate: LocalDate? = null,
) : Source
