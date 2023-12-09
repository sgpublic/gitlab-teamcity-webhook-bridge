package io.github.sgpublic.gtwb

import io.github.sgpublic.gtwb.utils.GtwbScope
import io.github.sgpublic.gtwb.utils.runBlockingWithCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

actual fun runWithCancelable(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(Thread {
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