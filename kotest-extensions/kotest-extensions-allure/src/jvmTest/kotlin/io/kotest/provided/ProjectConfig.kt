package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.SpecExecutionOrder
import io.kotest.extensions.allure.api.KotestAllureExecution.executionStartCallback
import io.kotest.extensions.allure.api.KotestAllureExecution.setUpFixture

class ProjectConfig : AbstractProjectConfig() {

   override val specExecutionOrder = SpecExecutionOrder.Annotated

   init {
      executionStartCallback = { it.setUpFixture("Project Set Up") }
   }
}
