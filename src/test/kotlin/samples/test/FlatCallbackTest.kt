package samples.test

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import samples.test.FlatCallbackTest.Expect
import samples.test.FlatCallbackTest.OnCallback

@OptIn(ExperimentalCoroutinesApi::class)
class FlatCallbackTest {

	@Test
	fun `success use case test`() = runTest {
		val caller = object : Caller {

			override var callback: Callback? = null

			fun success() {
				callback!!.onSuccess("success")
			}
		}

		concurrently { test(caller) }
			.performCallback { caller.success() }
			.expect { call ->
				Assertions.assertEquals("success", call.await())
			}
	}

	@Test
	fun `make it shorter`() = runTest {
		val caller = object : Caller {

			override var callback: Callback? = null

			fun success() {
				callback!!.onSuccess("success")
			}
		}

		concurrently { test(caller) }
			.performCallback { caller.success() }
			.expect { Assertions.assertEquals("success", it.await()) }
	}

	@Test
	fun `failure use case test`() = runTest {
		val caller = object : Caller {

			override var callback: Callback? = null

			fun fail() {
				callback!!.onFailure(Exception("failure"))
			}
		}

		concurrently { test(caller) }
			.performCallback { caller.fail() }
			.expect { call ->
				val exception = assertThrows<Exception> {
					runBlocking { call.await() }
				}
				Assertions.assertEquals("failure", exception.message)
			}
	}

	private inline fun <T> TestScope.concurrently(crossinline body: suspend () -> T): OnCallback<T> =
		OnCallback { callback ->
			Expect {
				val deferred = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
					.async { body() }
				callback()
				it(deferred)
			}
		}

	private fun interface OnCallback<T> {

		fun performCallback(body: suspend () -> Unit): Expect<T>
	}

	private fun interface Expect<T> {

		suspend fun expect(assertion: suspend (Deferred<T>) -> Unit)
	}
}

suspend fun test(caller: Caller): String =
	suspendCancellableCoroutine {
		caller.callback = object : Callback {
			override fun onSuccess(value: String) {
				it.resumeWith(Result.success(value))
			}

			override fun onFailure(reason: Exception) {
				it.resumeWith(Result.failure(reason))
			}
		}

		it.invokeOnCancellation {
			caller.callback = null
		}
	}

interface Callback {

	fun onSuccess(value: String)

	fun onFailure(reason: Exception)
}

interface Caller {

	var callback: Callback?
}