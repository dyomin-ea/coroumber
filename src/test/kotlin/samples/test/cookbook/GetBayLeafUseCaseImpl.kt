package samples.test.cookbook

class GetBayLeafUseCaseImpl(
	private val cabinet: KitchenCabinetRepository
) : GetBayLeafUseCase {

	override suspend fun invoke(): BayLeaf =
		cabinet.getBayLeaf()
}