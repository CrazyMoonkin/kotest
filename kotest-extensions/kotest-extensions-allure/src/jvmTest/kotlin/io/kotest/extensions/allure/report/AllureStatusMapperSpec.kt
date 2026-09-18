package io.kotest.extensions.allure.report

import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.extensions.ExtensionException
import io.kotest.engine.test.TestResult
import io.kotest.extensions.allure.helper.AllureStatusMapper.toAllure
import io.kotest.matchers.shouldBe
import io.qameta.allure.model.Status
import org.opentest4j.TestAbortedException
import kotlin.time.Duration.Companion.milliseconds

class AllureStatusMapperSpec : FreeSpec({
   "an explicit test abort is skipped" {
      TestResult.Error(0.milliseconds, TestAbortedException("aborted")).toAllure().first shouldBe Status.SKIPPED
   }

   "an unrelated beforeAny failure remains broken" {
      val error = ExtensionException.BeforeAnyException(IllegalStateException("setup failed"))
      TestResult.Error(0.milliseconds, error).toAllure().first shouldBe Status.BROKEN
   }

   "an error containing an abort as an indirect cause remains broken" {
      val error = ExtensionException.BeforeAnyException(
         IllegalStateException("setup failed", TestAbortedException("earlier abort"))
      )
      TestResult.Error(0.milliseconds, error).toAllure().first shouldBe Status.BROKEN
   }
})
