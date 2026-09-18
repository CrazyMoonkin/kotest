package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.qameta.allure.model.Status
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.extensions.allure.template.ExampleBddSpec
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleBddSpecReportSpec : FreeSpec({

   "steps aborted after a failure are SKIPPED and preserve the scenario failure" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults shouldHaveSize 2
      stub.testResults.forEach { result ->
         result.steps.map { it.status } shouldBe listOf(Status.PASSED, Status.FAILED, Status.SKIPPED)
         result.status shouldBe Status.FAILED
         result.statusDetails.message shouldContain "Step error1"
         result.steps.last().statusDetails.message shouldContain "Previous step was failed"
      }
   }

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().labels
         .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Feature annotation produces feature label" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().labels
         .any { it.name == "feature" && it.value == "Behavior" } shouldBe true
   }

   "@KAllureId annotation produces AS_ID label" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().labels
         .any { it.name == "AS_ID" && it.value == "888" } shouldBe true
   }

   "@KDescription sets description" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().description shouldContain "This is multiline description"
   }

   "@KTasks produces Jira links for each key" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      val links = stub.testResults.first().links
      withClue("link TTT-111") {
         links.any { it.name?.contains("TTT-111") == true || it.url?.contains("TTT-111") == true } shouldBe true
      }
      withClue("link TTT-000") {
         links.any { it.name?.contains("TTT-000") == true || it.url?.contains("TTT-000") == true } shouldBe true
      }
   }

   "@KTag and @KTags produce tag labels" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      val tags = stub.testResults.first().labels.filter { it.name == "tag" }.map { it.value }
      withClue("autotest tag") { tags shouldContain "autotest" }
      withClue("auto tag") { tags shouldContain "auto" }
      withClue("test tag") { tags shouldContain "test" }
   }

   "@Links annotation produces links with specified URLs" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      val links = stub.testResults.first().links
      withClue("iopump.ru link") { links.any { it.url?.contains("iopump.ru") == true } shouldBe true }
      withClue("ya.ru link") { links.any { it.url?.contains("ya.ru") == true } shouldBe true }
   }

   "Jira key in test name is extracted as link" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().links
         .any { it.name?.contains("PRJ-100") == true || it.url?.contains("PRJ-100") == true } shouldBe true
   }

   "AllureId in test name is extracted as AS_ID label" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      stub.testResults.first().labels
         .any { it.name == "AS_ID" } shouldBe true
   }

   "test name is cleaned from Jira key and AllureId" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      val name = stub.testResults.first().name
      withClue("Jira key should be removed from name") { name.contains("[PRJ-100]") shouldBe false }
      withClue("AllureId should be removed from name") { name.contains("#777") shouldBe false }
   }

   "standard labels are present" {
      val stub = AllureTestRunner.runSpec(ExampleBddSpec::class)

      val labelNames = stub.testResults.first().labels.map { it.name }
      withClue("suite label") { labelNames shouldContain "suite" }
      withClue("language label") { labelNames shouldContain "language" }
      withClue("framework label") { labelNames shouldContain "framework" }
   }
})
