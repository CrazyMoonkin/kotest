package io.kotest.extensions.allure.fixture

import io.kotest.core.spec.style.FreeSpec
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.kotest.extensions.allure.annotation.KAllureId
import io.kotest.extensions.allure.annotation.KDescription
import io.kotest.extensions.allure.annotation.KJira

/**
 * Fixture specs used by [io.kotest.extensions.allure.report.AllureReportSpec].
 * They have no tests defined — test cases are injected programmatically via AllureTestRunner.
 */

class SimpleFixtureSpec : FreeSpec()

@Epic("Fixture Epic")
@Feature("Fixture Feature")
class EpicFeatureFixtureSpec : FreeSpec()

@KJira("PROJ-42")
class KJiraFixtureSpec : FreeSpec()

@KAllureId("777")
class KAllureIdFixtureSpec : FreeSpec()

@KDescription("Fixture spec description")
class DescriptionFixtureSpec : FreeSpec()

@Epic("Severity Epic")
@Severity(SeverityLevel.CRITICAL)
class SeverityFixtureSpec : FreeSpec()
