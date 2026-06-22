// Sample project with intentional code smells for integration testing

fun main() {
    val name: String? = null
    val forced = name!! // NotNullAssertion

    try {
        risky()
    } catch (e: Exception) {
        // EmptyCatchBlock
    }

    while (true) {
        // TODO: refactor loop
        break
    }
}

fun risky() {
    error("boom")
}

class HugeClass {
    fun m1() {}
    fun m2() {}
    fun m3() {}
    fun m4() {}
    fun m5() {}
    fun m6() {}
    fun m7() {}
    fun m8() {}
    fun m9() {}
    fun m10() {}
    fun m11() {}
    fun m12() {}
    fun m13() {}
    fun m14() {}
    fun m15() {}
    fun m16() {}
    fun m17() {}
    fun m18() {}
    fun m19() {}
    fun m20() {}
    fun m21() {}
}

fun String.publicExtension() = this.length
