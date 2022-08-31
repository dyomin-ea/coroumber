package samples.test.cookbook

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface SuspendUntilCompleteUseCase {

	suspend operator fun invoke(): Alarm
}

class SuspendUntilCompleteUseCaseImpl(
	private val cookingTimeMs: Long,
	private val alarmDispatcher: CoroutineDispatcher,
) : SuspendUntilCompleteUseCase {

	override suspend fun invoke(): Alarm =
		withContext(alarmDispatcher) {
			delay(cookingTimeMs)
			Alarm
		}
}