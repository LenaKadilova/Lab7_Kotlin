package server

fun main(args: Array<String>) {
    System.setOut(java.io.PrintStream(System.out, true, "UTF-8"))

    server {
        val port = args.firstOrNull()?.toIntOrNull() ?: 12345
        val host = "127.0.0.1"
        val gatewayHost = "127.0.0.1"
        val gatewayPort = 12346

        initialize()

        start {
            startServer(host, port)
            sendRegistrationRequest(gatewayHost, gatewayPort, "Registration request")
        }

        startInteractiveMode()
    }
}