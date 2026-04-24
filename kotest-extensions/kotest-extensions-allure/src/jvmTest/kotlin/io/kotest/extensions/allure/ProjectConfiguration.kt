package io.kotest.extensions.allure

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.allure.api.KotestAllureExecution.EXECUTION_START_CALLBACK
import io.kotest.extensions.allure.api.KotestAllureExecution.setUpFixture

class ProjectConfiguration : AbstractProjectConfig() {
    override val extensions = listOf(KotestAllureListener)

    init {
        EXECUTION_START_CALLBACK = { it.setUpFixture("Project Set Up") }
    }
}
