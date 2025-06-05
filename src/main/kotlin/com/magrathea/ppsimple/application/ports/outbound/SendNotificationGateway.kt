package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Notification

interface SendNotificationGateway {
    fun send(notification: Notification): Boolean
}
