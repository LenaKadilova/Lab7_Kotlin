package client

fun main() {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

    client {
        connect()
        authorize()

        if (authorized) {
            startInteractiveMode()
        }
    }
}