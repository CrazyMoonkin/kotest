package io.kotest.extensions.allure.template

import io.kotest.core.spec.style.FreeSpec
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Link
import io.qameta.allure.Links

@Epic("Allure feature annotation on test class")
@Feature("FreeSpec")
@Links(
   value = [
      Link("iopump.ru"),
      Link("ya.ru")
   ]
)
class ExampleFreeSpec2 : FreeSpec() {
   init {

      "Тест кейс 1" - {
         "Шаг 1" - {
            "!Шаг 1 - 1 (пропущен)" - {
               "Шаг 1 - 1 - 1 (пропущен из-за контейнера-родителя)" {}
            }
            "Шаг 1 - 2" - {
               "!Шаг 1 - 2 - 1 (пропущен)" {}
               "Шаг 1 - 2 - 2" - {
                  "Шаг 1 - 2 - 2 - 1" {}
               }
            }
         }
         "!Шаг 2 (пропущен)" {}
      }

      "!Тест кейс 2 (пропущен)" - {
         "Шаг 1" {}
      }
   }
}
