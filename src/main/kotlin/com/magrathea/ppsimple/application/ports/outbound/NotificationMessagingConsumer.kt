package com.magrathea.ppsimple.application.ports.outbound

import com.magrathea.ppsimple.domain.Notification
import java.util.concurrent.CountDownLatch

interface NotificationMessagingConsumer {
    fun consume(notification: Notification)

    fun getLatch(): CountDownLatch

    fun resetLatch()

    fun getLastPayload(): String
}
