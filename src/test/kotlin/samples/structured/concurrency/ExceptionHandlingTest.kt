package samples.structured.concurrency

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import samples.TestException
import samples.TestExceptionHandler
import kotlin.test.assertEquals

class ExceptionHandlingTest {

    @Test
    fun `wrapping with try expect no effect`() {
        Assertions.assertThrows(TestException::class.java) {
            runBlocking {
                try {
                    launch {
                        throw TestException
                    }.join()
                } catch (e: TestException) {
                    Assertions.fail<Nothing>()
                }
            }
        }
    }

    /**
     * Expecting
     * Exception in thread "..." samples.TestException
     */
    @Test
    fun `catch was invoked but exception was propagated to outer scope`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        var th: Throwable? = null
        coroutineScope.launch {
            val deferred = async {
                throw TestException
            }

            try {
                deferred.await()
            } catch (e: TestException) {
                th = e
            }
        }.join()

        assertEquals(TestException, th)
    }

    @Test
    fun `exception was handled by both CEH & catch block`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val outerHandler = TestExceptionHandler()

        var th: Throwable? = null

        coroutineScope.launch(outerHandler) {
            val deferred = async {
                throw TestException
            }

            try {
                deferred.await()
            } catch (e: TestException) {
                th = e
            }
        }.join()

        outerHandler.assertCalled(TestException)
        assertEquals(TestException, th)
    }

    @Test
    fun `top level exception was handled only catch block`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val outerHandler = TestExceptionHandler()

        var th: Throwable? = null

        coroutineScope.launch(outerHandler) {
            val deferred = coroutineScope.async {
                throw TestException
            }

            try {
                deferred.await()
            } catch (e: TestException) {
                th = e
            }
        }.join()

        outerHandler.assertNotCalled()
        assertEquals(TestException, th)
    }

    @Test
    fun `sibling async ignores handler`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val outerHandler = TestExceptionHandler()
        val innerHandler = TestExceptionHandler()

        coroutineScope.launch(outerHandler) {
            val deferred = async(innerHandler) {
                throw TestException
            }

            deferred.await()
        }.join()

        outerHandler.assertCalled(TestException)
        innerHandler.assertNotCalled()
    }

    @Test
    fun `flat async ignores handler`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val outerHandler = TestExceptionHandler()
        val innerHandler = TestExceptionHandler()

        coroutineScope.launch(outerHandler) {
            val deferred = coroutineScope.async(innerHandler) {
                throw TestException
            }

            deferred.await()
        }.join()

        outerHandler.assertCalled(TestException)
        innerHandler.assertNotCalled()
    }

    @Test
    fun `exception are propagated to parent scope`() = runBlocking {
        val coroutineScope = CoroutineScope(SupervisorJob())
        val outerHandler = TestExceptionHandler()
        val innerHandler = TestExceptionHandler()

        coroutineScope.launch(outerHandler) {
            launch(innerHandler) {
                throw TestException
            }.join()
        }.join()

        outerHandler.assertCalled(TestException)
        innerHandler.assertNotCalled()
    }

    @Test
    fun `supervisor job skips exception propagation to parent scope`() = runBlocking {
        val coroutineScope = CoroutineScope(SupervisorJob())
        val outerHandler = TestExceptionHandler()
        val innerHandler = TestExceptionHandler()

        coroutineScope.launch(outerHandler) {
            launch(SupervisorJob() + innerHandler) {
                throw TestException
            }.join()
        }.join()

        outerHandler.assertNotCalled()
        innerHandler.assertCalled(TestException)
    }

    @Test
    fun `async wrapping unit calls throws exceptions and propagates exc to parent scope`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob())
        val handler = TestExceptionHandler()

        var catchCall = false

        fun unitMethod() {
            throw TestException
        }

        scope.launch(handler) {
            val deferred = async { unitMethod() }

            try {
            	deferred.await()
            } catch (e: TestException) {
                catchCall = true
            }
        }.join()

        handler.assertCalled(TestException)
        assert(catchCall)
        assert(scope.isActive)
    }
}