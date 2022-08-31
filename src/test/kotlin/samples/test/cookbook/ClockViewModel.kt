package samples.test.cookbook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ClockViewModel(
	getHourTimerFlowUseCase: GetHourTimerFlowUseCase,
) {

	private val scope by lazy {
		CoroutineScope(SupervisorJob() + Dispatchers.Main)
	}

	var currentHour = -1

	init {
		getHourTimerFlowUseCase()
			.onEach { currentHour = it }
			.launchIn(scope)
	}
}