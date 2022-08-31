@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(TimelineManagementExtension::class)
class BorschStepTest {

	private val getSaltUseCase: GetSaltUseCase = mock()
	private val getBayLeafUseCase: GetBayLeafUseCase = mock()

	private val viewModel by lazy {
		BorschViewModel(getBayLeafUseCase, getSaltUseCase)
	}

	@Test
	fun `before add bay leaf expect pure water`() = runTest {
		val bayLeaf = mock<BayLeaf>()
		whenever(getBayLeafUseCase()) doReturn bayLeaf

		viewModel.addBayLeaf()

		val actual = viewModel.currentStep
		val expected = BorschViewModel.Step.PureWater

		Assertions.assertEquals(expected, actual)
	}

	@Test
	fun `after add bay leaf expect borsch in progress`() = runTest {
		val bayLeaf = mock<BayLeaf>()
		whenever(getBayLeafUseCase()) doReturn bayLeaf

		viewModel.addBayLeaf()
		runCurrent()

		val actual = viewModel.currentStep
		val expected = BorschViewModel.Step.BorschProgress

		Assertions.assertEquals(expected, actual)
	}

	@Test
	fun `after timer complete expect complete borsch`() = runTest {
		viewModel.lauchBoiling()

		advanceTimeBy(3_600_000L)
		runCurrent()

		val expected = BorschViewModel.Step.Complete
		val actual = viewModel.currentStep

		Assertions.assertEquals(expected, actual)
	}

	@Test
	fun `after timer complete expect passed 3 hours`() = runTest {
		viewModel.lauchBoiling()

		advanceUntilIdle()

		val expectedMs = 3_600_000L
		val actualMs = currentTime

		Assertions.assertEquals(expectedMs, actualMs)
	}
}