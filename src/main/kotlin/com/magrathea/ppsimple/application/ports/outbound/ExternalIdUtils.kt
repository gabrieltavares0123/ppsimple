package com.magrathea.ppsimple.application.ports.outbound

import java.util.UUID

interface ExternalIdUtils {
    fun random(): UUID
    fun fromString(str: String): UUID
}