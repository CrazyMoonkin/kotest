package io.kotest.extensions.allure.skip

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleSomeSkippedFreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleSomeSkippedFreeSpecReportSpec : FreeSpec({

   "active scenarios produce test results" {
      val stub = AllureTestRunner.runSpec(ExampleSomeSkippedFreeSpec::class)

      stub.testResults.shouldNotBeEmpty()
   }

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleSomeSkippedFreeSpec::class)

      stub.testResults.first().labels
         .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Feature annotation produces feature label" {
      val stub = AllureTestRunner.runSpec(ExampleSomeSkippedFreeSpec::class)

      stub.testResults.first().labels
         .any { it.name == "feature" && it.value == "FreeSpec" } shouldBe true
   }

   "bang-prefixed root scenario does not produce a PASSED, FAILED, or BROKEN result" {
      val stub = AllureTestRunner.runSpec(ExampleSomeSkippedFreeSpec::class)

      stub.testResults.none {
         it.name.contains("Skipped Scenario 2") &&
            (it.status == Status.PASSED || it.status == Status.FAILED || it.status == Status.BROKEN)
      } shouldBe true
   }

   "Scenario 1 has at least one FAILED iteration because step 2 fails on --2--" {
      val stub = AllureTestRunner.runSpec(ExampleSomeSkippedFreeSpec::class)

      stub.testResults.any { it.name.contains("Scenario 1") && it.status == Status.FAILED } shouldBe true
   }

})
