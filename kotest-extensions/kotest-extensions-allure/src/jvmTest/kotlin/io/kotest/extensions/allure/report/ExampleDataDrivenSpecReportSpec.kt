package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.ExampleDataDrivenSpec
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleDataDrivenSpecReportSpec : FreeSpec({

    "@Epic annotation produces epic label" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.first().labels
            .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
    }

    "@Feature annotation produces feature label" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.first().labels
            .any { it.name == "feature" && it.value == "Data Driven" } shouldBe true
    }

    "@TmsLinks annotation produces TMS links" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        val links = stub.testResults.first().links
        withClue("T-1 link") { links.any { it.name == "T-1" || it.url?.contains("T-1") == true } shouldBe true }
        withClue("T-2 link") { links.any { it.name == "T-2" || it.url?.contains("T-2") == true } shouldBe true }
    }

    "@KAllureId annotation produces AS_ID label" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.first().labels
            .any { it.name == "AS_ID" } shouldBe true
    }

    "Jira key in scenario name is extracted as link" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.first().links
            .any { it.url?.contains("J-100") == true } shouldBe true
    }

    "TMS key in scenario name is extracted as link" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        val links = stub.testResults.first().links
        links.any { it.name?.contains("T-3") == true || it.url?.contains("T-3") == true } shouldBe true
    }

    "AllureId in scenario name is extracted as AS_ID label" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.first().labels
            .any { it.name == "AS_ID" && it.value == "999" } shouldBe true
    }

    "scenario name is cleaned from Jira key, TMS key and AllureId" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        val name = stub.testResults.first().name
        withClue("Jira key removed") { name.contains("[J-100]") shouldBe false }
        withClue("TMS key removed") { name.contains("(T-3)") shouldBe false }
        withClue("AllureId removed") { name.contains("#999") shouldBe false }
    }

    "all test iterations are PASSED" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        stub.testResults.forEach { result ->
            withClue("result '${result.name}'") {
                result.status shouldBe Status.PASSED
            }
        }
    }

    "standard labels are present" {
        val stub = AllureTestRunner.runSpec(ExampleDataDrivenSpec::class)

        val labelNames = stub.testResults.first().labels.map { it.name }
        withClue("suite label") { labelNames shouldContain "suite" }
        withClue("language label") { labelNames shouldContain "language" }
        withClue("framework label") { labelNames shouldContain "framework" }
    }
})
