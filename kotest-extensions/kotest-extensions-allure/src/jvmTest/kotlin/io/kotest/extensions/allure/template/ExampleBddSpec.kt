package io.kotest.extensions.allure.template

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Issue
import io.qameta.allure.Issues
import io.qameta.allure.Link
import io.qameta.allure.Links
import io.kotest.extensions.allure.annotation.KAllureId
import io.kotest.extensions.allure.annotation.KDescription
import io.kotest.extensions.allure.annotation.KTask
import io.kotest.extensions.allure.annotation.KTasks
import io.kotest.extensions.allure.annotation.KTag
import io.kotest.extensions.allure.annotation.KTags
import io.kotest.extensions.allure.step1
import io.kotest.extensions.allure.stepException1
import io.kotest.extensions.allure.stepException2

@Epic("Allure feature annotation on test class")
@Feature("Behavior")
@Links(
    value = [Link(url = "http://iopump.ru"), Link(url = "https://ya.ru")]
)
@KDescription(
    """
    This is multiline description.
    It must be a new line
"""
)
@Issues(
    value = [Issue("TTT-666"), Issue("TTT-777")]
)
@KTasks(
    value = [KTask("TTT-111"), KTask("TTT-000")]
)
@KAllureId("888")
@KTag("autotest")
@KTags(
    value = [KTag("auto"), KTag("test")]
)
class ExampleBddSpec : BehaviorSpec() {

    init {
        Given("[PRJ-100] Start kotest specification Scenario #777") {
            forAll(row("FirstIterArg"), row("SecondIterArg")) { arg ->

                When("Start step 1 [PRJ-110] - $arg") {
                   step1()
                }
                Then("[PRJ-160] Nested step has been printed - $arg") {
                   stepException1()
                }
                And("Step 2 has been printed too [PRJ-1300] - $arg") {
                   stepException2()
                }
            }
        }
    }
}
