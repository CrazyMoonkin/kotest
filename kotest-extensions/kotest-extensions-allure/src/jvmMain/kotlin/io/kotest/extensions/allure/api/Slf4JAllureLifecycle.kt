package io.kotest.extensions.allure.api

import io.qameta.allure.AllureLifecycle
import io.qameta.allure.model.StepResult
import org.slf4j.Logger
import io.kotest.extensions.allure.api.KotestAllureConstant.Var
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

/**
 * Decorate [startStep] and [addAttachment] with SLF4J [Logger]. Using by default.
 * You may set custom lifecycle via env/sys var [Var.ALLURE_LIFECYCLE_CLASS].
 */
open class Slf4JAllureLifecycle(private val logger: Logger) : AllureLifecycle() {

   override fun startStep(parentUuid: String?, uuid: String?, result: StepResult?) =
      super.startStep(parentUuid, uuid, result).also { logger.info("STEP: ${result.log}") }

   override fun addAttachment(name: String?, type: String?, fileExtension: String?, stream: InputStream?) {
      if (!logger.isDebugEnabled) {
         super.addAttachment(name, type, fileExtension, stream)
         return
      }
      // Buffer the payload so the original stream can be re-served while we also peek into it for the debug log.
      val bytes = stream.use { it?.readBytesOrError() ?: ByteArray(0) }
      super.addAttachment(name, type, fileExtension, bytes.inputStream())
      logger.debug("ATTACHMENT: $name $type $fileExtension\n{}", String(bytes, UTF_8).take(2000))
   }

   /////////////////
   //// PRIVATE ////
   /////////////////

   private fun InputStream.readBytesOrError(): ByteArray =
      runCatching { readBytes() }.getOrElse { (it.localizedMessage ?: "").toByteArray(UTF_8) }

   private val StepResult?.log
      get() = when (this) {
         null -> ""
         else -> "${name ?: ""} ${status ?: ""}" + (
                 params.entries
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString("\n", "\n") { "${it.key}: ${it.value}" } ?: ""
                 )
      }

   private val StepResult?.params
      get() = mapOf(
         "description" to this?.description,
         "parameters" to this?.parameters?.joinToString(prefix = "[", postfix = "]") { "${it.name}=${it.value}" },
         "attachments" to this?.attachments?.joinToString(
            prefix = "[",
            postfix = "]"
         ) { "${it.name}=${it.type}(${it.source.length})" }
      ).filterNot { it.value.isNullOrBlank() || it.value == "[]" }
}
