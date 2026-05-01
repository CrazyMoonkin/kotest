package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.ExampleFreeSpec2
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleFreeSpec2ReportSpec : FreeSpec({

   "two test results are captured — active and !-prefixed" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      stub.testResults shouldHaveSize 2
   }

   "active scenario Тест кейс 1 is PASSED" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      val result = stub.testResults.find { it.name == "Тест кейс 1" }
      result.shouldNotBeNull()
      result.status shouldBe Status.PASSED
   }

   "bang-prefixed scenario is not PASSED or FAILED" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      val skipped = stub.testResults.find { it.name == "Тест кейс 2 (пропущен)" }
         ?: stub.testResults.find { it.name != "Тест кейс 1" }
      skipped.shouldNotBeNull()
      withClue("!-prefixed scenario must not pass or fail") {
         skipped.status shouldBe Status.SKIPPED
      }
   }

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      val result = stub.testResults.first { it.name == "Тест кейс 1" }
      result.labels.any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Feature annotation produces feature label" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      val result = stub.testResults.first { it.name == "Тест кейс 1" }
      result.labels.any { it.name == "feature" && it.value == "FreeSpec" } shouldBe true
   }

   "active scenario has recorded steps" {
      val stub = AllureTestRunner.runSpec(ExampleFreeSpec2::class)

      val result = stub.testResults.first { it.name == "Тест кейс 1" }
      withClue("Тест кейс 1 must have at least one step") {
         result.steps.isNotEmpty() shouldBe true
      }
   }
})
