package io.kotest.extensions.allure.fast

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.allure.template.ExampleFailInitFreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.util.AllureTestRunner

class ExampleFailInitFreeSpecReportSpec : FreeSpec({

    "instantiation error produces a BROKEN test result" {
        val stub = AllureTestRunner.runSpec(ExampleFailInitFreeSpec::class)

        stub.testResults.any { it.status == Status.BROKEN } shouldBe true
    }

    "no test inside the spec runs — only the instantiation error result is present" {
        val stub = AllureTestRunner.runSpec(ExampleFailInitFreeSpec::class)

        stub.testResults.none { it.status == Status.PASSED } shouldBe true
    }

    "the instantiation error result mentions the spec class name" {
        val stub = AllureTestRunner.runSpec(ExampleFailInitFreeSpec::class)

        val brokenResult = stub.testResults.first { it.status == Status.BROKEN }
        brokenResult.statusDetails?.message shouldContain "ExampleFailInitFreeSpec"
    }
})
