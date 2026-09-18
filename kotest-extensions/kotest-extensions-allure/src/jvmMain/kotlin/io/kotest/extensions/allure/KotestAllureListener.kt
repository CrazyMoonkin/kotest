package io.kotest.extensions.allure


import io.kotest.common.reflection.bestName
import io.kotest.core.descriptors.Descriptor.TestDescriptor
import io.kotest.core.descriptors.DescriptorId
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.listeners.IgnoredSpecListener
import io.kotest.core.listeners.IgnoredTestListener
import io.kotest.core.listeners.InstantiationErrorListener
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.names.TestName
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.test.TestResult
import io.qameta.allure.model.TestResultContainer
import io.kotest.extensions.allure.api.KotestAllureExecution.allure
import io.kotest.extensions.allure.api.KotestAllureExecution.executionStartCallback
import io.kotest.extensions.allure.api.KotestAllureExecution.projectUuid
import io.kotest.extensions.allure.api.KotestAllureExecution.containerUuid
import io.kotest.extensions.allure.helper.AllureExecutionState
import io.kotest.extensions.allure.helper.AllureLifecycleBootstrap
import io.kotest.extensions.allure.helper.logger
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

/**
 * Extended Kotest Allure listener.
 * It provides full hierarchy model in Allure Report instead of official Kotest extension.
 * With unlimited nesting of steps and support for iterations.
 * See [io.kotest.extensions.allure.api.KotestAllureExecution] - get access to Spec and Project uuid
 * to add Allure fixture.
 */
object KotestAllureListener : ProjectListener,
   TestListener,
   InstantiationErrorListener,
   IgnoredSpecListener,
   IgnoredTestListener {
   internal val log = logger<KotestAllureListener>()

   /**
    * Per-execution state. Kept as `var` so tests can swap in a fresh instance to isolate
    * inner runs from the outer Kotest engine; production code never reassigns it.
    */
   internal var state: AllureExecutionState = AllureExecutionState()

   override suspend fun beforeProject() {
      debug("beforeProject")
      AllureLifecycleBootstrap.clearPreviousResults()

      TestResultContainer().run {
         uuid = projectUuid
         name = "KOTEST PROJECT EXECUTION"
         allure.startTestContainer(this)
         executionStartCallback(uuid)
      }
   }

   override suspend fun afterProject() {
      debug("afterProject")

      allure.stopTestContainer(projectUuid)
      allure.writeTestContainer(projectUuid)
   }

   override suspend fun prepareSpec(kclass: KClass<out Spec>) {
      debug("prepareSpec - $kclass")

      val specContainerUuid = kclass.containerUuid
      val specContainerResult = TestResultContainer().apply {
         uuid = specContainerUuid
         name = specContainerUuid
      }

      allure.startTestContainer(projectUuid, specContainerResult)
   }

   override suspend fun finalizeSpec(kclass: KClass<out Spec>, results: Map<TestCase, TestResult>) {
      debug("finalizeSpec - $kclass")

      val specContainerUuid = kclass.containerUuid
      allure.stopTestContainer(specContainerUuid)
      allure.writeTestContainer(specContainerUuid)
   }

   override suspend fun beforeAny(testCase: TestCase) {
      debug("beforeAny - $testCase")
      if (testCase.descriptor.isRootTest()) state.startScenario(testCase)
      else state.startStep(testCase)
   }

   override suspend fun afterAny(testCase: TestCase, result: TestResult) {
      debug("afterAny - $testCase - $result")
      if (testCase.descriptor.isRootTest()) state.stopScenario(testCase, result)
      else state.stopStep(testCase, result)
   }

   override suspend fun ignoredSpec(kclass: KClass<*>, reason: String?) {
      debug("specIgnored - $kclass")

      emptySpecInternal(kclass, kclass.toDescriptor().id.value, TestResult.Ignored(reason))
   }

   override suspend fun ignoredTest(testCase: TestCase, reason: String?) {
      debug("testIgnored - $testCase - $reason")

      if (reason != "Failfast enabled") {
         if (testCase.descriptor.isRootTest()) state.startScenario(testCase)
         else state.startStep(testCase)
      }

      val ignored = TestResult.Ignored(reason)
      if (testCase.descriptor.isRootTest()) state.stopScenario(testCase, ignored)
      else state.stopStep(testCase, ignored)
   }

   /**
    * On spec class creation error.
    * For example spring context errors.
    */
   override suspend fun instantiationError(kclass: KClass<*>, t: Throwable) {
      debug("instantiationError - $kclass - ${t.localizedMessage}")
      emptySpecInternal(kclass, kclass.toDescriptor().id.value, TestResult.Error(0.seconds, t))
   }

   private fun emptySpecInternal(kclass: KClass<*>, message: String, testResult: TestResult) {
      val specContainerUuid = kclass.containerUuid
      val specContainerResult = TestResultContainer().apply {
         uuid = specContainerUuid
         name = specContainerUuid
      }
      allure.startTestContainer(projectUuid, specContainerResult)
      val informationTestCase = TestCase(
         descriptor = TestDescriptor(kclass.toDescriptor(), DescriptorId(message)),
         name = TestName(kclass.bestName(), false, false, message, null, false),
         spec = object : DslDrivenSpec() {},
         test = {},
         type = TestType.Test
      )

      // The placeholder must not determine the result's container or annotation metadata.
      state.startScenario(informationTestCase, kclass.java.asSubclass(Spec::class.java).kotlin)
      state.stopScenario(informationTestCase, testResult)

      allure.stopTestContainer(specContainerUuid)
      allure.writeTestContainer(specContainerUuid)
   }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private fun debug(msg: String) = if (log.isDebugEnabled) log.debug(msg) else null
}
