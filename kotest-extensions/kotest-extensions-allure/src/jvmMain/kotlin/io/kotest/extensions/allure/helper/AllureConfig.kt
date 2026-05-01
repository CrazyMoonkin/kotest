package io.kotest.extensions.allure.helper

import java.lang.System.getProperty
import java.lang.System.getenv
import java.math.BigDecimal
import java.util.Locale
import kotlin.reflect.full.isSubclassOf

/**
 * Reads configuration values from system properties and environment variables.
 *
 * Each lookup tries the original key, the upper/lower-case variants, the same with `.` replaced by `_`,
 * across both `System.getProperty` and `System.getenv` to be tolerant to different launch environments.
 */
internal object AllureConfig {

   internal inline fun <reified T> String.prop(default: T): T = prop() ?: default

   internal inline fun <reified T> String.prop(): T? = (
      getProperty(this)
         ?: getProperty(uppercase(Locale.getDefault()))
         ?: getProperty(lowercase(Locale.getDefault()))
         ?: getenv(this)
         ?: getenv(uppercase(Locale.getDefault()))
         ?: getenv(lowercase(Locale.getDefault()))
         ?: getenv(replace(".", "_"))
         ?: getenv(replace(".", "_").uppercase(Locale.getDefault()))
         ?: getenv(replace(".", "_").lowercase(Locale.getDefault()))
      )
      .smartCast()

   internal inline fun <reified T> Any?.smartCast(): T? =
      when {
         this == null -> null
         this is T -> this
         T::class.isSubclassOf(Boolean::class) -> toString().toBoolean() as T
         T::class.isSubclassOf(BigDecimal::class) -> toString().toBigDecimalOrNull() as T
         T::class.isSubclassOf(Double::class) -> toString().toDoubleOrNull() as T
         T::class.isSubclassOf(Int::class) -> toString().toIntOrNull() as T
         T::class.isSubclassOf(Long::class) -> toString().toLongOrNull() as T
         else -> this as T
      }
}