package io.kotest.extensions.allure.disabled

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleIgnoredFreeSpec
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleIgnoredFreeSpecReportSpec : FreeSpec({

   "@Ignored spec produces no PASSED or FAILED results" {
      val stub = AllureTestRunner.runSpec(ExampleIgnoredFreeSpec::class)

      stub.testResults.none {
         it.status == Status.PASSED || it.status == Status.FAILED || it.status == Status.BROKEN
      } shouldBe true
   }

   "@Ignored spec result is SKIPPED" {
      val stub = AllureTestRunner.runSpec(ExampleIgnoredFreeSpec::class)

      stub.testResults.all { it.status == Status.SKIPPED } shouldBe true
   }
})
