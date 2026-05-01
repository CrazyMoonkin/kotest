package io.kotest.extensions.allure.helper

import org.slf4j.LoggerFactory

internal inline fun <reified T> logger() = LoggerFactory.getLogger(T::class.java)
