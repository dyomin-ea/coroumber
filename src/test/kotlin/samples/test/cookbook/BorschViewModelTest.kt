@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(TestCoroutineExtension::class)
class BorschViewModelTest {

	private val getBayLeafUseCase: GetBayLeafUseCase = mock()
	private val getSaltUseCase: GetSaltUseCase = mock()

	private val viewModel by lazy {
		BorschViewModel(getBayLeafUseCase, getSaltUseCase)
	}

	@Test
	fun `on add bay leaf expect bay leaf among ingredients`() = runTest {
		val bayLeaf = mock<BayLeaf>()
		whenever(getBayLeafUseCase()) doReturn bayLeaf

		viewModel.addBayLeaf()

		Assertions.assertTrue(bayLeaf in viewModel.ingredients)
	}

	@Test
	fun `before bay leaf was found expect missing it among ingredients`() = runTest {
		whenever(getBayLeafUseCase()).thenNeverAnswer()

		viewModel.addBayLeaf()

		Assertions.assertTrue(viewModel.ingredients.none { it is BayLeaf })
	}

	@Test
	fun `on add salt expect not enough saltiness`() = runTest {
		val salt = mock<Salt>()
		whenever(getSaltUseCase()).doReturn(salt)

		viewModel.addSaltToTaste()

		val expected = 1
		val actual = viewModel.ingredients.count { it == salt }

		Assertions.assertEquals(expected, actual)
	}

	@Test
	fun `on taste expect enough saltiness`() = runTest {
		val salt = mock<Salt>()
		val extraSalt = mock<Salt>()

		whenever(getSaltUseCase())
			.doReturn(salt)
			.doReturn(extraSalt)

		viewModel.addSaltToTaste()
		advanceUntilIdle()

		val expected = 2
		val actual = viewModel.ingredients.count { it is Salt }

		Assertions.assertEquals(expected, actual)
	}
}

