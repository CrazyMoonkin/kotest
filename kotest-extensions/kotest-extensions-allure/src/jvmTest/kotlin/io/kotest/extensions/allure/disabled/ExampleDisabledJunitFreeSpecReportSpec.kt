package io.kotest.extensions.allure.disabled

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleDisabledJunitFreeSpec
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleDisabledJunitFreeSpecReportSpec : FreeSpec({

   // @Disabled is a JUnit 5 platform annotation. It is handled by the JUnit Platform
   // layer before Kotest's engine runs. When invoked via TestEngineLauncher directly
   // (as AllureTestRunner does), JUnit's @Disabled is bypassed and tests execute normally.

   "spec is BROKEN because @Disabled is bypassed by TestEngineLauncher and error() runs" {
      val stub = AllureTestRunner.runSpec(ExampleDisabledJunitFreeSpec::class)

      stub.testResults.any { it.status == Status.BROKEN } shouldBe true
   }

   "no test passes because the only test body throws" {
      val stub = AllureTestRunner.runSpec(ExampleDisabledJunitFreeSpec::class)

      stub.testResults.none { it.status == Status.PASSED } shouldBe true
   }

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleDisabledJunitFreeSpec::class)

      stub.testResults.first().labels
         .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Story annotation produces story label" {
      val stub = AllureTestRunner.runSpec(ExampleDisabledJunitFreeSpec::class)

      stub.testResults.first().labels
         .any { it.name == "story" && it.value == "@Disabled" } shouldBe true
   }
})
