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
        var dispatcher: CoroutineDispatcher? = null

        scope.launch(NamedDispatcher("scope dispatcher")) {
            dispatcher = coroutineContext[CoroutineDispatcher]
        }.join()

        assertEquals("scope dispatcher", dispatcher.testName)
    }

    @Test
    fun `when wrapped withing inner dispatcher expect its usage`() = runBlocking {
        var dispatcher: CoroutineDispatcher? = null

        scope.launch(NamedDispatcher("scope dispatcher")) {
            dispatcher = withContext(NamedDispatcher("inner dispatcher")) {
                coroutineContext[CoroutineDispatcher]
            }
        }.join()

        assertEquals("inner dispatcher", dispatcher.testName)
    }

    @Test
    fun `when switched to original context expect old dispatcher`() = runBlocking {
        var dispatcher: CoroutineDispatcher? = null
        scope.launch(NamedDispatcher("scope dispatcher")) {
            withContext(NamedDispatcher("inner dispatcher")) {
                coroutineContext[CoroutineDispatcher].testName
            }

            dispatcher = coroutineContext[CoroutineDispatcher]

        }.join()

        assertEquals("scope dispatcher", dispatcher.testName)
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
        var withContextThread: Thread? = null

        scope.launch(Dispatchers.Unconfined) {
            assertSame(startThread, currentThread)
            withContextThread = withContext(NamedDispatcher("inner dispatcher")) {
                currentThread
            }
        }.join()

        assertSame(withContextThread, currentThread)
    }
}

