@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

class GetHourFlowImplTest {

	private val useCase = GetHourFlowImpl()

	@Test
	fun `on start expect emit 0, then nothing new, and then 1`() = runTest {
		useCase().test {
			Assertions.assertEquals(0, currentTime)
			assertValues(0)

			advanceTimeBy(3_600_000L)
			assertValues(0)

			advanceTimeBy(400_000L)
			assertValues(0, 1)
		}
	}
}