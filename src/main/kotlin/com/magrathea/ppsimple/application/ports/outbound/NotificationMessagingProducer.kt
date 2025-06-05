package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Notification

interface NotificationMessagingProducer {
    fun produce(notification: Notification)
}
