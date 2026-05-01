package io.kotest.extensions.allure.fast

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleFailFastFreeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleFailFastFreeSpecReportSpec : FreeSpec({

   "produces exactly one test result" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults shouldHaveSize 1
   }

   "scenario status is BROKEN because error() throws IllegalStateException" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults[0].status shouldBe Status.BROKEN
   }

   "scenario has the correct name" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults[0].name shouldBe "Scenario: should be skipped steps after fail"
   }

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults[0].labels
         .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Feature annotation produces feature label" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults[0].labels
         .any { it.name == "feature" && it.value == "FreeSpec" } shouldBe true
   }

   "@Story annotation produces story label" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      stub.testResults[0].labels
         .any { it.name == "story" && it.value == "Fail Fast" } shouldBe true
   }

   "first step passes" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      val steps = stub.testResults[0].steps
      withClue("Step: passed 1 status") {
         steps.first { it.name == "Step: passed 1" }.status shouldBe Status.PASSED
      }
   }

   "second step is BROKEN" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      val steps = stub.testResults[0].steps
      withClue("Step: failed 2 status") {
         steps.first { it.name == "Step: failed 2" }.status shouldBe Status.BROKEN
      }
   }

   "broken step captures the error message" {
      val stub = AllureTestRunner.runSpec(ExampleFailFastFreeSpec::class)

      val failedStep = stub.testResults[0].steps.first { it.name == "Step: failed 2" }
      failedStep.statusDetails?.message shouldContain "Fail Fast ERROR"
   }
})
