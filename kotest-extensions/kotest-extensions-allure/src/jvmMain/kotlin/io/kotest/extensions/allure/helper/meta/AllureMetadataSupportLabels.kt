package io.kotest.extensions.allure.helper.meta

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.spec.Spec
import io.qameta.allure.Epic
import io.qameta.allure.Severity
import io.qameta.allure.model.Label
import io.qameta.allure.util.AnnotationUtils
import io.qameta.allure.util.ResultsUtils.ALLURE_ID_LABEL_NAME
import io.qameta.allure.util.ResultsUtils.createEpicLabel
import io.qameta.allure.util.ResultsUtils.createLabel
import io.qameta.allure.util.ResultsUtils.createSeverityLabel
import io.kotest.extensions.allure.api.KotestAllureConstant
import io.kotest.extensions.allure.api.KotestAllureConstant.Task
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupport.findAll
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

internal object AllureMetadataSupportLabels {

   internal inline val KClass<out Spec>?.labelAnnotations: Set<Label>
      get() = this?.let { AnnotationUtils.getLabels(it.java) }.orEmpty()

   internal inline val KClass<out Spec>?.epicFromPkg: Label?
      get() = this?.run {
         if (hasAnnotation<Epic>()) null else createEpicLabel(this.java.`package`.name)
      }

   internal inline val KClass<out Spec>?.severity: Label?
      get() = this?.findAnnotation<Severity>()?.let { createSeverityLabel(it.value) }

   internal inline val Descriptor?.allureIdsFromTestName: Collection<Label>
      get() = this?.run {
         id.value
            .findAll(KotestAllureConstant.AllureId.PATTERN)
            .map { key -> createLabel(ALLURE_ID_LABEL_NAME, key) }
            .toList()
      }.orEmpty()

   internal inline val Descriptor?.taskLabelsFromTestName: Collection<Label>
      get() = this?.run {
         id.value
            .findAll(Task.PATTERN)
            .map { key -> createLabel(Task.LABEL_NAME, key) }
            .toList()
      }.orEmpty()
}
