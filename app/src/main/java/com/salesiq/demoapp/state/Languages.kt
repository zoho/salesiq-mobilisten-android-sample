package com.salesiq.demoapp.state

/** A representative subset of the SDK's supported languages for the pickers. */
data class SupportedLanguage(val code: String, val label: String)

val SUPPORTED_LANGUAGES = listOf(
    SupportedLanguage("en", "English"),
    SupportedLanguage("ar", "Arabic"),
    SupportedLanguage("fr", "French"),
    SupportedLanguage("de", "German"),
    SupportedLanguage("es", "Spanish"),
    SupportedLanguage("ja", "Japanese"),
    SupportedLanguage("zh", "Chinese"),
)

/** Cycles to the next language in the supported list, wrapping around. */
fun nextLanguage(currentLabel: String): SupportedLanguage {
    val index = SUPPORTED_LANGUAGES.indexOfFirst { it.label == currentLabel }
    return SUPPORTED_LANGUAGES[(index + 1) % SUPPORTED_LANGUAGES.size]
}
