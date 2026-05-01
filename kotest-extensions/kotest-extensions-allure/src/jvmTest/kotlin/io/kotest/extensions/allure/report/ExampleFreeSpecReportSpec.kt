package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.ExampleFreeSpec
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleFreeSpecReportSpec : FreeSpec({

   "test results are not empty" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      stub.testResults.shouldNotBeEmpty()
   }

   "@Epic annotation produces epic label on every result" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      stub.testResults.forEach { result ->
         withClue("epic label in '${result.name}'") {
            result.labels.any {
               it.name == "epic" && it.value == "Allure feature annotation on test class"
            } shouldBe true
         }
      }
   }

   "@Feature annotation produces feature label on every result" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      stub.testResults.forEach { result ->
         withClue("feature label in '${result.name}'") {
            result.labels.any { it.name == "feature" && it.value == "FreeSpec" } shouldBe true
         }
      }
   }

   "Scenario 2 is PASSED because all its steps pass" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      val scenario2 = stub.testResults.find { it.name == "Start kotest specification Scenario 2" }
      withClue("Scenario 2 must be present") { scenario2 shouldBe scenario2 }
      scenario2?.status shouldBe Status.PASSED
   }

   "Scenario 1 has at least one FAILED iteration because step 2 fails on --2--" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      stub.testResults.any { it.name.contains("Scenario 1") && it.status == Status.FAILED } shouldBe true
   }

   "spec container is written" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec::class)

      stub.containers.shouldNotBeEmpty()
   }
})
