@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestCoroutineExtension::class)
class SuspendUntilCompleteUseCaseImplTest {

	@Test
	fun `on alarm expect passed aggregated time`() = runTest {
		val timeoutMs = 100_500L
		val useCase = SuspendUntilCompleteUseCaseImpl(
			timeoutMs,
			UnconfinedTestDispatcher()
		)

		useCase()

		Assertions.assertEquals(timeoutMs, currentTime)
	}
}