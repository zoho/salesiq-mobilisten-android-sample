package com.salesiq.demoapp.sdk

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.salesiq.demoapp.state.AuthMode
import com.salesiq.demoapp.state.ConfigStore
import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.salesiq.demoapp.state.SDKInitStatus
import com.salesiq.demoapp.state.SettingsStore
import com.salesiq.demoapp.state.hasPlaceholderKeys
import com.salesiq.demoapp.ui.screens.jsonOf
import com.zoho.commons.ChatComponent
import com.zoho.livechat.android.NotificationListener
import com.zoho.livechat.android.SIQVisitor
import com.zoho.livechat.android.SalesIQCustomAction
import com.zoho.livechat.android.VisitorChat
import com.zoho.livechat.android.listeners.SalesIQActionListener
import com.zoho.livechat.android.listeners.SalesIQChatListener
import com.zoho.livechat.android.listeners.SalesIQCustomActionListener
import com.zoho.livechat.android.listeners.SalesIQListener
import com.zoho.livechat.android.modules.authentication.ui.models.SalesIQJWTAuth
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource
import com.zoho.livechat.android.modules.knowledgebase.ui.listeners.SalesIQKnowledgeBaseListener
import com.zoho.livechat.android.modules.notifications.sdk.entities.SalesIQNotificationPayload
import com.zoho.salesiq.mobilisten.calls.apis.ZohoSalesIQCalls
import com.zoho.salesiq.mobilisten.calls.apis.interfaces.SalesIQCallsListener
import com.zoho.salesiqembed.ZohoSalesIQ
import com.zoho.salesiqembed.models.SalesIQConfiguration

/**
 * Centralized SDK integration for the sample app.
 *
 * - Registers ALL listeners so the Events Console gets a live cross-module feed.
 * - Initializes the SDK automatically at launch with the stored keys.
 * - Registers push automatically (no init/register-push buttons in the UI).
 */
object SalesIQManager {
    private const val TAG = "mobilisten:manager"

