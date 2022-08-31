package samples.test.cookbook

interface GetBayLeafUseCase {

	suspend operator fun invoke(): BayLeaf
}