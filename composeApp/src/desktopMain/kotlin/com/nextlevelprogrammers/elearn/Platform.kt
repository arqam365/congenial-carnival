package com.nextlevelprogrammers.elearn

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class JVMPlatform {
    @OptIn(ExperimentalTime::class)
    val name: String = "Java ${Clock.System.getProperty("java.version")}"
}

@OptIn(ExperimentalTime::class)
private fun Clock.System.getProperty(string: String): String {

    return TODO("Provide the return value")
}

fun getPlatform() = JVMPlatform()