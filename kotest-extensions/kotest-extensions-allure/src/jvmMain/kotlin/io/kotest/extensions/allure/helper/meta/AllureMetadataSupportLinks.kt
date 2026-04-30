package io.kotest.extensions.allure.helper.meta

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.spec.Spec
import io.qameta.allure.Issues
import io.qameta.allure.Links
import io.qameta.allure.TmsLinks
import io.qameta.allure.model.Link
import io.qameta.allure.util.AnnotationUtils.getLinks
import io.qameta.allure.util.ResultsUtils.createLink
import io.qameta.allure.util.ResultsUtils.createTmsLink
import io.kotest.extensions.allure.annotation.KTasks
import io.kotest.extensions.allure.api.KotestAllureConstant.TASK
import io.kotest.extensions.allure.api.KotestAllureConstant.TMS
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupport.findAll
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

internal object AllureMetadataSupportLinks {

    internal inline val KClass<out Spec>?.linkAnnotations: Collection<Link> get() = this?.let { getLinks(it.java) }.orEmpty()

    internal inline val KClass<out Spec>?.issues: Collection<Link> get() = this?.findAnnotation<Issues>()?.value?.map { createLink(it) } ?: emptySet()

    internal inline val KClass<out Spec>?.links: Collection<Link> get() = this?.findAnnotation<Links>()?.value?.map { createLink(it) } ?: emptySet()

    internal inline val KClass<out Spec>?.tmsLinks: Collection<Link> get() = this?.findAnnotation<TmsLinks>()?.value?.let { getLinks(it.toSet()) } ?: emptySet()

    internal inline val KClass<out Spec>?.taskLinks: Collection<Link> get() = this?.findAnnotation<KTasks>()?.value?.let { getLinks(it.toSet()) } ?: emptySet()

    internal inline val Descriptor?.taskLinksFromTestName: Collection<Link>
        get() = this?.run {
            id.value
                .findAll(TASK.PATTERN)
                .map { key -> createLink(key, key, null, TASK.LINK_TYPE) }
                .toList()
        }.orEmpty()

    internal inline val Descriptor?.tmsLinksFromTestName: Collection<Link>
        get() = this?.run {
            id.value
                .findAll(TMS.PATTERN)
                .map { key -> createTmsLink(key) }
                .toList()
        }.orEmpty()
}
