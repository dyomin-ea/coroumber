package samples.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class RunTest {

	@Test
	fun `on flat delay expect automatic delay skipping`() = runTest {
		delay(42)
		Assertions.assertEquals(42, currentTime)
	}

	@Test
	fun `on delay within launch coroutine expect ignoring delay`() = runTest {
		val job = launch {
			delay(42)
		}

		Assertions.assertEquals(0, currentTime)
		Assertions.assertTrue(job.isActive)
	}

	@Test
	fun `on delay within launch coroutine expect delay skipping`() = runTest {
		val job = launch {
			delay(42)
		}

		advanceUntilIdle()

		Assertions.assertEquals(42, currentTime)
		Assertions.assertTrue(job.isCompleted)
	}

	@Test
	fun `on delay within sus fun expect delay skipping`() = runTest {
		suspend fun suspendFunWithDelay() {
			delay(42)
		}

		suspendFunWithDelay()

		Assertions.assertEquals(42, currentTime)
	}

	@Test
	fun `using test dispatchers doesnt cause physical delays`() = runTest {
		val unconfinedTestDispatcherMark = measureTimeMillis {
			launch(UnconfinedTestDispatcher(coroutineContext[TestCoroutineScheduler])) {
				delay(500)
			}
		}
		advanceUntilIdle()
		Assertions.assertEquals(500, currentTime)
		Assertions.assertTrue(unconfinedTestDispatcherMark < 100)
	}

	@Test
	fun `using non test dispatchers cause physical delays`() = runTest {
		var unconfinedTestDispatcherMark = 0L
		val job = launch(Dispatchers.Unconfined) {
			unconfinedTestDispatcherMark = measureTimeMillis {
				delay(500)
				println("foo")
			}
		}
		job.join()
		Assertions.assertEquals(0, currentTime)
		Assertions.assertTrue(unconfinedTestDispatcherMark >= 500)
	}


}