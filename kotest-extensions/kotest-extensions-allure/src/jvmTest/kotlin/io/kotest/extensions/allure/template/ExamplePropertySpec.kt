package io.kotest.extensions.allure.template

import io.kotest.core.spec.style.FreeSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.kotest.extensions.allure.api.KotestAllureExecution.allureId
import io.kotest.extensions.allure.api.KotestAllureExecution.task
import io.kotest.extensions.allure.api.KotestAllureExecution.tms
import io.kotest.extensions.allure.stepNested

@Epic("Allure feature annotation on test class")
@Feature("Data Driven")
class ExamplePropertySpec : FreeSpec() {

   init {
      "Property".tms("T-100").task("J-100").allureId("000") - {
         Arb.boolean().checkAll(2) {
            val index = attempts()
            "Nested Scenario $index" - {
               "Step $index" {
                  stepNested()
               }
            }
         }
      }
   }
}
