package io.github.sgpublic.gtwb

import io.github.sgpublic.gtwb.utils.GtwbScope
import io.github.sgpublic.gtwb.utils.runBlockingWithCancellation
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import platform.posix.SIGINT
import platform.posix.signal

@OptIn(ExperimentalForeignApi::class)
actual fun runWithCancelable(args: Array<String>) {
    signal(SIGINT, staticCFunction<Int, Unit> {
        runBlocking {
            postExit(e = null)
            GtwbScope.cancel()
        }
    })
    GtwbScope.runBlockingWithCancellation(
        block = {
            realMain(args)
        },
        exit = {
            postExit(e = it)
        }
    )
}