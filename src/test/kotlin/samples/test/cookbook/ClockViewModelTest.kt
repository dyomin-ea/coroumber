@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(TestCoroutineExtension::class)
class ClockViewModelTest {

	private val getHourTimerFlowUseCase: GetHourTimerFlowUseCase = mock()

	@Test
	fun `using flowOf expect update state with flowOf last element`() = runTest {
		val flow = flowOf(1, 1, 2, 3, 5, 8)
		whenever(getHourTimerFlowUseCase()).doReturn(flow)

		val viewModel = ClockViewModel(getHourTimerFlowUseCase)

		val currentHour = viewModel.currentHour
		Assertions.assertEquals(8, currentHour)
	}

	@Test
	fun `using sharedFlow expect state update by each emit`() = runTest {
		val sharedFlow = MutableSharedFlow<Int>()
		whenever(getHourTimerFlowUseCase()).doReturn(sharedFlow)

		val viewModel = ClockViewModel(getHourTimerFlowUseCase)

		Assertions.assertEquals(-1, viewModel.currentHour)

		sharedFlow.emit(42)
		Assertions.assertEquals(42, viewModel.currentHour)
	}

}