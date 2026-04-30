package io.kotest.extensions.allure.helper.meta

import io.kotest.core.descriptors.Descriptor
import io.kotest.core.spec.Spec
import io.qameta.allure.model.Label
import io.qameta.allure.model.Link
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportDescriptions.kDescription
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLabels.allureIdsFromTestName
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLabels.epicFromPkg
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLabels.taskLabelsFromTestName
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLabels.labelAnnotations
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLabels.severity
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.issues
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.taskLinks
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.taskLinksFromTestName
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.linkAnnotations
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.links
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.tmsLinks
import io.kotest.extensions.allure.helper.meta.AllureMetadataSupportLinks.tmsLinksFromTestName
import kotlin.reflect.KClass

internal class AllureMetadata(
    specClass: KClass<out Spec>? = null,
    description: Descriptor? = null
) {

    internal val allLabels: List<Label> = buildList {
        addAll(specClass.labelAnnotations)
        add(specClass.severity)
        add(specClass.epicFromPkg)
        addAll(description.allureIdsFromTestName)
        addAll(description.taskLabelsFromTestName)
    }.filterNotNull()

    internal val allLinks: List<Link> = buildList {
        addAll(specClass.linkAnnotations)
        addAll(specClass.issues)
        addAll(specClass.links)
        addAll(specClass.tmsLinks)
        addAll(specClass.taskLinks)
        addAll(description.taskLinksFromTestName)
        addAll(description.tmsLinksFromTestName)
    }.asSequence()
        .filterNot { it.url.isNullOrBlank() && it.name.isNullOrBlank() }
        .onEach { if (it.name.isNullOrBlank()) it.name = it.url }
        .distinctBy { it.url.orEmpty() + "|" + it.name.orEmpty() }
        .toList()

    internal val allDescriptions: String = buildList {
        add(specClass.kDescription)
    }.joinToString(separator = System.lineSeparator())
}
