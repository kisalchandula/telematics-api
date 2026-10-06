package org.kisal.telematicsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelematicsApiApplication

fun main(args: Array<String>) {
    runApplication<TelematicsApiApplication>(*args)
}
