package io.github.sgpublic.gtwb.utils

actual fun getenv(name: String): String? {
    return System.getenv(name)?.takeIf { it.isNotBlank() }
}