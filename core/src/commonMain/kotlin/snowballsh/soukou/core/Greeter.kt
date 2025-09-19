package snowballsh.soukou.core

class Greeter(private val salutation: String = "Hello") {
    fun greet(name: String): String = "$salutation, $name!"
}

