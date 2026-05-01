package io.kotest.extensions.allure.skip

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleAllSkippedFreeSpec
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleAllSkippedFreeSpecReportSpec : FreeSpec({

   "no PASSED, FAILED, or BROKEN results are produced when all root scenarios are !-prefixed" {
      val stub = AllureTestRunner.runSpec(ExampleAllSkippedFreeSpec::class)

      stub.testResults.none {
         it.status == Status.PASSED || it.status == Status.FAILED || it.status == Status.BROKEN
      } shouldBe true
   }
})
