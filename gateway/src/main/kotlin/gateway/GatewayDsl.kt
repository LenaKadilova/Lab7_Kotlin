package gateway

class GatewayDsl {
    private var gatewayPort: Int = 12344
    private var registrationPort: Int = 12346
    private lateinit var gateway: Gateway

    fun listen(clientPort: Int, registrationPort: Int) {
        this.gatewayPort = clientPort
        this.registrationPort = registrationPort
        gateway = Gateway(gatewayPort, registrationPort)
    }

    fun startInteractiveMode() {
        gateway.start()
    }
}

fun gateway(block: GatewayDsl.() -> Unit) {
    GatewayDsl().apply(block)
}