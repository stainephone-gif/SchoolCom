package ru.schoolcom.biblio.model

/**
 * Автор или редактор. Имя и отчество могут быть полными («Иван») или уже
 * инициалами («И.»), форматтер приводит их к виду «И. И.».
 */
data class Person(
    val lastName: String,
    val firstName: String = "",
    val middleName: String = "",
) {
    /** «И. И.», «Ж.-П.», «J. R.»; пустая строка, если имени нет. */
    fun initials(): String =
        listOf(firstName, middleName)
            .filter { it.isNotBlank() }
            .joinToString(" ") { initialOf(it) }

    /** Форма для заголовка записи: «Иванов, И. И.». */
    fun inverted(): String {
        val i = initials()
        return if (i.isEmpty()) lastName.trim() else "${lastName.trim()}, $i"
    }

    /** Форма для сведений об ответственности: «И. И. Иванов». */
    fun direct(): String {
        val i = initials()
        return if (i.isEmpty()) lastName.trim() else "$i ${lastName.trim()}"
    }

    /** Полное имя для диссертаций: «Иванов Иван Иванович». */
    fun fullName(): String =
        listOf(lastName, firstName, middleName).filter { it.isNotBlank() }.joinToString(" ") { it.trim() }

    companion object {
        private fun initialOf(name: String): String =
            name.trim()
                .split('-')
                .filter { it.isNotBlank() }
                .joinToString("-") { part -> part.trim().trimEnd('.').take(1).uppercase() + "." }

        /**
         * Разбирает строку с именем: «Иванов Иван Иванович», «Иванов И. И.»,
         * «Иванов И.И.», «Иванов, И. И.», «Smith, John R.», «И. И. Иванов», «John Smith».
         */
        fun parse(raw: String): Person? {
            val text = raw.trim().replace(Regex("\\s+"), " ")
            if (text.isEmpty()) return null

            if (',' in text) {
                val last = text.substringBefore(',').trim()
                val given = splitGiven(text.substringAfter(',').trim())
                return Person(last, given.getOrElse(0) { "" }, given.drop(1).joinToString(" "))
            }

            val tokens = text.split(' ')
            if (tokens.size == 1) {
                // «Иванов И.И.» без пробела между фамилией и инициалами не бывает,
                // так что одно слово — это фамилия.
                return Person(tokens[0])
            }

            val leadingInitials = tokens.takeWhile { isInitials(it) }
            if (leadingInitials.isNotEmpty() && leadingInitials.size < tokens.size) {
                // «И. И. Иванов»
                val given = leadingInitials.flatMap { splitGiven(it) }
                return Person(
                    tokens.drop(leadingInitials.size).joinToString(" "),
                    given.getOrElse(0) { "" },
                    given.drop(1).joinToString(" "),
                )
            }

            if (tokens.drop(1).all { isInitials(it) }) {
                // «Иванов И. И.» / «Иванов И.И.»
                val given = tokens.drop(1).flatMap { splitGiven(it) }
                return Person(tokens[0], given.getOrElse(0) { "" }, given.drop(1).joinToString(" "))
            }

            val isLatin = text.none { it in 'А'..'я' || it == 'Ё' || it == 'ё' }
            return if (isLatin) {
                // «John Ronald Smith» — фамилия последней
                Person(tokens.last(), tokens[0], tokens.subList(1, tokens.size - 1).joinToString(" "))
            } else {
                // «Иванов Иван Иванович» — фамилия первой
                Person(tokens[0], tokens[1], tokens.drop(2).joinToString(" "))
            }
        }

        private fun isInitials(token: String): Boolean =
            token.matches(Regex("(\\p{Lu}\\.(-\\p{Lu}\\.)?)+"))

        /** «И.И.» → [«И.», «И.»]; «John R.» → [«John», «R.»]. */
        private fun splitGiven(text: String): List<String> =
            text.split(' ')
                .filter { it.isNotBlank() }
                .flatMap { token ->
                    if (isInitials(token)) {
                        Regex("\\p{Lu}\\.(-\\p{Lu}\\.)?").findAll(token).map { it.value }.toList()
                    } else {
                        listOf(token)
                    }
                }
    }
}
