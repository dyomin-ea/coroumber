package samples

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class NamedDispatcher(val name: String) : CoroutineDispatcher() {

    private val delegationDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        delegationDispatcher.dispatch(context, block)
    }
}