package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.ExampleStringSpec
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleStringSpecReportSpec : FreeSpec({

   "@Epic annotation produces epic label" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      stub.testResults.first().labels
         .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
   }

   "@Feature annotation produces feature label" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      stub.testResults.first().labels
         .any { it.name == "feature" && it.value == "StringSpec" } shouldBe true
   }

   "AllureId in test name is extracted as AS_ID label" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      stub.testResults.first().labels
         .any { it.name == "AS_ID" && it.value == "666" } shouldBe true
   }

   "test name is cleaned from AllureId" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      stub.testResults.first().name.contains("#666") shouldBe false
   }

   "spec has setUp fixture in container" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      withClue("at least one container with befores") {
         stub.containers.any { it.befores.isNotEmpty() } shouldBe true
      }
      withClue("setUp fixture name") {
         stub.containers.flatMap { it.befores }
            .any { it.name.contains("Set up testing fixture") } shouldBe true
      }
   }

   "spec has tearDown fixture in container" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      withClue("at least one container with afters") {
         stub.containers.any { it.afters.isNotEmpty() } shouldBe true
      }
      withClue("tearDown fixture name") {
         stub.containers.flatMap { it.afters }
            .any { it.name.contains("Tear Down testing fixture") } shouldBe true
      }
   }

   "test is FAILED because stepException1 throws AssertionError" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      stub.testResults.any { it.status == Status.FAILED } shouldBe true
   }

   "standard labels are present" {
      val stub = AllureTestRunner.runSpec(ExampleStringSpec::class)

      val labelNames = stub.testResults.first().labels.map { it.name }
      withClue("suite label") { labelNames shouldContain "suite" }
      withClue("language label") { labelNames shouldContain "language" }
      withClue("framework label") { labelNames shouldContain "framework" }
   }
})
