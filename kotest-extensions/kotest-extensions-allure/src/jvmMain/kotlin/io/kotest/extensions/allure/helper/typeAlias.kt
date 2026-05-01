package io.kotest.extensions.allure.helper

import io.qameta.allure.model.StepResult
import io.qameta.allure.model.TestResult

// Disambiguate Allure model types from `io.kotest.engine.test.TestResult`, which is used
// throughout the listener — the unqualified `TestResult` always means the Kotest one.
typealias AllureTestResult = TestResult
typealias AllureStepResult = StepResult
