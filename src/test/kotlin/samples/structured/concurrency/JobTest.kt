package samples.structured.concurrency

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import samples.TestException
import samples.silentExceptionHandler

@OptIn(ExperimentalCoroutinesApi::class)
class JobTest {

    @Test
    fun `on job is done expect completion`() = runBlocking {
        val job = launch {
            doNoting()
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun `on exception expect cancellation`() = runBlocking {
        val parentJob = Job()

        val siblingJob = CoroutineScope(parentJob + silentExceptionHandler).launch {
            throw TestException
        }

        siblingJob.join()
        assertTrue(parentJob.isCancelled)
    }

    @Test
    fun `on sibling exception expect cancellation`() = runBlocking {
        val parentJob = SupervisorJob()
        val childJob = CoroutineScope(parentJob + silentExceptionHandler).launch {
            throw TestException
        }

        childJob.join()
        assertFalse(parentJob.isCancelled)
    }

    suspend fun doNoting() = Unit
}