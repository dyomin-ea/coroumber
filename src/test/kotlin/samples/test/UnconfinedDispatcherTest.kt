package samples.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class UnconfinedDispatcherTest {

	private val unconfinedTestDispatcher = UnconfinedTestDispatcher()

	@Test
	fun `running test with UTD expect same within context`() = runTest(unconfinedTestDispatcher) {
		Assertions.assertSame(
			unconfinedTestDispatcher,
			coroutineContext[CoroutineDispatcher]!!
		)
	}


	@Test
	fun `on method invoke expect success coroutine start`() = runTest(unconfinedTestDispatcher) {
		val values = mutableListOf<Int>()

		launch {
			values.add(42)
		}

		Assertions.assertEquals(listOf(42), values)
	}

	@Test
	fun `on delay expect time stop on it`() = runTest(unconfinedTestDispatcher) {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(42)
		}

		Assertions.assertEquals(0, currentTime)
	}

	@Test
	fun `on advance time without running expect coroutine execution pause`() = runTest(unconfinedTestDispatcher) {
		val values = mutableListOf<Int>()

		launch {
			delay(42)
			values.add(42)
		}
		advanceTimeBy(42L)

		Assertions.assertTrue(currentTime == 42L)
		Assertions.assertTrue(values.isEmpty())
	}

	@Test
	fun `on advance time and run expect success coroutine execution`() = runTest(unconfinedTestDispatcher) {
		val values = mutableListOf<Int>()

		launch {
			delay(42)
			values.add(42)
		}
		advanceTimeBy(42L)
		runCurrent()

		Assertions.assertEquals(listOf(42), values)
	}

	@Test
	fun `on advance until idle success coroutine execution`() = runTest(unconfinedTestDispatcher) {
		val values = mutableListOf<Int>()

		launch {
			delay(42)
			values.add(42)
		}
		advanceUntilIdle()

		Assertions.assertEquals(listOf(42), values)
	}
}