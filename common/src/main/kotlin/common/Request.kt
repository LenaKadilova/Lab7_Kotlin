package common

import common.model.Dragon
import java.io.Serializable


data class Request(
    val commandName: String,
    val argument: String?,
    val dragon: Dragon?
) : Serializable