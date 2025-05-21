package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Notification

interface NotificationMessagingConsumer {

    fun consume(notification: Notification)

}