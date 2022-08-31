package samples.test.cookbook

interface KitchenCabinetRepository {

	suspend fun getBayLeaf(): BayLeaf
}