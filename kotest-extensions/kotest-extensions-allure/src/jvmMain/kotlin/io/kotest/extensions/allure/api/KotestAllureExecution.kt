package io.kotest.extensions.allure.api

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.qameta.allure.Allure
import io.qameta.allure.AllureLifecycle
import io.qameta.allure.model.FixtureResult
import io.qameta.allure.model.Status
import io.kotest.extensions.allure.KotestAllureListener
import io.kotest.extensions.allure.api.KotestAllureConstant.AllureId
import io.kotest.extensions.allure.api.KotestAllureConstant.Task
import io.kotest.extensions.allure.api.KotestAllureConstant.Tms
import io.kotest.extensions.allure.api.KotestAllureConstant.Var
import io.kotest.extensions.allure.api.KotestAllureExecution.containerUuid
import io.kotest.extensions.allure.api.KotestAllureExecution.projectUuid
import io.kotest.extensions.allure.api.KotestAllureExecution.setUpFixture
import io.kotest.extensions.allure.helper.InternalUtil.logger
import io.kotest.extensions.allure.helper.InternalUtil.prop
import io.kotest.extensions.allure.helper.InternalUtil.safeFileName
import java.io.File
import java.util.UUID
import kotlin.reflect.KClass

/**
 * User API to get access to allure listener execution.
 * For example: create fixture [setUpFixture] or obtain actual [Spec] uuid in Allure Storage - [containerUuid]
 * or project execution root uuid - [projectUuid]
 */
object KotestAllureExecution {
   private val log = logger<KotestAllureExecution>()

   /**
    * Get current [AllureLifecycle] or extended version for example [Slf4JAllureLifecycle]
    */
   var allure: AllureLifecycle = initAllureLifecycle()
      internal set

   /**
    * Get container uuid of entire Execution.
    *
    * See [setUpFixture]
    * See [tearDownFixture]
    * See [executionStartCallback]
    */
   val projectUuid = KotestAllureListener.hashCode().toString()

   /**
    * Use to add project level Fixture.
    * Not thread safe. Not final variable.
    *
    * See [setUpFixture]
    * See [tearDownFixture]
    */
   var executionStartCallback: (projectUuid: String) -> Unit = { }

   /**
    * Get container uuid of [Spec]
    *
    * See [setUpFixture]
    * See [tearDownFixture]
    */
   val Spec.containerUuid get() = this::class.containerUuid

   /**
    * Get container uuid of [Spec] class
    *
    * See [setUpFixture]
    * See [tearDownFixture]
    */
   val KClass<*>.containerUuid get() = qualifiedName.safeFileName

   /**
    * The longest name for [TestCase.descriptor]
    */
   fun Descriptor.bestName() = this.ids().joinToString("_") { it.value }

   /**
    * Create Set Up Fixture for [Spec]
    */
   fun Spec.setUpFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ) = this::class.setUpFixture(name, atomic, fixtureResult)

   /**
    * Create Set Up Fixture for [Spec]
    */
   fun KClass<out Spec>.setUpFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ): String = containerUuid.setUpFixture(name, atomic, fixtureResult)

   /**
    * Create Set Up Fixture by container uuid.
    *
    * See [projectUuid]
    * See [executionStartCallback]
    * See [containerUuid]
    */
   fun String.setUpFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ): String {
      val fixture = FixtureResult()
         .also { it.name = name; it.status = Status.PASSED }
         .also(fixtureResult)

      val uuid = UUID.randomUUID().toString()
      allure.startPrepareFixture(this, uuid, fixture)
      if (atomic) allure.stopFixture(uuid)
      return uuid
   }

   /**
    * Create Tear Down Fixture for [Spec]
    */
   fun Spec.tearDownFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ) = this::class.tearDownFixture(name, atomic, fixtureResult)

   /**
    * Create Tear Down Fixture for [Spec]
    */
   fun KClass<out Spec>.tearDownFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ): String = containerUuid.tearDownFixture(name, atomic, fixtureResult)

   /**
    * Create Tear Down Fixture by container uuid.
    *
    * See [projectUuid]
    * See [executionStartCallback]
    * See [containerUuid]
    */
   fun String.tearDownFixture(
      name: String,
      atomic: Boolean = true,
      fixtureResult: FixtureResult.() -> Unit = {}
   ): String {
      val fixture = FixtureResult()
         .also { it.name = name; it.status = Status.PASSED }
         .also(fixtureResult)

      val uuid = UUID.randomUUID().toString()
      allure.startTearDownFixture(this, uuid, fixture)
      if (atomic) allure.stopFixture(uuid)
      return uuid
   }

   /**
    * Add TMS key to test name.
    * @see KotestAllureConstant.Tms
    */
   fun String.tms(tmsKey: String) = "$this($tmsKey)"
      .shouldBeDefaultPattern(Tms.PATTERN.pattern, Tms.PATTERN_DEFAULT, "tms", "allure.tms.pattern")

   /**
    * Add ISSUE key to test name.
    * @see KotestAllureConstant.Task
    */
   fun String.task(issueKey: String) = "$this[$issueKey]"
      .shouldBeDefaultPattern(Task.PATTERN.pattern, Task.PATTERN_DEFAULT, "issue", "allure.task.pattern")

   /**
    * Add AllureID (TestOps) key to test name.
    * @see KotestAllureConstant.AllureId
    */
   fun String.allureId(allureId: String) = "$this#$allureId"
      .shouldBeDefaultPattern(AllureId.PATTERN.pattern, AllureId.PATTERN_DEFAULT, "allureId", "allure.id.pattern")

   /////////////////
   //// PRIVATE ////
   /////////////////

   private fun String.shouldBeDefaultPattern(
      yourPattern: String,
      defaultPattern: String,
      funName: String,
      varName: String,
   ) = if (yourPattern != defaultPattern) error(yourPattern, defaultPattern, funName, varName) else this

   private fun error(yourPattern: String, defaultPattern: String, funName: String, varName: String): Nothing =
      throw UnsupportedOperationException(
         "You must use this function '$funName' only with default patter '$varName=$defaultPattern'. " +
            "But you have changed default pattern '$varName=$yourPattern' " +
            "You should implement your own 'fun String.${funName}My(key: String)' extension function"
      )

   private fun initAllureLifecycle(): AllureLifecycle {
      val resultDir = Var.ALLURE_RESULTS_DIR.prop("build/allure-results")
      val slf4jEnabled = Var.ALLURE_SLF4J_LOG.prop(true)
      val allureClassRef: String = Var.ALLURE_LIFECYCLE_CLASS.prop("")

      System.setProperty(Var.ALLURE_RESULTS_DIR, resultDir)
      clearPreviousResults(File(resultDir))

      return (if (allureClassRef.isNotBlank())
         runCatching { Class.forName(allureClassRef).getConstructor().newInstance() as AllureLifecycle }
            .onFailure { throw RuntimeException("Cannot create AllureLifecycle from class '$allureClassRef'", it) }
            .getOrThrow()
      else (if (slf4jEnabled) Slf4JAllureLifecycle(log) else AllureLifecycle())).also { Allure.setLifecycle(it) }
   }

   private fun clearPreviousResults(dir: File) {
      if (Var.CLEAR_ALLURE_RESULTS_DIR.prop(true)) {
         if (dir.exists() && dir.isDirectory) {
            runCatching { dir.deleteRecursively() }.getOrElse { log.error("Cannot delete '$dir'", it) }
         }
      }
   }
}
