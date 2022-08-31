package samples.test.cookbook

import kotlinx.coroutines.*

class BorschViewModel(
	private val getBayLeafUseCase: GetBayLeafUseCase,
	private val getSaltUseCase: GetSaltUseCase,
) {

	private val scope by lazy {
		CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
	}

	val ingredients = mutableSetOf<Ingredient>()
	var currentStep: Step = Step.BorschDreams
		private set

	fun addBayLeaf() {
		currentStep = Step.PureWater
		scope.launch {
			val bayLeaf = getBayLeafUseCase()
			ingredients.add(bayLeaf)
			currentStep = Step.BorschProgress
		}
	}

	fun addSaltToTaste() {
		scope.launch {
			addSalt()
			taste()

			val saltiness = ingredients
				.filter { it is Salt }
				.count()

			if (saltiness < 2) {
				addSalt()
			}
		}
	}

	private suspend fun taste() {
		delay(COUPLE_MOMENTS_MS)
	}

	private fun addSalt() {
		val salt = getSaltUseCase()
		ingredients.add(salt)
	}

	fun lauchBoiling() {
		scope.launch {
			launchTimer()
			currentStep = Step.Complete
		}
	}

	private suspend fun launchTimer() {
		delay(COOKING_TIME_MS)
	}

	companion object {

		val COOKING_TIME_MS = 3_600_000L

		val COUPLE_MOMENTS_MS = 1_000L
	}

	sealed interface Step {

		object BorschDreams : Step

		object PureWater : Step

		object BorschProgress : Step

		object Complete : Step
	}
}

