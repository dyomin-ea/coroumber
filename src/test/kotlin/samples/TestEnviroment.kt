package samples

import kotlinx.coroutines.CoroutineExceptionHandler

val silentExceptionHandler: CoroutineExceptionHandler
    get() = CoroutineExceptionHandler { _, _ ->
        // ignore
    }