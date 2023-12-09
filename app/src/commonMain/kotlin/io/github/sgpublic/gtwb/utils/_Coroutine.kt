package io.github.sgpublic.gtwb.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

val GtwbScope: CoroutineScope by lazy {
    CoroutineScope(Dispatchers.IO)
}
fun MystereScope(context: CoroutineContext = Dispatchers.IO): CoroutineScope {
    return CoroutineScope(GtwbScope.newCoroutineContext(context) + Job())
}
fun lazyMystereScope(context: CoroutineContext = Dispatchers.IO) = lazy {
    MystereScope(context)
}

fun CoroutineScope.runBlockingWithCancellation(
    block: suspend () -> Unit,
    exit: suspend (Throwable?) -> Unit,
) {
    runBlocking {
        this@runBlockingWithCancellation.launch {
            try {
                block()
            } catch (e: Throwable) {
                exit(e)
                return@launch
            }
            while (true) {
                delay(10_000)
            }
        }.join()
    }
}