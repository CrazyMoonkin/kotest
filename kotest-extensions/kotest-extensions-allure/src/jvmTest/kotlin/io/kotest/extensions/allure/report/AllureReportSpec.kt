package io.kotest.extensions.allure.report

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.fixture.DescriptionFixtureSpec
import io.kotest.extensions.allure.fixture.EpicFeatureFixtureSpec
import io.kotest.extensions.allure.fixture.KAllureIdFixtureSpec
import io.kotest.extensions.allure.fixture.KJiraFixtureSpec
import io.kotest.extensions.allure.fixture.SeverityFixtureSpec
import io.kotest.extensions.allure.fixture.SimpleFixtureSpec
import io.kotest.extensions.allure.util.AllureTestRunner
import io.kotest.extensions.allure.util.broken
import io.kotest.extensions.allure.util.failed
import io.kotest.extensions.allure.util.passed

class AllureReportSpec : FreeSpec({

    "simple passing test" - {
        "produces PASSED status" {
            val stub = AllureTestRunner.runSpec(SimpleFixtureSpec::class, passed("my test"))

            stub.testResults shouldHaveSize 1
            stub.testResults[0].status shouldBe Status.PASSED
        }

        "preserves test name" {
            val stub = AllureTestRunner.runSpec(SimpleFixtureSpec::class, passed("exact name"))

            stub.testResults[0].name shouldBe "exact name"
        }

        "multiple tests are all written" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed("test one"),
                passed("test two"),
                passed("test three"),
            )

            stub.testResults shouldHaveSize 3
            stub.testResults.map { it.name } shouldContain "test two"
        }
    }

    "failing test" - {
        "produces FAILED status" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                failed("my failing test", AssertionError("expected 1 but was 2")),
            )

            stub.testResults shouldHaveSize 1
            stub.testResults[0].status shouldBe Status.FAILED
        }

        "captures the error message in status details" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                failed("failing", AssertionError("expected 1 but was 2")),
            )

            stub.testResults[0].statusDetails.shouldNotBeNull()
            stub.testResults[0].statusDetails.message shouldContain "expected 1 but was 2"
        }
    }

    "broken test (unexpected exception)" - {
        "produces BROKEN status" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                broken("broken test", RuntimeException("NPE")),
            )

            stub.testResults[0].status shouldBe Status.BROKEN
        }

        "captures the exception message" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                broken("broken", RuntimeException("NPE")),
            )

            stub.testResults[0].statusDetails.message shouldContain "NPE"
        }
    }

    "nested steps" - {
        "are recorded as steps inside the scenario" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed(
                    "root scenario",
                    passed("step one"),
                    passed("step two"),
                ),
            )

            stub.testResults shouldHaveSize 1
            val scenario = stub.testResults[0]
            scenario.name shouldBe "root scenario"
            scenario.steps shouldHaveSize 2
            scenario.steps[0].name shouldBe "step one"
            scenario.steps[1].name shouldBe "step two"
        }

        "deeply nested steps are preserved" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed(
                    "root",
                    passed(
                        "level 1",
                        passed("level 2"),
                    ),
                ),
            )

            val level1 = stub.testResults[0].steps[0]
            level1.name shouldBe "level 1"
            level1.steps shouldHaveSize 1
            level1.steps[0].name shouldBe "level 2"
        }

        "failed nested step propagates FAILED to scenario" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed(
                    "root",
                    failed("failing step", AssertionError("step failed")),
                ),
            )

            stub.testResults[0].status shouldBe Status.FAILED
        }
    }

    "test name metadata extraction" - {
        "Jira key in name is extracted as link and removed from display name" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed("create user [PRJ-100]"),
            )

            val result = stub.testResults[0]
            withClue("name should be cleaned from Jira key") {
                result.name shouldBe "create user"
            }
            withClue("Jira link should be added") {
                result.links.any { it.url?.contains("PRJ-100") == true } shouldBe true
            }
        }

        "AllureId in name is extracted as label and removed from display name" {
            val stub = AllureTestRunner.runSpec(
                SimpleFixtureSpec::class,
                passed("some test #42"),
            )

            val result = stub.testResults[0]
            result.name shouldBe "some test"
            result.labels.any { it.name == "AS_ID" && it.value == "42" } shouldBe true
        }
    }

    "@Epic annotation produces epic label" {
        val stub = AllureTestRunner.runSpec(EpicFeatureFixtureSpec::class, passed("t"))

        stub.testResults[0].labels
            .any { it.name == "epic" && it.value == "Fixture Epic" } shouldBe true
    }

    "@Feature annotation produces feature label" {
        val stub = AllureTestRunner.runSpec(EpicFeatureFixtureSpec::class, passed("t"))

        stub.testResults[0].labels
            .any { it.name == "feature" && it.value == "Fixture Feature" } shouldBe true
    }

    "@KJira annotation produces Jira link" {
        val stub = AllureTestRunner.runSpec(KJiraFixtureSpec::class, passed("t"))

        val links = stub.testResults[0].links
        links.any { it.url?.contains("PROJ-42") == true } shouldBe true
    }

    "@KAllureId annotation produces allure_id label" {
        val stub = AllureTestRunner.runSpec(KAllureIdFixtureSpec::class, passed("t"))

        stub.testResults[0].labels
            .any { it.name == "AS_ID" && it.value == "777" } shouldBe true
    }

    "@Severity annotation produces severity label" {
        val stub = AllureTestRunner.runSpec(SeverityFixtureSpec::class, passed("t"))

        stub.testResults[0].labels
            .any { it.name == "severity" && it.value == "critical" } shouldBe true
    }

    "@KDescription annotation sets description" {
        val stub = AllureTestRunner.runSpec(DescriptionFixtureSpec::class, passed("t"))

        stub.testResults[0].description shouldContain "Fixture spec description"
    }

    "spec container is written" {
        val stub = AllureTestRunner.runSpec(SimpleFixtureSpec::class, passed("t"))

        val specContainerUuid = SimpleFixtureSpec::class.qualifiedName
            ?.replace("[^\\sа-яА-Яa-zA-Z0-9]".toRegex(), "")
            ?.replace("\\s{2,}".toRegex(), "_")
        stub.containers.any { it.uuid == specContainerUuid } shouldBe true
    }

    "standard labels are always present" {
        val stub = AllureTestRunner.runSpec(SimpleFixtureSpec::class, passed("t"))

        val labelNames = stub.testResults[0].labels.map { it.name }
        withClue("suite label") { labelNames shouldContain "suite" }
        withClue("language label") { labelNames shouldContain "language" }
        withClue("framework label") { labelNames shouldContain "framework" }
    }
})
