package gateway

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

    gateway {
        listen(clientPort = 12344, registrationPort = 12346)
        startInteractiveMode()
    }
}