package com.salesiq.demoapp.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.salesiq.demoapp.BuildConfig
import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.zoho.salesiqembed.ZohoSalesIQ

/**
 * Push handling is automatic: SalesIQ notifications are forwarded to the SDK and
 * new tokens are re-registered. There is no register-push button in the UI.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        // Check whether this incoming push actually belongs to SalesIQ before handling it.
        if (ZohoSalesIQ.Notification.isZohoSalesIQNotification(data)) {
            // Parse the push into a typed payload (getPayload) so it surfaces in the
            // Events console, then hand it to the SDK to render.
            ZohoSalesIQ.Notification.getPayload(data) { result ->
                EventStore.push(
                    "Push payload parsed",
                    "{\"parsed\":${result.isSuccess}}",
                    EventSource.Notification,
                )
            }
            // Let the SDK build and display the notification from the push data.
            ZohoSalesIQ.Notification.handle(applicationContext, data)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New token received")
        // Re-register the refreshed FCM token so push keeps working.
        ZohoSalesIQ.Notification.enablePush(token, BuildConfig.DEBUG)
    }

    private companion object {
        const val TAG = "mobilisten:firebase"
    }
}
