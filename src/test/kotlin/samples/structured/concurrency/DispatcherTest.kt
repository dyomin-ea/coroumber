package samples.structured.concurrency

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import samples.NamedDispatcher

@OptIn(ExperimentalStdlibApi::class)
class DispatcherTest {

    private val CoroutineDispatcher?.testName: String
        get() {
            if (this == null) {
                return "null"
            }

            if (this is NamedDispatcher) {
                return name
            }

            return this::class.java.simpleName
        }

    private val scope: CoroutineScope
        get() = CoroutineScope(Job())

    private val currentThread
        get() = Thread.currentThread()

    @Test
    fun `when launching within dispatcher expect its usage`() = runBlocking {
        scope.launch(NamedDispatcher("scope dispatcher")) {
            assertEquals(
                "scope dispatcher",
                coroutineContext[CoroutineDispatcher].testName
            )
        }.join()
    }

    @Test
    fun `when wrapped withing inner dispatcher expect its usage`() = runBlocking {
        scope.launch(NamedDispatcher("scope dispatcher")) {
            val actual = withContext(NamedDispatcher("inner dispatcher")) {
                coroutineContext[CoroutineDispatcher].testName
            }
            assertEquals(
                "inner dispatcher",
                actual
            )
        }.join()
    }

    @Test
    fun `when switched to original context expect old dispatcher`() = runBlocking {
        scope.launch(NamedDispatcher("scope dispatcher")) {
            withContext(NamedDispatcher("inner dispatcher")) {
                coroutineContext[CoroutineDispatcher].testName
            }

            assertEquals(
                "scope dispatcher",
                coroutineContext[CoroutineDispatcher].testName
            )
        }.join()
    }

    @Test
    fun `switching unconfined dispatcher expect same name`() = runBlocking {
        scope.launch(Dispatchers.Unconfined) {
            assertEquals("Unconfined", coroutineContext[CoroutineDispatcher].testName)
            withContext(NamedDispatcher("inner dispatcher")) {
                coroutineContext[CoroutineDispatcher].testName
            }
            assertEquals("Unconfined", coroutineContext[CoroutineDispatcher].testName)
        }.join()
    }

    @Test
    fun `switching unconfined dispatcher various execution threads`() = runBlocking {
        val startThread = Thread.currentThread()
        scope.launch(Dispatchers.Unconfined) {
            assertSame(startThread, currentThread)
            val withContextThread = withContext(NamedDispatcher("inner dispatcher")) {
                currentThread
            }
            assertSame(withContextThread, currentThread)
        }.join()
    }
}

