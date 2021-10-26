package samples.structured.concurrency

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import samples.silentExceptionHandler

class CancellationExceptionTest {

    private val scope: CoroutineScope
        get() = CoroutineScope(Job())

    @Test
    fun `when parent is a job in parallel coroutines expect full cancellation`() = runBlocking {
        val parentJob = Job()
        var c1: Job = Job()
        var c2: Job = Job()
        var c3: Job = Job()

        scope.launch(parentJob + silentExceptionHandler) {
            c1 = launch {
                try {
                    doWork("c1")
                } catch (e: Exception) {
                    println("c1 got exception $e")
                }
            }
            c2 = launch {
                try {
                    doWork("c2")
                } catch (e: Exception) {
                    println("c2 got exception $e")
                }
            }
            c3 = launch {
                try {
                    doWork("c3")
                } catch (e: Exception) {
                    println("c3 got exception $e")
                }
            }
            launch {
                delay(2000)
                println("Hello c4")
                throw RuntimeException("Cancellation trigger")
            }

        }

        parentJob.join()

        assertTrue(parentJob.isCancelled)
        assertTrue(c1.isCancelled)
        assertTrue(c2.isCancelled)
        assertTrue(c3.isCancelled)
    }

    @Test
    fun `when parent is a job in coroutines sibling chain expect full cancellation`() = runBlocking {
        val parentJob = Job()
        var c1: Job = Job()
        var c2: Job = Job()
        var c3: Job = Job()

        scope.launch(parentJob + silentExceptionHandler) {
            c1 = launch {
                c2 = launch {
                    c3 = launch {
                        try {
                            doWork("c3")
                        } catch (e: Exception) {
                            println("c3 got exception $e")
                        }
                    }
                    try {
                        doWork("c2")
                    } catch (e: Exception) {
                        println("c2 got exception $e")
                    }
                }
                try {
                    doWork("c1")
                } catch (e: Exception) {
                    println("c1 got exception $e")
                }
            }
            val launch = launch {
                delay(2000)
                println("Hello c4")
                throw ArithmeticException()
            }

        }

        parentJob.join()

        assertTrue(parentJob.isCancelled)
        assertTrue(c1.isCancelled)
        assertTrue(c2.isCancelled)
        assertTrue(c3.isCancelled)
    }

    private suspend fun doWork(name: String) {
        for (i in 0..40) {
            delay(500)
            println("$name $i")
        }
    }
}