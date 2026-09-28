package ru.schoolcom.biblio.format

/** Способ ссылки в тексте работы (ГОСТ Р 7.0.5-2008). */
enum class InTextMode {
    /** [1, с. 25] */
    NUMBERED,

    /** [Иванов, 2020, с. 25] */
    AUTHOR_YEAR,
}

/**
 * Настройки стиля. ГОСТ и профиль ВШЭ отличаются только значениями этих полей.
 */
data class CitationStyle(
    val id: String,
    val name: String,
    /** Добавлять «Текст : непосредственный» / «Текст : электронный». */
    val contentTypeMarker: Boolean = true,
    /** Если авторов не больше этого числа, перечисляются все. */
    val listAllAuthorsUpTo: Int = 4,
    /** Сколько авторов оставить перед «[и др.]», когда их больше порога. */
    val authorsBeforeEtAl: Int = 3,
    /** Нормативные акты — отдельной группой в начале списка. */
    val legalActsFirst: Boolean = false,
    /** Нумеровать записи в списке литературы. */
    val numbered: Boolean = true,
    val inTextMode: InTextMode = InTextMode.NUMBERED,
)

object Styles {
    val GOST = CitationStyle(
        id = "gost",
        name = "ГОСТ Р 7.0.100-2018",
        contentTypeMarker = true,
        legalActsFirst = false,
    )

    /**
     * Профиль по общим требованиям к ВКР НИУ ВШЭ: записи по ГОСТ Р 7.0.100-2018,
     * нормативные акты в начале, затем источники на кириллице, затем на латинице.
     * Требования образовательных программ различаются — сверять с правилами своей программы.
     */
    val HSE = CitationStyle(
        id = "hse",
        name = "ВКР НИУ ВШЭ",
        contentTypeMarker = false,
        legalActsFirst = true,
    )

    val all = listOf(GOST, HSE)

    fun byId(id: String): CitationStyle = all.firstOrNull { it.id == id } ?: GOST
}
