package samples.test.cookbook

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface GetHourTimerFlowUseCase {

	operator fun invoke(): Flow<Int>
}

class GetHourFlowImpl : GetHourTimerFlowUseCase {

	override fun invoke(): Flow<Int> =
		flow {
			var currentHour = 0
			while (true) {
				emit(currentHour++)
				delay(MS_IN_HOUR)
			}
		}

	private companion object {

		const val MS_IN_HOUR = 3_600_000L
	}
}


