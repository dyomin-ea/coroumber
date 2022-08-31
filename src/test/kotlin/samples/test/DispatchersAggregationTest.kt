package samples.test

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DispatchersAggregationTest {

	private val customScheduler = TestCoroutineScheduler()

	@Test
	fun `passing scheduler to main expect same withing a test scope`() {
		Dispatchers.setMain(UnconfinedTestDispatcher(customScheduler))

		runTest {
			Assertions.assertSame(customScheduler, testScheduler)
		}

		Dispatchers.resetMain()
	}

	@Test
	fun `using variable as test subject expect usage of the same timeline source`() {
		Dispatchers.setMain(UnconfinedTestDispatcher())

		runTest {
			val subject = Subject(UnconfinedTestDispatcher())

			val actual = subject.runWithinPassedDispatcher()

			Assertions.assertEquals(42, actual)
			Assertions.assertEquals(42_000_000L, currentTime)
		}

		Dispatchers.resetMain()
	}

	@Test
	fun `passing standard dispatcher expect fail to control timeline`() {
		Dispatchers.setMain(UnconfinedTestDispatcher())

		runTest {
			val subject = Subject(StandardTestDispatcher())

			val actual = subject.runWithinPassedDispatcher()

			Assertions.assertEquals(42, actual)
			Assertions.assertEquals(42_000_000L, currentTime)
		}

		Dispatchers.resetMain()
	}

	@Test
	fun `run delayed task expect test dispatcher does not pause execution before delay`() = runTest {
		val subject = Subject()

		val actual = subject.run()

		Assertions.assertEquals(42, actual)
		Assertions.assertEquals(42_000_000L, currentTime)
	}

	@Test
	fun tbd() = runTest {

//		val subject = Subject()

		launch {
//			val actual = subject.run()

			delay(42_000_000L)
//			Assertions.assertEquals(42, actual)
			println("42")
		}.join()

		println(currentTime)
		Assertions.assertEquals(42_000_000L, currentTime)
	}

	class Subject(
		// will be passed by tests where ever it'll be used
		private val dispatcher: CoroutineDispatcher = StandardTestDispatcher()
	) {

		suspend fun runWithinPassedDispatcher(): Int =
			withContext(dispatcher) {
				delay(42_000_000L)
				42
			}

		suspend fun run(): Int {
			delay(42_000_000L)
			return 42
		}
	}
}