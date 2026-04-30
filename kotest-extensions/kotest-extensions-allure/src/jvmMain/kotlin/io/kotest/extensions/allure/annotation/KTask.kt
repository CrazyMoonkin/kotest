package io.kotest.extensions.allure.annotation

import io.qameta.allure.LabelAnnotation
import io.qameta.allure.LinkAnnotation
import io.kotest.extensions.allure.api.KotestAllureConstant.TASK.LABEL_NAME
import io.kotest.extensions.allure.api.KotestAllureConstant.TASK.LINK_TYPE
import java.lang.annotation.Inherited

@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@LinkAnnotation(type = LINK_TYPE)
@LabelAnnotation(name = LABEL_NAME)
@Repeatable
annotation class KTask(val value: String)
