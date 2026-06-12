package gateway

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))
    val gateway = Gateway(gatewayPort = 12344, registrationPort = 12346)
    gateway.start()
}