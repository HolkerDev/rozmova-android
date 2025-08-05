package eu.rozmova.app.domain

enum class Level(
    val value: String,
) {
    A1("A1"),
    A2("A2"),
    B1("B1"),
    B2("B2"),
    C1("C1"),
    C2("C2"),
    ;

    companion object {
        fun fromValue(value: String): Level? = values().find { it.value == value }
    }
}
