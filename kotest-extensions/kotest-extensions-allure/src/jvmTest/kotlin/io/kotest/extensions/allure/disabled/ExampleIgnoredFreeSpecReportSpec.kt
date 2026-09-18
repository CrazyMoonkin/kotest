package io.kotest.extensions.allure.disabled

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleIgnoredFreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.extensions.allure.api.KotestAllureExecution.containerUuid
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

      stub.testResults.single().status shouldBe Status.SKIPPED
   }

   "ignored spec result belongs to its spec container and retains class metadata" {
      val stub = AllureTestRunner.runSpec(ExampleIgnoredFreeSpec::class)
      val result = stub.testResults.single()
      val container = stub.containers.single { it.uuid == ExampleIgnoredFreeSpec::class.containerUuid }

      container.children shouldContain result.uuid
      result.labels.single { it.name == "package" }.value shouldBe "io.kotest.extensions.allure.template"
      result.labels.single { it.name == "feature" }.value shouldBe "FreeSpec"
      result.labels.single { it.name == "story" }.value shouldBe "@Ignored"
   }
})
