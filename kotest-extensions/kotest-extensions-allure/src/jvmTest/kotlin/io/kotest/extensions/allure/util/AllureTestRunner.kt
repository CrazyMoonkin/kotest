package io.kotest.extensions.allure.util

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorId
import io.kotest.core.descriptors.toDescriptor
import io.kotest.core.names.TestName
import io.kotest.core.spec.DslDrivenSpec
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CollectingTestEngineListener
import io.kotest.engine.test.TestResult
import io.qameta.allure.AllureLifecycle
import io.kotest.extensions.allure.KotestAllureListener
import io.kotest.extensions.allure.api.KotestAllureExecution
import io.kotest.extensions.allure.helper.InternalExecutionModel
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * DSL for describing a fake test execution tree to be fed to [AllureTestRunner.runSpec].
 */
data class FakeTest(
   val name: String,
   val result: TestResult,
   val children: List<FakeTest> = emptyList(),
   val type: TestType = if (children.isEmpty()) TestType.Test else TestType.Container,
)

fun passed(name: String, vararg children: FakeTest): FakeTest =
   FakeTest(name, TestResult.Success(0.milliseconds), children.toList(),
      if (children.isEmpty()) TestType.Test else TestType.Container)

fun failed(name: String, cause: AssertionError = AssertionError("Expected failure")): FakeTest =
   FakeTest(name, TestResult.Failure(0.milliseconds, cause))

fun broken(name: String, cause: Throwable = RuntimeException("Unexpected error")): FakeTest =
   FakeTest(name, TestResult.Error(0.milliseconds, cause))

/**
 * Runs Kotest specs through [KotestAllureListener] with an in-memory [AllureResultsWriterStub].
 *
 * Two modes:
 * - [runSpec] with no [FakeTest]s — executes the actual spec via [TestEngineLauncher],
 *   so all tests defined in the spec body run for real.
 * - [runSpec] with [FakeTest]s — injects a synthetic test tree without running test bodies,
 *   useful for testing listener behavior in isolation.
 *
 * In both modes the outer Kotest run is unaffected: [KotestAllureExecution.allure] and
 * [InternalExecutionModel] state are saved before and fully restored after the inner run.
 */
object AllureTestRunner {

   /**
    * Runs the actual tests defined inside [klass] via [TestEngineLauncher].
    * All test bodies are executed. Results are captured in the returned stub.
    */
   suspend fun runSpec(klass: KClass<out Spec>): AllureResultsWriterStub =
      withStub {
         TestEngineLauncher()
            .addExtension(KotestAllureListener)
            .withSpecRefs(SpecRef.Reference(klass))
            .withListener(CollectingTestEngineListener())
            .withoutEnvFilters()
            .execute()
      }

   /**
    * Runs a synthetic test tree (no real test bodies) for [klass] through [KotestAllureListener].
    * Useful for unit-testing listener behavior without depending on real spec logic.
    */
   suspend fun runSpec(klass: KClass<out Spec>, vararg tests: FakeTest): AllureResultsWriterStub =
      withStub {
         KotestAllureListener.beforeProject()
         KotestAllureListener.prepareSpec(klass)

         val specDescriptor = klass.toDescriptor()
         val spec = runCatching { klass.java.getDeclaredConstructor().newInstance() as Spec }
            .getOrElse { object : DslDrivenSpec() {} }
         val resultsMap = mutableMapOf<TestCase, TestResult>()

         tests.forEach { fake -> executeTest(specDescriptor, spec, fake, resultsMap) }

         KotestAllureListener.finalizeSpec(klass, resultsMap)
         KotestAllureListener.afterProject()
      }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private suspend fun withStub(block: suspend () -> Unit): AllureResultsWriterStub {
      val stub = AllureResultsWriterStub()
      val outerLifecycle = KotestAllureExecution.allure
      val outerSnapshot = InternalExecutionModel.snapshotTestUuidMap()
      try {
         KotestAllureExecution.allure = AllureLifecycle(stub)
         InternalExecutionModel.resetForTest()
         block()
      } finally {
         KotestAllureExecution.allure = outerLifecycle
         InternalExecutionModel.restoreTestUuidMap(outerSnapshot)
      }
      return stub
   }

   private suspend fun executeTest(
      parentDescriptor: Descriptor,
      spec: Spec,
      fake: FakeTest,
      resultsMap: MutableMap<TestCase, TestResult>,
   ) {
      val descriptor = Descriptor.TestDescriptor(parentDescriptor, DescriptorId(fake.name))
      val testCase = TestCase(
         descriptor = descriptor,
         name = TestName(fake.name, false, false, fake.name, null, false),
         spec = spec,
         test = {},
         type = fake.type,
      )
      KotestAllureListener.beforeAny(testCase)
      fake.children.forEach { child -> executeTest(descriptor, spec, child, resultsMap) }
      KotestAllureListener.afterAny(testCase, fake.result)
      resultsMap[testCase] = fake.result
   }
}
