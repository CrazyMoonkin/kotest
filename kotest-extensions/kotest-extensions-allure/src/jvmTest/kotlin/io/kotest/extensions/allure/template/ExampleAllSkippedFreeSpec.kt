package io.kotest.extensions.allure.template

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.qameta.allure.Link
import io.qameta.allure.Links
import io.kotest.extensions.allure.attachText
import io.kotest.extensions.allure.step1
import io.kotest.extensions.allure.step2
import io.kotest.extensions.allure.stepNested

@Links(
   value = [
      Link("iopump.ru"),
      Link("ya.ru")
   ]
)
class ExampleAllSkippedFreeSpec : FreeSpec() {

   init {

      "!Scenario: 1" - {
         forAll(
            row("--1--"),
            row("--2--")
         ) {
            "!Step 1 - $it" {
               step1()
            }
            "!Step 2 - $it" {
               stepNested()
            }
            "!Step 3 - $it" {
               step2()
               if (it == "--2--") throw AssertionError("Only on --2-- iteration")
            }
         }
      }

      "!Scenario: 2" - {
         forAll(
            row("--1--"),
            row("--2--"),
            row("--3--")
         ) {
            "Step 1 [$it]" {
               step1()
            }
            "Step 2 [$it]" {
               stepNested()
            }
            "Step 3 [$it]" {
               step2()
            }
         }

         forAll(
            row("10"),
            row("20"),
         ) {
            "Step 4 [$it]" {
               step1()
               attachText("forAll")
            }
         }
      }
   }
}
