package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.template.BigSpecSpec
import io.kotest.extensions.allure.util.AllureTestRunner

class BigSpecReportSpec : FreeSpec({

    "two root scenarios are captured" {
        val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

        stub.testResults shouldHaveSize 2
        stub.testResults.map { it.name } shouldContain "Scenario: Getting employee by id"
        stub.testResults.map { it.name } shouldContain "Scenario: Creating new employee"
    }

    "all scenarios are PASSED" {
        val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

        stub.testResults.forEach { scenario ->
            withClue("scenario '${scenario.name}'") {
                scenario.status shouldBe Status.PASSED
            }
        }
    }

    "all steps are PASSED" {
        val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

        stub.testResults.forEach { scenario ->
            scenario.steps.forEach { step ->
                withClue("step '${step.name}' inside '${scenario.name}'") {
                    step.status shouldBe Status.PASSED
                }
            }
        }
    }

    "Scenario: Getting employee by id" - {

        "has 3 steps" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val scenario = stub.testResults.find { it.name == "Scenario: Getting employee by id" }
            scenario.shouldNotBeNull()
            scenario.steps shouldHaveSize 3
        }

        "first step is Given" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val steps = stub.testResults.find { it.name == "Scenario: Getting employee by id" }!!.steps
            steps[0].name shouldBe "Given test environment is up and test data prepared"
        }

        "second step is When" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val steps = stub.testResults.find { it.name == "Scenario: Getting employee by id" }!!.steps
            steps[1].name shouldStartWith "When client sent request to get the employee by id="
        }

        "third step is Then" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val steps = stub.testResults.find { it.name == "Scenario: Getting employee by id" }!!.steps
            steps[2].name shouldStartWith "Then client received response with status 200 and id="
        }
    }

    "Scenario: Creating new employee" - {

        "has 4 steps" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val scenario = stub.testResults.find { it.name == "Scenario: Creating new employee" }
            scenario.shouldNotBeNull()
            scenario.steps shouldHaveSize 4
        }

        "step names are correct in order" {
            val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

            val steps = stub.testResults.find { it.name == "Scenario: Creating new employee" }!!.steps
            steps.map { it.name } shouldBe listOf(
                "Given test environment is up and test data prepared",
                "When client sent request to create new employee",
                "Then server received request with employee",
                "And client received response with status 200 with generated id",
            )
        }
    }

    "@Epic and @Feature labels are present on every test result" {
        val stub = AllureTestRunner.runSpec(BigSpecSpec::class)

        stub.testResults.forEach { result ->
            withClue("@Epic label in '${result.name}'") {
                result.labels.any { it.name == "epic" && it.value == "Allure feature annotation on test class" } shouldBe true
            }
            withClue("@Feature label in '${result.name}'") {
                result.labels.any { it.name == "feature" && it.value == "Concurrency" } shouldBe true
            }
        }
    }
})
