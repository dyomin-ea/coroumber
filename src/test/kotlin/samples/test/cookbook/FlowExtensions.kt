package samples.test.cookbook

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal interface ValuesCollector<T> : TestFlowCollector<T> {

	val throwable: Throwable?

	val values: Collection<T>

	val job: Job
}

internal val <T> TestFlowCollector<T>.values: Collection<T>
	get() = unwrapped.values

internal val <T> TestFlowCollector<T>.throwable: Throwable?
	get() = unwrapped.throwable

internal val <T> TestFlowCollector<T>.job: Job
	get() = unwrapped.job

private val <T> TestFlowCollector<T>.unwrapped: ValuesCollector<T>
	get() {
		require(this is ValuesCollector)
		return this
	}

sealed interface TestFlowCollector<T>

suspend inline fun <T> Flow<T>.test(context: CoroutineContext = EmptyCoroutineContext, crossinline assertion: suspend TestFlowCollector<T>.() -> Unit) {
	coroutineScope {
		val values = mutableListOf<T>()
		var throwable: Throwable? = null

		val collectionScope = takeIf { context === EmptyCoroutineContext } ?: CoroutineScope(context)

		val job = onEach { values.add(it) }
			.onCompletion { throwable = it }
			.launchIn(collectionScope)

		val collector = object : ValuesCollector<T> {
			override val throwable: Throwable?
				get() = throwable

			override val values: Collection<T>
				get() = values

			override val job: Job
				get() = job
		}

		launch {
			assertion(collector)
			job.cancelAndJoin()
		}
	}
}

fun <T> TestFlowCollector<T>.assertValues(vararg assertionValues: T) {
	assertAll(assertionValues.asList())
}

fun <T> TestFlowCollector<T>.assertAll(assertionValues: Collection<T>) {
	assertEquals(assertionValues, values)
}

fun <T> TestFlowCollector<T>.assertTimes(expected: Int) {
	assertEquals(expected, values.size)
}

fun <T> TestFlowCollector<T>.assertEmpty() {
	assertTrue(values.isEmpty())
}

fun <T> TestFlowCollector<T>.assertException(expected: Exception) {
	assertEquals(expected, throwable)
}

fun <T> TestFlowCollector<T>.assertActive() {
	assertTrue(job.isActive)
}

fun <T> TestFlowCollector<T>.assertCompleted() {
	assertTrue(job.isCompleted)
}