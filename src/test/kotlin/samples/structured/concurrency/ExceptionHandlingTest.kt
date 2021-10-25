package samples.structured.concurrency

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import samples.TestException

@OptIn(ExperimentalCoroutinesApi::class)
class ExceptionHandlingTest {
    // expect fail with TestException
    // gonna catch em all
    @Test
    fun `wrapping with try expect no effect`() = runBlocking {
        try {
            launch {
                throw TestException
            }.join()
        } catch (e: TestException) {
            //ignore
        }
    }

    @Test
    fun `sample 0`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        coroutineScope.launch {
            val deferred = async {
                throw TestException
            }

            try {
                deferred.await()
            } catch (e: TestException) {
                println(1)
            }
        }.join()
    }
    /*
    * Exception in thread "..." samples.TestException
    * */

    @Test
    fun `sample 1`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val handler = CoroutineExceptionHandler { _, _ -> println(1) }
        coroutineScope.launch(handler) {
            val deferred = async {
                throw TestException
            }

            try {
                deferred.await()
            } catch (e: TestException) {
                println(2)
            }
        }.join()
    }
    /*
    * 2
    * 1
    * */

    @Test
    fun `sample 2`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val handler = CoroutineExceptionHandler { _, _ -> println(1) }
        val handler2 = CoroutineExceptionHandler { _, _ -> println(2) }
        coroutineScope.launch(handler) {
            val deferred = async(handler2) {
                throw TestException
            }

            deferred.await()
        }.join()
    }
    /*
    * 1
    * */

    @Test
    fun `sample 3`() = runBlocking {
        val coroutineScope = CoroutineScope(Job())
        val handler = CoroutineExceptionHandler { _, _ -> println(1) }
        val handler2 = CoroutineExceptionHandler { _, _ -> println(2) }
        coroutineScope.launch(handler) {
            val deferred = coroutineScope.async(handler2) {
                throw TestException
            }

            deferred.await()
        }.join()
    }
    /*
    * 1
    * */
}