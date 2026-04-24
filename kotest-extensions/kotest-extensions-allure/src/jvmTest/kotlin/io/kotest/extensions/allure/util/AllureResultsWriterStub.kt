package io.kotest.extensions.allure.util

import io.qameta.allure.AllureResultsWriter
import io.qameta.allure.model.TestResult
import io.qameta.allure.model.TestResultContainer
import java.io.InputStream

class AllureResultsWriterStub : AllureResultsWriter {
    val testResults = mutableListOf<TestResult>()
    val containers = mutableListOf<TestResultContainer>()

    override fun write(testResult: TestResult) {
        testResults.add(testResult)
    }

    override fun write(testResultContainer: TestResultContainer) {
        containers.add(testResultContainer)
    }

    override fun write(source: String, attachment: InputStream) = Unit
}
