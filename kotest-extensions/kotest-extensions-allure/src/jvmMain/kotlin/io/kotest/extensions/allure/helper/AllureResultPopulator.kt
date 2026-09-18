package io.kotest.extensions.allure.helper

import io.kotest.core.test.TestCase
import io.kotest.core.spec.Spec
import io.qameta.allure.model.Label
import io.qameta.allure.util.ResultsUtils
import io.kotest.extensions.allure.api.KotestAllureConstant
import io.kotest.extensions.allure.api.KotestAllureConstant.Var.TEST_NAME_AUTO_CLEAN_UP
import io.kotest.extensions.allure.api.KotestAllureExecution.bestName
import io.kotest.extensions.allure.helper.AllureConfig.prop
import io.kotest.extensions.allure.helper.meta.AllureMetadata
import kotlin.reflect.KClass

/**
 * Populates Allure [AllureTestResult] / [AllureStepResult] from Kotest [TestCase] data:
 * names, labels, links, descriptions, plus name sanitisation for filesystem use.
 */
internal object AllureResultPopulator {

   internal val String?.safeFileName
      get() = this?.replace("[^\\sа-яА-Яa-zA-Z0-9]".toRegex(), "")
         ?.replace("\\s{2,}".toRegex(), "_")
         ?.let { it.takeIf { it.length <= 130 } ?: (it.take(120) + it.hashCode()) } ?: "null"

   internal fun AllureTestResult.updateTestResult(
      testUuid: String,
      test: TestCase,
      meta: AllureMetadata,
      iteration: Int = 0,
      specClass: KClass<out Spec> = test.spec::class,
   ) {
      val suffix = " [$iteration]".takeIf { iteration >= 1 }.orEmpty()
      val index = "$iteration".takeIf { iteration >= 1 }.orEmpty()
      uuid = testUuid

      name = test.name.name.allureMetaCleanUp() + suffix
      description = meta.allDescriptions

      fullName = test.descriptor.bestName().allureMetaCleanUp() + index
      testCaseId = test.descriptor.bestName().allureMetaCleanUp() + index
      historyId = test.descriptor.bestName().allureMetaCleanUp() + index
      labels = testCaseLabels(test, meta, specClass)
      links = meta.allLinks
   }

   internal fun AllureStepResult.updateStepResult(testCase: TestCase, metadata: AllureMetadata) {
      name = testCase.name.name
      description = metadata.allDescriptions
   }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private val isAllureMetaCleanUp = TEST_NAME_AUTO_CLEAN_UP.prop(true)

   private fun String.allureMetaCleanUp() =
      if (isAllureMetaCleanUp)
         replace(KotestAllureConstant.Task.PATTERN, "")
            .replace(KotestAllureConstant.Tms.PATTERN, "")
            .replace(KotestAllureConstant.AllureId.PATTERN, "")
            .trim()
      else this

   private fun testCaseLabels(testCase: TestCase, metadata: AllureMetadata, specClass: KClass<out Spec>): List<Label> {
      val pkgName = specClass.java.`package`.name

      return listOfNotNull(
         ResultsUtils.createSuiteLabel(testCase.descriptor.spec().id.value),
         ResultsUtils.createThreadLabel(),
         ResultsUtils.createHostLabel(),
         ResultsUtils.createLanguageLabel("kotlin"),
         ResultsUtils.createFrameworkLabel("kotest"),
         ResultsUtils.createPackageLabel(pkgName)
      ).plus(metadata.allLabels)
   }
}
