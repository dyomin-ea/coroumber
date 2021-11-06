package samples

import kotlinx.coroutines.CoroutineExceptionHandler
import org.junit.jupiter.api.Assertions
import kotlin.coroutines.CoroutineContext

class TestExceptionHandler : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    private var th: Throwable? = null

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        th = exception
    }

    fun assertCalled(t: Throwable) = Assertions.assertEquals(t, th)
    fun assertNotCalled() = Assertions.assertNull(th)
}