    /** Wire every listener before init so no early event is missed. */
    fun registerListeners() {
        // Global SDK listener: fires for app-wide events like support open/close and triggers.
        ZohoSalesIQ.setListener(object : SalesIQListener {
            override fun handleSupportOpen() {
                EventStore.push("Support open", "{}", EventSource.System)
            }

            override fun handleSupportClose() {
                EventStore.push("Support close", "{}", EventSource.System)
            }

            override fun handleOperatorsOnline() {
                EventStore.push("Operators online", "{}", EventSource.System)
            }

            override fun handleOperatorsOffline() {
                EventStore.push("Operators offline", "{}", EventSource.System)
            }

            override fun handleIPBlock() {
                EventStore.push("IP blocked", "{}", EventSource.System)
            }

            override fun handleTrigger(triggerName: String?, visitor: SIQVisitor?) {
                EventStore.push("Trigger", jsonOf("trigger" to triggerName), EventSource.System)
            }

            override fun handleCustomLauncherVisibility(visible: Boolean) {
                EventStore.push("Custom launcher visibility", jsonOf("visible" to visible), EventSource.Launcher)
            }

            override fun handleBotTrigger() {
                EventStore.push("Bot trigger", "{}", EventSource.System)
            }
        })

        // Chat listener: fires for chat lifecycle events (opened, closed, rated, missed, etc.).
        ZohoSalesIQ.Chat.setListener(object : SalesIQChatListener {
            override fun handleChatViewOpen(chatId: String?) {
                EventStore.push("Chat view open", jsonOf("chatId" to chatId), EventSource.Chat)
            }

            override fun handleChatViewClose(chatId: String?) {
                EventStore.push("Chat view close", jsonOf("chatId" to chatId), EventSource.Chat)
            }

            override fun handleChatOpened(visitorChat: VisitorChat?) {
                EventStore.push("Chat opened", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleChatClosed(visitorChat: VisitorChat?) {
                EventStore.push("Chat closed", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleChatAttended(visitorChat: VisitorChat?) {
                EventStore.push("Chat attended", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleChatMissed(visitorChat: VisitorChat?) {
                EventStore.push("Chat missed", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleChatReOpened(visitorChat: VisitorChat?) {
                EventStore.push("Chat reopened", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleRating(visitorChat: VisitorChat?) {
                EventStore.push("Rating submitted", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleFeedback(visitorChat: VisitorChat?) {
                EventStore.push("Feedback submitted", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleQueuePositionChange(visitorChat: VisitorChat?) {
                EventStore.push("Queue position change", jsonOf("chatId" to visitorChat?.chatID), EventSource.Chat)
            }

            override fun handleUri(uri: Uri?, visitorChat: VisitorChat?): Boolean {
                EventStore.push("URI invoked", jsonOf("uri" to uri), EventSource.Chat)
                return false
            }

            override fun onChatExpired(chat: VisitorChat?) {
                EventStore.push("Chat expired", jsonOf("chatId" to chat?.chatID), EventSource.Chat)
            }
        })

        // Notification listener: fires when the unread badge count changes or a push is tapped.
        ZohoSalesIQ.Notification.setListener(object : NotificationListener {
            override fun onBadgeChange(count: Int) {
                EventStore.push("Badge change", jsonOf("count" to count), EventSource.Notification)
            }

            override fun onClick(context: Context?, payload: SalesIQNotificationPayload) {
                EventStore.push("Notification click", jsonOf("payloadType" to payload.javaClass.simpleName), EventSource.Notification)
            }
        })

        // Knowledge Base listener: fires when a help article/FAQ is opened, closed, liked or disliked.
        ZohoSalesIQ.KnowledgeBase.setListener(object : SalesIQKnowledgeBaseListener {
            override fun handleResourceOpened(resourceType: ZohoSalesIQ.ResourceType, resource: Resource?) {
                EventStore.push("Resource opened", jsonOf("title" to resource?.title), EventSource.KnowledgeBase)
            }

            override fun handleResourceClosed(resourceType: ZohoSalesIQ.ResourceType, resource: Resource?) {
                EventStore.push("Resource closed", jsonOf("title" to resource?.title), EventSource.KnowledgeBase)
            }

            override fun handleResourceLiked(resourceType: ZohoSalesIQ.ResourceType, resource: Resource?) {
                EventStore.push("Resource liked", jsonOf("title" to resource?.title), EventSource.KnowledgeBase)
            }

            override fun handleResourceDisliked(resourceType: ZohoSalesIQ.ResourceType, resource: Resource?) {
                EventStore.push("Resource disliked", jsonOf("title" to resource?.title), EventSource.KnowledgeBase)
            }
        })

        // Chat-actions listener: fires when a visitor triggers a custom chat action registered via
        // ZohoSalesIQ.ChatActions.register(...). The sample records it and auto-completes so the bot
        // flow proceeds.
        ZohoSalesIQ.ChatActions.setListener(object : SalesIQActionListener {
            override fun handleCustomAction(
                customAction: SalesIQCustomAction,
                listener: SalesIQCustomActionListener,
            ) {
                EventStore.push(
                    "Chat action triggered",
                    jsonOf(
                        "name" to customAction.name,
                        "label" to customAction.label,
                        "elementID" to customAction.elementID,
                    ),
                    EventSource.Chat,
                )
                listener.onSuccess("Handled by sample")
            }
        })

        // Calls listener: fires when an audio call changes state or its queue position moves.
        ZohoSalesIQCalls.addListener(object : SalesIQCallsListener {
            override fun onCallStateChanged(callState: ZohoSalesIQCalls.SalesIQCallState) {
                EventStore.push("Call state changed", jsonOf("status" to callState.status), EventSource.Calls)
            }

            override fun onQueuePositionChanged(conversationId: String, position: Int) {
                EventStore.push("Call queue position", jsonOf("position" to position), EventSource.Calls)
            }
        })
    }

    /** Initialize the SDK with the stored keys. Called from Application.onCreate. */
    fun initialize(application: Application, appKey: String, accessKey: String) {
        if (hasPlaceholderKeys(appKey, accessKey)) {
            SettingsStore.setInitStatus(SDKInitStatus.KeysRequired)
            EventStore.push("Init skipped", "{\"reason\":\"keys required\"}", EventSource.System)
            return
        }

        // Start building the SDK configuration from your app key and access key.
        val configBuilder = SalesIQConfiguration.Builder(appKey, accessKey)
            // Show calls in a floating window that stays on top of the app.
            .setCallViewMode(SalesIQConfiguration.SalesIQCallViewMode.FLOATING)

        // Reflect the Core screen's configuration builder into the automatic init call.
        val authValue = ConfigStore.authValue.value.trim()
        when (ConfigStore.authMode.value) {
            // Identify the visitor by a simple unique user id.
            AuthMode.UserId -> if (authValue.isNotEmpty()) configBuilder.setUserId(authValue)
            // Identify the visitor securely with a signed JWT token.
            AuthMode.Jwt -> if (authValue.isNotEmpty()) configBuilder.setAuth(SalesIQJWTAuth(authValue))
            AuthMode.Guest -> { /* Guest is the default; no auth override. */ }
        }
        if (ConfigStore.customFonts.value) {
            // Sample custom fonts — map to bundled asset paths if present.
            // Override the SDK's default font at the given weight with a bundled font file.
            configBuilder.setFont(400, "fonts/regular.ttf")
            configBuilder.setFont(500, "fonts/medium.ttf")
        }

        // Finalize the configuration object to hand to the SDK.
        val config = configBuilder.build()

        // Boot the SDK with the configuration; the callback reports success or failure.
        ZohoSalesIQ.initialize(application, config) { result ->
            if (result.isSuccess) {
                SettingsStore.setInitStatus(SDKInitStatus.Initialized)
                EventStore.push("SDK initialized", "{\"status\":\"success\"}", EventSource.System)
                // Make the chat UI follow the device's light/dark theme automatically.
                ZohoSalesIQ.syncThemeWithOS(true)
                // Keep the floating chat launcher button visible at all times.
                ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS)
                // Show the pre-chat form so visitors enter details before chatting.
                ZohoSalesIQ.Chat.setVisibility(ChatComponent.prechatForm, true)
                // Display the operator's photo on the launcher button.
                ZohoSalesIQ.Chat.showOperatorImageInLauncher(true)
                enablePushAutomatically()
            } else {
                val error = result.error
                Log.d(TAG, "Init failed: ${error?.code} ${error?.message}")
                SettingsStore.setInitStatus(SDKInitStatus.Failed, error?.message)
                EventStore.push(
                    "SDK init failed",
                    jsonOf("code" to error?.code, "message" to error?.message),
                    EventSource.System,
                )
            }
        }
    }

    /** Register push automatically at launch — no button in the UI. */
    private fun enablePushAutomatically() {
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    // Register this device's FCM token so SalesIQ can deliver push notifications.
                    ZohoSalesIQ.Notification.enablePush(token, false)
                    // Enable in-app notification banners for new messages.
                    ZohoSalesIQ.Notification.enableInApp()
                    EventStore.push(
                        "Push registered",
                        "{\"status\":\"enabled\"}",
                        EventSource.Notification
                    )
                }
                .addOnFailureListener { e ->
                    EventStore.push(
                        "Push registration failed",
                        jsonOf("message" to e.message),
                        EventSource.Notification
                    )
                }
        }.onFailure {
            Log.d(TAG, "Push registration failed: ${it.message}", it)
            EventStore.push(
                "Push registration failed",
                jsonOf("message" to it.message),
                EventSource.Notification
            )
        }
    }
}
