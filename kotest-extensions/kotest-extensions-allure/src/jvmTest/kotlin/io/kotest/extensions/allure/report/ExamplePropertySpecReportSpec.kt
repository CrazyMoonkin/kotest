package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.ExamplePropertySpec
import io.kotest.extensions.allure.util.AllureTestRunner

class ExamplePropertySpecReportSpec : FreeSpec({

    "test results are not empty" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.shouldNotBeEmpty()
    }

    "@Epic annotation produces epic label" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.first().labels
            .any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
    }

    "@Feature annotation produces feature label" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.first().labels
            .any { it.name == "feature" && it.value == "Data Driven" } shouldBe true
    }

    "programmatic TMS link via .tms() produces TMS link" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        val links = stub.testResults.first().links
        withClue("T-100 link") {
            links.any { it.name?.contains("T-100") == true || it.url?.contains("T-100") == true } shouldBe true
        }
    }

    "programmatic task link via .task() produces Jira link" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.first().links
            .any { it.url?.contains("J-100") == true } shouldBe true
    }

    "programmatic allureId via .allureId() produces AS_ID label" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.first().labels
            .any { it.name == "AS_ID" && it.value == "000" } shouldBe true
    }

    "all test results are PASSED because stepNested does not throw" {
        val stub = AllureTestRunner.runSpec(ExamplePropertySpec::class)

        stub.testResults.forEach { result ->
            withClue("result '${result.name}'") {
                result.status shouldBe Status.PASSED
            }
        }
    }
})
