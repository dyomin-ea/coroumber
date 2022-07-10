package samples.test

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class STDAsMainDispatcherTest {

	@BeforeEach
	fun before() {
		Dispatchers.setMain(StandardTestDispatcher())
	}

	@AfterEach
	fun after() {
		Dispatchers.resetMain()
	}

	@Test
	fun `running test expect main dispatcher is not the same with test scope dispatcher`() = runTest {
		Assertions.assertNotSame(
			Dispatchers.Main,
			coroutineContext[CoroutineDispatcher]
		)
	}

	@Test
	fun `running common coroutine expect coroutine launch after runCurrent invoke`() = runTest {
		val subject = Subject()

		subject.runCoroutine()

		Assertions.assertTrue(subject.values.isEmpty())

		runCurrent()

		Assertions.assertEquals(listOf(42), subject.values)
	}

	@Test
	fun `running with advance timeline expect success timeline manipulation through test scope`() = runTest {
		val subject = Subject()

		subject.runDelayedCoroutine()

		runCurrent()
		Assertions.assertTrue(subject.values.isEmpty())

		advanceTimeBy(42)
		Assertions.assertTrue(subject.values.isEmpty())

		runCurrent()
		Assertions.assertEquals(listOf(42), subject.values)
	}

	@Test
	fun `running with noticeable time advance expect running until current time`() = runTest {
		val subject = Subject()
		subject.runComplexDelayedCoroutine()

		advanceTimeBy(100L)

		Assertions.assertEquals(listOf(142, 242), subject.values)
	}

	@Test
	fun `running with advance until idle expect no other needs to manipulate timeline`() = runTest {
		val subject = Subject()

		subject.runComplexDelayedCoroutine()

		advanceUntilIdle()
		Assertions.assertEquals(listOf(142, 242, 342), subject.values)
		Assertions.assertEquals(126L, currentTime)
	}

	private class Subject : CoroutineScope {

		override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main

		val values = mutableListOf<Int>()

		fun runCoroutine() {
			launch {
				values.add(42)
			}
		}

		fun runDelayedCoroutine() {
			launch {
				delay(42L)
				values.add(42)
			}
		}

		fun runComplexDelayedCoroutine() {
			launch {
				delay(42L)
				values.add(142)
				delay(42L) // 84
				values.add(242)
				delay(42L) // 126
				values.add(342)
			}
		}
	}
}

