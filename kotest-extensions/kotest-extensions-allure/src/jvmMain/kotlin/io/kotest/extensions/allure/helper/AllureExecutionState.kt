package io.kotest.extensions.allure.helper

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.Descriptor.TestDescriptor
import io.kotest.core.source.SourceRef
import io.kotest.core.source.SourceRef.ClassLineSource
import io.kotest.core.source.SourceRef.ClassSource
import io.kotest.core.source.SourceRef.None
import io.kotest.engine.test.TestResult
import io.kotest.engine.test.TestResult.Success
import io.qameta.allure.model.Status
import io.qameta.allure.model.StatusDetails
import io.qameta.allure.model.StepResult
import io.kotest.extensions.allure.api.KotestAllureConstant.Var.DATA_DRIVEN_SUPPORT
import io.kotest.extensions.allure.api.KotestAllureExecution.allure
import io.kotest.extensions.allure.api.KotestAllureExecution.containerUuid
import io.kotest.extensions.allure.helper.AllureConfig.prop
import io.kotest.extensions.allure.helper.AllureResultPopulator.updateStepResult
import io.kotest.extensions.allure.helper.AllureResultPopulator.updateTestResult
import io.kotest.extensions.allure.helper.AllureStatusMapper.processSkipResult
import io.kotest.extensions.allure.helper.AllureStatusMapper.toAllure
import io.kotest.extensions.allure.helper.AllureStatusMapper.updateStatus
import io.kotest.extensions.allure.helper.meta.AllureMetadata
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/**
 * Per-execution state for the Allure listener: tracks scenario/step UUIDs and DDT iteration index.
 *
 * Each instance is independent — replacing [io.kotest.extensions.allure.KotestAllureListener.state]
 * with a fresh instance is the primary isolation mechanism for tests, replacing the older
 * `snapshot/reset/restore` triple.
 */
internal class AllureExecutionState {

   private val log = logger<AllureExecutionState>()
   private val dataDrivenSupport: Boolean = DATA_DRIVEN_SUPPORT.prop(true)
   private val testUuidMap: MutableMap<Descriptor, String> = ConcurrentHashMap()
   private val iterationMap: MutableMap<Descriptor, Iteration> = ConcurrentHashMap()

   internal fun startScenario(testCase: KotestTestCase): String =
      testUuidMap.computeIfAbsent(testCase.descriptor) { uuid() }
         .also { uuid ->
            val index = when (dataDrivenSupport) {
               true -> iterationMap
                  .compute(testCase.descriptor) { _, iteration -> Iteration.next(testCase, iteration) }
                  ?.index
                  ?.also { if (it >= 1) log.debug("New iteration has been started with index '$it'") }
                  ?: 0

               false -> 0
            }
            val metadata = AllureMetadata(testCase.spec::class, testCase.descriptor)
            val result = AllureTestResult().apply { updateTestResult(uuid, testCase, metadata, index) }
            allure.scheduleTestCase(testCase.spec.containerUuid, result)
            allure.startTestCase(uuid)
         }

   internal fun stopScenario(testCase: KotestTestCase, testResult: KotestTestResult, prune: Boolean = true) {
      val uuid = testUuidMap[testCase.descriptor]
      if (uuid == null) {
         log.error("Cannot stop Scenario '$testCase' because it hasn't been started")
         return
      }
      allure.updateTestCase(uuid) { it.updateStatus(testResult.toAllure()) }
      allure.stopTestCase(uuid)
      allure.writeTestCase(uuid)

      if (dataDrivenSupport && prune) iterationMap.remove(testCase.descriptor)
      testUuidMap.remove(testCase.descriptor)
   }

   internal fun stopScenario(testCase: KotestTestCase, reason: String?, prune: Boolean = true) {
      val uuid = testUuidMap[testCase.descriptor]
      if (uuid == null) {
         log.error("Cannot stop Scenario '$testCase' because it hasn't been started")
         return
      }
      allure.updateTestCase(uuid) {
         it.updateStatus(Status.SKIPPED to StatusDetails().apply { this.message = reason })
      }
      allure.stopTestCase(uuid)
      allure.writeTestCase(uuid)

      if (dataDrivenSupport && prune) iterationMap.remove(testCase.descriptor)
      testUuidMap.remove(testCase.descriptor)
   }

