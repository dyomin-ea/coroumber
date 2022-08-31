@file:OptIn(ExperimentalCoroutinesApi::class)

package samples.test.cookbook

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetBayLeafUseCaseImplTest {

	private val cabinet: KitchenCabinetRepository = mock()
	private val useCase = GetBayLeafUseCaseImpl(cabinet)

	@Test
	fun `get bay leaf from cabinet expect bay leaf`() = runTest {
		val expectedBayLeaf = mock<BayLeaf>()
		whenever(cabinet.getBayLeaf()).doReturn(expectedBayLeaf)

		val actualBayLeaf = useCase()

		Assertions.assertEquals(expectedBayLeaf, actualBayLeaf)
	}
}