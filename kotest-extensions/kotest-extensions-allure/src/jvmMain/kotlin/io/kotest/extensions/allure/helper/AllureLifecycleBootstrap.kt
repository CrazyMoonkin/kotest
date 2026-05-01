package io.kotest.extensions.allure.helper

import io.qameta.allure.Allure
import io.qameta.allure.AllureLifecycle
import io.kotest.extensions.allure.api.KotestAllureConstant.Var
import io.kotest.extensions.allure.api.Slf4JAllureLifecycle
import io.kotest.extensions.allure.helper.AllureConfig.prop
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Builds the [AllureLifecycle] from configuration and exposes an explicit, idempotent
 * [clearPreviousResults] hook.
 *
 * The lifecycle is created lazily — accessing [lifecycle] is the first point of contact;
 * [clearPreviousResults] is invoked once per JVM by [io.kotest.extensions.allure.KotestAllureListener.beforeProject],
 * which keeps directory deletion out of object-initialization side effects.
 */
internal object AllureLifecycleBootstrap {

   private val log = logger<AllureLifecycleBootstrap>()
   private val cleared = AtomicBoolean(false)

   private val resultsDir: String by lazy {
      val dir = Var.ALLURE_RESULTS_DIR.prop("build/allure-results")
      System.setProperty(Var.ALLURE_RESULTS_DIR, dir)
      dir
   }

   internal val lifecycle: AllureLifecycle by lazy {
      // Touch resultsDir so the system property is set before the lifecycle starts writing.
      resultsDir
      val slf4jEnabled = Var.ALLURE_SLF4J_LOG.prop(true)
      val allureClassRef: String = Var.ALLURE_LIFECYCLE_CLASS.prop("")

      val instance = if (allureClassRef.isNotBlank())
         runCatching { Class.forName(allureClassRef).getConstructor().newInstance() as AllureLifecycle }
            .onFailure { throw RuntimeException("Cannot create AllureLifecycle from class '$allureClassRef'", it) }
            .getOrThrow()
      else if (slf4jEnabled) Slf4JAllureLifecycle(log)
      else AllureLifecycle()

      Allure.setLifecycle(instance)
      instance
   }

   /**
    * Removes the configured Allure results directory once per JVM, if [Var.CLEAR_ALLURE_RESULTS_DIR] is enabled.
    * Subsequent calls are no-ops, matching the original behaviour where cleanup ran exactly once at class load.
    */
   internal fun clearPreviousResults() {
      if (!cleared.compareAndSet(false, true)) return
      if (!Var.CLEAR_ALLURE_RESULTS_DIR.prop(true)) return
      val dir = File(resultsDir)
      if (dir.exists() && dir.isDirectory) {
         runCatching { dir.deleteRecursively() }.onFailure { log.error("Cannot delete '$dir'", it) }
      }
   }
}
