package samples.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class StandardDispatcherTest {

	@Test
	fun `running test without args of runTest expect standard dispatcher`() = runTest {
		val dispatcher = coroutineContext[CoroutineDispatcher]!!
		Assertions.assertTrue(dispatcher::class.java.simpleName == "StandardTestDispatcherImpl")
	}

	@Test
	fun `on method invoke expect unstarted coroutine`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			values.add(42)
		}

		Assertions.assertTrue(values.isEmpty())
	}

	@Test
	fun `on running coroutine expect emitted value`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			values.add(42)
		}
		runCurrent()

		Assertions.assertEquals(values, listOf(42))
	}

	@Test
	fun `on delay expect unstarted coroutine`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(42)
		}
		runCurrent()

		Assertions.assertTrue(values.isEmpty())
	}

	@Test
	fun `on advance time without running tasks expect emitted value`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(42)
		}
		advanceTimeBy(42L)

		Assertions.assertTrue(values.isEmpty())
	}

	@Test
	fun `on advance time noticeably without running tasks expect coroutine execution in timeline order`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(142)
		}

		launch {
			delay(84L)
			values.add(242)
		}

		launch {
			delay(126L)
			values.add(342)
		}
		advanceTimeBy(100L)

		Assertions.assertEquals(listOf(142, 242), values)
	}

	@Test
	fun `on advance time expect emitted value`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(42)
		}
		advanceTimeBy(42L)
		runCurrent()

		Assertions.assertEquals(values, listOf(42))
	}

	@Test
	fun `on advance until idle expect running all test`() = runTest {
		val values = mutableListOf<Int>()

		launch {
			delay(42L)
			values.add(42)
			delay(42L)
			values.add(42)
		}
		advanceUntilIdle()

		println("time: $currentTime")
		println(values)
	}
}
