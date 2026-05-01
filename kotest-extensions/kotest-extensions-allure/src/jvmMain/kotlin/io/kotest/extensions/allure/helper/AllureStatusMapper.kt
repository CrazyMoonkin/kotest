package io.kotest.extensions.allure.helper

import io.kotest.engine.test.TestResult
import io.qameta.allure.model.Status
import io.qameta.allure.model.Status.BROKEN
import io.qameta.allure.model.Status.FAILED
import io.qameta.allure.model.Status.PASSED
import io.qameta.allure.model.Status.SKIPPED
import io.qameta.allure.model.StatusDetails
import org.opentest4j.TestAbortedException
import io.kotest.extensions.allure.api.KotestAllureConstant.Var.SKIP_ON_FAIL
import io.kotest.extensions.allure.helper.AllureConfig.prop
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Maps Kotest [TestResult] to Allure [Status] / [StatusDetails], applies status updates to Allure
 * test/step results, and implements the "skip on previous failure" policy.
 */
internal object AllureStatusMapper {

   internal fun TestResult.toAllure(): Pair<Status, StatusDetails> {
      val status = when (this) {
         is TestResult.Error -> BROKEN
         is TestResult.Failure -> FAILED
         is TestResult.Ignored -> SKIPPED
         is TestResult.Success -> PASSED
      }

      val details = this.errorOrNull?.let { throwable ->
         StatusDetails().apply {
            this.message = throwable.toString()
            this.trace = throwable.readStackTrace()
         }
      } ?: StatusDetails()

      return status to details
   }

   internal fun AllureTestResult.updateStatus(statusAndDetails: Pair<Status, StatusDetails>) {
      // Kotest 5.4.X listener doesn't take into account child steps results.
      // We should find previous fail / broken child steps in ALLURE storage and use it on top
      val closestPreviousErrorOrBrokenStatusAndDetails: Pair<Status, StatusDetails> =
         steps.map { it.status to it.statusDetails }.lastOrNull { it.first.isBrokenOrFailed } ?: statusAndDetails

      val effectiveStatusAndDetails =
         if (statusAndDetails.first.isNotPassed) statusAndDetails else closestPreviousErrorOrBrokenStatusAndDetails

      val currentStatus = this.status
      val needUpdate = currentStatus == null  // status not yet set
         || currentStatus == PASSED  // current status is passed
         || currentStatus == SKIPPED // current status is skipped
      if (needUpdate) {
         this.status = effectiveStatusAndDetails.first
         this.statusDetails = effectiveStatusAndDetails.second
      }
   }

   internal fun AllureStepResult.updateStatus(statusAndDetails: Pair<Status, StatusDetails>) {
      val currentStatus = this.status
      val needUpdate = currentStatus == null // status not yet set
         || currentStatus == PASSED // current status is passed
         || currentStatus == SKIPPED // current status is skipped
      if (needUpdate) {
         this.status = statusAndDetails.first
         this.statusDetails = statusAndDetails.second
      }
   }

   internal fun processSkipResult(result: AllureTestResult) {
      if (skipOnFail and result.isBad) {
         val causeMessage = result.statusDetails.message.removePrefix(SKIP_MSG)
         throw TestAbortedException(SKIP_MSG + causeMessage)
      }
   }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private const val SKIP_MSG: String =
      "Previous step was failed. This will be skipped due to '$SKIP_ON_FAIL' option is true\n"

   private val brokenOrFailed: Array<Status> = arrayOf(BROKEN, FAILED)

   private val skipOnFail: Boolean = SKIP_ON_FAIL.prop(true)

   private val AllureTestResult.isBad: Boolean get() = status?.isBrokenOrFailed ?: false

   private val Status?.isBrokenOrFailed: Boolean get() = this in brokenOrFailed

   private val Status?.isNotPassed get() = this != PASSED

   private fun Throwable.readStackTrace(): String {
      val stringWriter = StringWriter()
      PrintWriter(stringWriter).use { printWriter -> printStackTrace(printWriter) }
      return stringWriter.toString()
   }
}
