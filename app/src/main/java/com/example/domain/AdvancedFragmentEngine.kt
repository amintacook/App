package com.example.domain

data class FragmentProfile(
    val name: String,
    val lengthParam: String,
    val intervalMs: String
)

object AdvancedFragmentEngine {
    val profiles = listOf(
        FragmentProfile("Off", "", ""),
        FragmentProfile("Generic GFW", "10-20,10-20,tlshello", "10-20"),
        FragmentProfile("Iran Hamrah Aval", "2-4,2-4,tlshello", "5-10"),
        FragmentProfile("Iran MCI", "1-3,1-3,tlshello", "1-5"),
        FragmentProfile("Iran Irancell", "10-30,10-30,tlshello", "10-50")
    )

    fun buildFragmentString(lengthMin: Int, lengthMax: Int, type: String = "tlshello"): String {
        return "$lengthMin-$lengthMax,$lengthMin-$lengthMax,$type"
    }
}
