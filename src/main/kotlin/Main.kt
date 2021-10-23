import kotlinx.coroutines.delay

suspend fun main() {
    delay(42)
    println("Hello, coroutines!")
}