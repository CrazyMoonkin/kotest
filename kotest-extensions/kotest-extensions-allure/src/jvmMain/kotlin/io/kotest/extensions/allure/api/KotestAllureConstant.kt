package io.kotest.extensions.allure.api

import io.qameta.allure.AllureLifecycle
import io.kotest.extensions.allure.api.KotestAllureConstant.Var.ALLURE_RESULTS_DIR
import io.kotest.extensions.allure.helper.AllureConfig.prop

/**
 * Constants API.
 */
@Suppress("MemberVisibilityCanBePrivate")
object KotestAllureConstant {

   /**
    * @see Var.ALLURE_TASK_PATTERN
    */
   object Task {

      const val LINK_TYPE = "task"

      const val LABEL_NAME = "task"

      internal const val PATTERN_DEFAULT = "\\[([a-zA-Z]+-\\d+)]"

      /**
       * Default Task issue in Kotest Test name.
       * See [Var.ALLURE_TASK_PATTERN]
       */
      val PATTERN: Regex = Var.ALLURE_TASK_PATTERN.prop(PATTERN_DEFAULT).toRegex()
   }

   /**
    * @see Var.ALLURE_TMS_PATTERN
    */
   object Tms {

      internal const val PATTERN_DEFAULT = "\\(([a-zA-Z]+-\\d+)\\)"

      /**
       * Default TMS Key in Kotest Test name.
       * See [Var.ALLURE_TMS_PATTERN]
       */
      val PATTERN: Regex = Var.ALLURE_TMS_PATTERN.prop(PATTERN_DEFAULT).toRegex()
   }

   /**
    * @see Var.ALLURE_ID_PATTERN
    */
   object AllureId {

      internal const val PATTERN_DEFAULT = "#(\\d+)"

      /**
       * Default AllureId (TestOps) in Kotest Test name.
       * See [Var.ALLURE_ID_PATTERN]
       */
      val PATTERN: Regex = Var.ALLURE_ID_PATTERN.prop(PATTERN_DEFAULT).toRegex()
   }

   /**
    * System or Environment variable name to configure the extension.
    * First try System then Environment.
    */
   object Var {

      /**
       * Set [AllureLifecycle] full class name.
       * Default = [io.kotest.extensions.allure.api.Slf4JAllureLifecycle].
       */
      const val ALLURE_LIFECYCLE_CLASS = "allure.lifecycle.class"

      /**
       * Set task ticket pattern in Kotest Test name.
       * Default = '\[([a-zA-Z]+-\d+)]' like `[KT-100]`
       */
      const val ALLURE_TASK_PATTERN = "allure.task.pattern"

      /**
       * Set allure id (TestOps) pattern in Kotest Test name.
       * Default = '#(\d+)' like `#777`
       */
      const val ALLURE_ID_PATTERN = "allure.id.pattern"

      /**
       * Set TMS key pattern in Kotest Test name.
       * Default = '\(([a-zA-Z]+-\d+)\)' like `(TC-100)`
       */
      const val ALLURE_TMS_PATTERN = "allure.tms.pattern"

      /**
       * Result directory.
       * Default = 'build/allure-results'
       */
      const val ALLURE_RESULTS_DIR = "allure.results.directory"

      /**
       * Clear [ALLURE_RESULTS_DIR] before execution.
       * Default = true - clear
       */
      const val CLEAR_ALLURE_RESULTS_DIR = "allure.results.directory.clear"

      /**
       * Use extended [io.kotest.extensions.allure.api.Slf4JAllureLifecycle] or [AllureLifecycle].
       * Default = true - use [io.kotest.extensions.allure.api.Slf4JAllureLifecycle]
       */
      const val ALLURE_SLF4J_LOG = "allure.slf4j.log"

      /**
       * Skip following tests after any error in scenario. By default raw Kotest doesn't skip any tests or steps.
       *
       * !!!But this extension will override source behavior!!!
       *
       * Default = true - skip.
       */
      const val SKIP_ON_FAIL = "kotest.allure.skip.on.fail"

      /**
       * Create new Allure test case on each new iteration in Data Driven tests or Property Testing.
       *
       * But supported only using single data driven block like 'forAll' on entire content of test container.
       *
       * Default = true - enable. (disable it if face unexpected behavior)
       */
      const val DATA_DRIVEN_SUPPORT = "kotest.allure.data.driven"

      /**
       * Clean all allure metadata (issue, tms, allureId) from test case name.
       *
       * `"My test name [JIRA-1](TMS-2)#3 - { ... }` - will be removed by patterns.
       *
       * Default = true - enable. (disable it if face unexpected test cases name after cleaning up)
       *
       *  @see Var.ALLURE_ID_PATTERN
       *  @see Var.ALLURE_TMS_PATTERN
       *  @see Var.ALLURE_TASK_PATTERN
       */
      const val TEST_NAME_AUTO_CLEAN_UP = "kotest.allure.meta.cleanup"
   }
}
