package com.magrathea.ppsimple.infra.adapters.outbound.utils

import com.magrathea.ppsimple.application.ports.outbound.ExternalIdUtils
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UUIDUtilsAdapter : ExternalIdUtils {
    override fun random(): UUID = UUID.randomUUID()

    override fun fromString(str: String): UUID = UUID.fromString(str)
}
