package io.github.sgpublic.gtwb.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun getenv(name: String): String? {
    return getenv(name)?.toKString()?.takeIf { it.isNotBlank() }
}