   internal fun startStep(testCase: KotestTestCase) {
      testUuidMap.computeIfAbsent(testCase.descriptor) { uuid() }
         .also { uuid ->
            if (dataDrivenSupport) processIteration(testCase)
            val parentUuid = testCase.parentUuid
            if (parentUuid != null) {
               val metadata = AllureMetadata(description = testCase.descriptor)
               val result = StepResult().also { it.updateStepResult(testCase, metadata) }
               allure.startStep(parentUuid, uuid, result)
               processSkip(testCase)
            } else {
               startScenario(testCase)
            }
         }
   }

   internal fun stopStep(testCase: KotestTestCase, testResult: KotestTestResult) {
      val uuid = testUuidMap[testCase.descriptor]
      if (uuid == null) {
         log.error("Cannot stop Step '$testCase' because it hasn't been started")
         return
      }
      if (testCase.parentUuid == null) {
         stopScenario(testCase, testResult)
         return
      }
      allure.updateStep(uuid) { it.updateStatus(testResult.toAllure()) }
      allure.stopStep(uuid)
      if (testResult.needPassOnTop) {
         testCase.descriptor.parents().forEach { description ->
            val parentUuid = testUuidMap[description] ?: return@forEach
            if (description.isRootTest())
               allure.updateTestCase(parentUuid) { it.updateStatus(testResult.toAllure()) }
            else
               allure.updateStep(parentUuid) { it.updateStatus(testResult.toAllure()) }
         }
      }
      testUuidMap.remove(testCase.descriptor)
   }

   internal fun stopStep(testCase: KotestTestCase, reason: String?) {
      val uuid = testUuidMap[testCase.descriptor]
      if (uuid == null) {
         log.error("Cannot stop Step '$testCase' because it hasn't been started")
         return
      }
      if (testCase.parentUuid == null) {
         stopScenario(testCase = testCase, reason = reason)
         return
      }
      allure.updateStep(uuid) {
         it.updateStatus(Status.SKIPPED to StatusDetails().apply { this.message = reason })
      }
      allure.stopStep(uuid)
      testUuidMap.remove(testCase.descriptor)
   }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private fun processSkip(testCase: KotestTestCase) {
      val scenario = testCase.scenario ?: return
      val scenarioUuid = testUuidMap[scenario] ?: return
      allure.updateTestCase(scenarioUuid) { processSkipResult(it) }
   }

   private fun processIteration(testCase: KotestTestCase) {
      val scenario = testCase.scenario ?: return
      val iteration = iterationMap[scenario] ?: return
      if (iteration.isNotStarted) {
         iteration.start(testCase)
         return
      }
      if (testCase.isNewIteration(iteration)) {
         stopScenario(iteration.scenario, Success(0.milliseconds), false)
         startScenario(iteration.scenario)
         iteration.start(testCase)
      }
   }

   private fun KotestTestCase.isNewIteration(iteration: Iteration): Boolean =
      source.lineNumber() <= iteration.startLineNumber

   private val KotestTestCase.scenario: Descriptor?
      get() = descriptor.parents().firstOrNull { it is TestDescriptor }

   private val KotestTestCase.parentUuid: String? get() = testUuidMap[descriptor.parent]

   private val KotestTestResult.needPassOnTop: Boolean
      get() = when (this) {
         is TestResult.Error -> true
         is TestResult.Failure -> true
         is TestResult.Ignored -> false
         is Success -> false
      }
}

private data class Iteration(val index: Int, val scenario: KotestTestCase, var startLineNumber: Int) {
   val isNotStarted get() = startLineNumber <= 0
   fun start(step: KotestTestCase) {
      startLineNumber = step.source.lineNumber()
   }

   companion object {
      fun next(scenario: KotestTestCase, previous: Iteration?) =
         Iteration(previous?.index?.inc() ?: 0, scenario, 0)
   }
}

private fun SourceRef.lineNumber(): Int = when (this) {
   is None, is ClassSource -> {
      System.err.println(
         "Cannot determine correct line number for DDT iteration! " +
            "You should add test sources to your project. " +
            "Or disable DATA_DRIVEN_SUPPORT > 'kotest.allure.data.driven=false'"
      )
      0
   }
   is ClassLineSource -> lineNumber ?: 0
}

private fun uuid(): String = UUID.randomUUID().toString()
