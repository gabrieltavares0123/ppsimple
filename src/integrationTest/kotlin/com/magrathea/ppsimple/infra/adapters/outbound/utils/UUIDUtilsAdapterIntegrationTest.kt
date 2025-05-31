package com.magrathea.ppsimple.infra.adapters.outbound.utils

import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UUIDUtilsAdapterIntegrationTest {

    private val externalIdUtils: ExternalIdUtils = UUIDUtilsAdapter()

    @Test
    fun `should return a random UUID`() {
        val result = externalIdUtils.random()

        assertNotNull(result)
        assertInstanceOf<UUID>(result)
    }

    @Test
    fun `should return a new UUID from string`() {
        val str = "d15fd044-fbbd-4fb4-b085-e7245cdac7c1"
        val result = externalIdUtils.fromString(str)

        assertNotNull(result)
        assertInstanceOf<UUID>(result)
        assertEquals(actual = result.toString(), expected = str)
    }
}