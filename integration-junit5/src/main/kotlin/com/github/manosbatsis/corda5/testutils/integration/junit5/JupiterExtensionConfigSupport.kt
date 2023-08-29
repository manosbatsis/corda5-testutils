package com.github.manosbatsis.corda5.testutils.integration.junit5

import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.Preconditions
import java.lang.reflect.Field

/**
 * Helper for extensions that build their configuration
 * based on current testsuite fields.
 */
interface JupiterExtensionConfigSupport {

    fun isOfType(
        field: Field,
        fieldType: Class<*>
    ): Boolean {
        return if (fieldType.isAssignableFrom(field.type)) true else false
    }

    fun <T> getFieldValue(
        testInstance: Any?,
        field: Field
    ): T? = try {
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        Preconditions.notNull(field.get(testInstance) as T, "Container " + field.name + " needs to be initialized")
    } catch (e: IllegalAccessException) {
        throw RuntimeException("Can not access container defined in field " + field.name)
    }

    fun <T : Class<*>, F> findFieldValue(
        testClass: T,
        fieldClass: Class<F>,
        instanceFields: Boolean = false
    ): F? = testClass.declaredFields
        .filter { isOfType(it, fieldClass) }
        .mapNotNull { f: Field ->
            try {
                getFieldValue<F>(null, f)
            } catch (e: Throwable) {
                null
            }
        }
        .singleOrNull()

    fun getRequiredTestClass(context: ExtensionContext): Class<*> =
        context.testClass.orElseThrow {
            ExtensionConfigurationException("Extension is only supported for classes.")
        }
}
