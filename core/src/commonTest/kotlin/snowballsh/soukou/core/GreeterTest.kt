package snowballsh.soukou.core

import kotlin.test.Test
import kotlin.test.assertEquals

class GreeterTest {
    @Test
    fun greetsWithDefaultSalutation() {
        val greeter = Greeter()
        assertEquals("Hello, World!", greeter.greet("World"))
    }

    @Test
    fun greetsWithCustomSalutation() {
        val greeter = Greeter("Hey")
        assertEquals("Hey, Kotlin!", greeter.greet("Kotlin"))
    }
}
