package io.kotest.extensions.allure.template

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Links
import io.kotest.extensions.allure.attachText
import io.kotest.extensions.allure.step1
import io.kotest.extensions.allure.step2
import io.kotest.extensions.allure.stepNested

@Epic("Allure feature annotation on test class")
@Feature("FreeSpec")
@Links(
   value = [
      Link("iopump.ru"),
      Link("ya.ru")
   ]
)
class ExampleSomeSkippedFreeSpec : FreeSpec() {

   init {

      "Start kotest specification Scenario 1 (skip test)" - {
         forAll(
            row("--1--"),
            row("--2--")
         ) {
            "Start step 1 - $it" {
               step1()
            }
            "!Skipped step  - $it" {
               stepNested()
            }
            "Step 2 has been printed too  - $it" {
               step2()
               if (it == "--2--") throw AssertionError("Only on --2-- iteration")
            }
         }
      }

      "!Skipped Scenario 2" - {
         forAll(
            row("--1--"),
            row("--2--"),
            row("--3--")
         ) {
            "Start step 1 [$it]" {
               step1()
            }
            "Nested step has been printed [$it]" {
               stepNested()
            }
            "Step 2 has been printed too [$it]" {
               step2()
            }
         }

         forAll(
            row("10"),
            row("20"),
         ) {
            "!Just single step [$it]" {
               step1()
               attachText("forAll")
            }
         }
      }
   }
}
