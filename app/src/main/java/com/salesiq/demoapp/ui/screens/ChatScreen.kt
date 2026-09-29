package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.TitleTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.commons.ChatComponent
import com.zoho.livechat.android.VisitorChat
import com.zoho.livechat.android.listeners.ConversationListener
import com.zoho.livechat.android.listeners.OperatorImageListener
import com.zoho.salesiqembed.ZohoSalesIQ

private data class ComponentRow(val component: ChatComponent, val title: String)

private val CHAT_COMPONENTS = listOf(
    ComponentRow(ChatComponent.operatorImage, "Operator image"),
    ComponentRow(ChatComponent.rating, "Rating"),
    ComponentRow(ChatComponent.feedback, "Feedback"),
    ComponentRow(ChatComponent.fileShare, "File sharing"),
    ComponentRow(ChatComponent.prechatForm, "Pre-chat form"),
    ComponentRow(ChatComponent.visitorName, "Visitor name"),
    ComponentRow(ChatComponent.emailTranscript, "Email transcript"),
    ComponentRow(ChatComponent.screenshot, "Screenshot"),
    ComponentRow(ChatComponent.takePhoto, "Take photo"),
    ComponentRow(ChatComponent.recordVideo, "Record video"),
    ComponentRow(ChatComponent.gallery, "Gallery"),
    ComponentRow(ChatComponent.reopen, "Reopen"),
    ComponentRow(ChatComponent.queuePosition, "Queue position"),
    ComponentRow(ChatComponent.call, "Call"),
    ComponentRow(ChatComponent.end, "End"),
    ComponentRow(ChatComponent.endWhenInQueue, "End · when in queue"),
    ComponentRow(ChatComponent.endWhenBotConnected, "End · when bot connected"),
    ComponentRow(ChatComponent.endWhenOperatorConnected, "End · when operator connected"),
    ComponentRow(ChatComponent.fileSharingWhenBotConnected, "File sharing · when bot connected"),
    ComponentRow(ChatComponent.voiceNoteWhenBotConnected, "Voice note · when bot connected"),
)

/** Screen 05 — start/trigger, open, end, titles, waiting, feedback, attributes, components, operator image. */
@Composable
fun ChatScreen(nav: NavController) {
    var question by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Support") }
    var triggerAction by remember { mutableStateOf("") }
    var attrName by remember { mutableStateOf("") }
    var chatId by remember { mutableStateOf("") }
    var onlineTitle by remember { mutableStateOf("") }
    var offlineTitle by remember { mutableStateOf("") }
    var waitingTime by remember { mutableStateOf("30") }
    var hideQueueTime by remember { mutableStateOf(false) }
    var feedbackAfterSkip by remember { mutableStateOf(true) }
    var attenderId by remember { mutableStateOf("") }
    var chatActionName by remember { mutableStateOf("apply_coupon") }
    var attenderImage by remember { mutableStateOf<android.graphics.drawable.Drawable?>(null) }
    var chats by remember { mutableStateOf<List<VisitorChat>>(emptyList()) }
    var offlineMessage by remember { mutableStateOf(true) }
    var allowOfflineChat by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<String?>(null) }

    // DepartmentPicker returns the chosen department via the saved-state handle.
    val currentEntry = nav.currentBackStackEntry
    LaunchedEffect(currentEntry) {
        val handle = currentEntry?.savedStateHandle ?: return@LaunchedEffect
        handle.getStateFlow<String?>("selectedDepartment", null).collect { picked ->
            if (!picked.isNullOrBlank()) {
                department = picked
                handle["selectedDepartment"] = null
            }
        }
    }

    val componentState = remember {
        mutableStateMapOf(
            ChatComponent.operatorImage to true,
            ChatComponent.rating to true,
            ChatComponent.feedback to true,
            ChatComponent.fileShare to true,
            ChatComponent.prechatForm to false,
            ChatComponent.visitorName to true,
            ChatComponent.emailTranscript to true,
            ChatComponent.screenshot to true,
            ChatComponent.takePhoto to true,
            ChatComponent.recordVideo to true,
            ChatComponent.gallery to true,
            ChatComponent.reopen to true,
            ChatComponent.queuePosition to true,
            ChatComponent.call to true,
            ChatComponent.end to true,
            ChatComponent.endWhenInQueue to true,
            ChatComponent.endWhenBotConnected to true,
            ChatComponent.endWhenOperatorConnected to true,
            ChatComponent.fileSharingWhenBotConnected to true,
            ChatComponent.voiceNoteWhenBotConnected to true,
        )
    }

    ScreenScaffold(
        title = "Chat",
        subtitle = "Conversations & window components",
        onBack = { nav.popBackStack() },
    ) {
        AppButton("Start a conversation", icon = AppIcon.Chat, onClick = {
            // Open a new chat with a starting question, routed to the given department.
            ZohoSalesIQ.Chat.start(question.ifBlank { "How can we help?" }, null, department) { res ->
                result = res.toResultString { chat -> jsonOf("chatId" to chat?.chatID, "status" to chat?.chatStatus) }
                if (res.isSuccess) Toaster.show("Conversation started", ToastTone.Success)
                else failToast("Couldn't start chat", res.error)
            }
        })
        AppButton("Orders demo — a chat per order", icon = AppIcon.Homepage, variant = ButtonVariant.Secondary, onClick = {
            nav.navigate(Routes.ORDERS)
        })

        // ── New chat ──────────────────────────────────────────────────────
        Section(title = "New chat") {
            Card {
                row { Field("Question", question, { question = it }, placeholder = "How can we help?") }
                row {
                    ListRow("Department", value = department, chevron = true, onClick = {
                        nav.navigate(Routes.DEPARTMENT_PICKER)
                    })
                }
                row { Field("Start with trigger", triggerAction, { triggerAction = it }, placeholder = "custom action name", autoCapitalize = false) }
                row { Field("Conversation attribute · name", attrName, { attrName = it }, placeholder = "e.g. VIP checkout") }
            }
            AppButton("Start with trigger", variant = ButtonVariant.Secondary, onClick = {
                if (triggerAction.isBlank()) {
                    Toaster.show("Enter a custom action name", ToastTone.Danger)
                    return@AppButton
                }
                // Start a chat by firing a named bot/automation trigger set up in the portal.
                ZohoSalesIQ.Chat.startWithTrigger(triggerAction.trim(), null, department) { res ->
                    result = res.toResultString { chat -> jsonOf("chatId" to chat?.chatID, "trigger" to triggerAction.trim()) }
                    if (res.isSuccess) Toaster.show("Triggered chat", ToastTone.Success)
                    else failToast("Couldn't trigger chat", res.error)
                }
            })
            AppButton("Apply conversation attributes", variant = ButtonVariant.Ghost, onClick = {
                // Attach custom attributes (like a name) to the next conversation.
                ZohoSalesIQ.Conversation.setAttributes { builder ->
                    if (attrName.isNotBlank()) builder.setName(attrName.trim())
                    builder
                }
                result = jsonOf("attribute" to "name", "value" to attrName)
                Toaster.show("Conversation attributes set", ToastTone.Success)
            })
            AppButton("Set default question", variant = ButtonVariant.Ghost, onClick = {
                if (question.isBlank()) {
                    Toaster.show("Enter a question first", ToastTone.Danger)
                    return@AppButton
                }
                // Pre-fill the question a visitor sees when they open a new chat.
                ZohoSalesIQ.Chat.setQuestion(question.trim())
                result = jsonOf("setQuestion" to question.trim())
                Toaster.show("Default question set", ToastTone.Success)
            })
            AppButton("Route to multiple departments", variant = ButtonVariant.Ghost, onClick = {
                val depts = listOf("Support", "Sales")
                // Offer the visitor a choice of these departments for new chats.
                ZohoSalesIQ.Chat.setDepartments(depts)
                result = jsonOf("setDepartments" to depts.joinToString())
                Toaster.show("Routing across ${depts.size} departments", ToastTone.Success)
            })
        }

        // ── Chat actions ──────────────────────────────────────────────────
        Section(
            title = "Chat actions",
            footer = "Register a custom chat action; when a visitor triggers it in chat it appears in Events (auto-completed by the sample listener).",
        ) {
            Card {
                row { Field("Action name", chatActionName, { chatActionName = it }, placeholder = "apply_coupon", autoCapitalize = false) }
            }
            AppButton("Register action", variant = ButtonVariant.Secondary, onClick = {
                val name = chatActionName.trim()
                if (name.isBlank()) {
                    Toaster.show("Enter an action name", ToastTone.Danger)
                    return@AppButton
                }
                ZohoSalesIQ.ChatActions.register(name)
                result = jsonOf("registered" to name)
                Toaster.show("Action registered", ToastTone.Success)
            })
            AppButton("Unregister action", variant = ButtonVariant.Ghost, onClick = {
                val name = chatActionName.trim()
                if (name.isBlank()) {
                    Toaster.show("Enter an action name", ToastTone.Danger)
                    return@AppButton
                }
                ZohoSalesIQ.ChatActions.unregister(name)
                result = jsonOf("unregistered" to name)
                Toaster.show("Action unregistered", ToastTone.Success)
            })
            AppButton("Unregister all", variant = ButtonVariant.Ghost, onClick = {
                ZohoSalesIQ.ChatActions.unregisterAll()
                result = jsonOf("unregisteredAll" to true)
                Toaster.show("All actions unregistered", ToastTone.Success)
            })
            AppButton("Set action timeout (30s)", variant = ButtonVariant.Ghost, onClick = {
                ZohoSalesIQ.ChatActions.setTimeout(30000L)
                result = jsonOf("timeoutMs" to 30000)
                Toaster.show("Action timeout set", ToastTone.Success)
            })
        }

        // ── Manage ────────────────────────────────────────────────────────
        Section(title = "Manage") {
            Card {
                row { Field("Chat ID", chatId, { chatId = it }, placeholder = "Paste a conversation ID", autoCapitalize = false) }
                row {
                    ListRow("Open by ID", titleTone = TitleTone.Brand, chevron = true, onClick = {
                        if (chatId.isBlank()) {
                            Toaster.show("Enter a chat ID first", ToastTone.Danger)
                            return@ListRow
                        }
                        val options = com.zoho.livechat.android.modules.common.ui.entities.PresentOptions.Builder()
                            .setScreen(
                                com.zoho.livechat.android.modules.common.ui.entities.PresentOptions.Screen.Conversation(
                                    chatId.trim(),
                                    com.zoho.livechat.android.modules.common.ui.entities.PresentOptions.Screen.Conversation.SessionType.CHAT,
                                ),
                            ).build()
                        // Open the SDK UI directly on the conversation with the given chat ID.
                        ZohoSalesIQ.present(options) { res ->
                            result = res.toResultString { jsonOf("openById" to chatId.trim()) }
                            if (res.isSuccess) Toaster.show("Opening chat", ToastTone.Success)
                            else failToast("Couldn't open chat", res.error)
                        }
                    })
                }
                row {
                    ListRow("Get chat by ID", subtitle = "fetch the VisitorChat object", chevron = true, onClick = {
                        if (chatId.isBlank()) {
                            Toaster.show("Enter a chat ID first", ToastTone.Danger)
                            return@ListRow
                        }
                        // Fetch a single conversation's details by its chat ID.
                        ZohoSalesIQ.Chat.get(chatId.trim()) { res ->
                            result = res.toResultString { chat -> jsonOf("chatId" to chat?.chatID, "status" to chat?.chatStatus, "question" to chat?.question) }
                            if (res.isSuccess) Toaster.show("Chat fetched", ToastTone.Success)
                            else failToast("Couldn't fetch chat", res.error)
                        }
                    })
                }
                row {
                    ListRow("Get chats", subtitle = "filter: open / closed / all", value = "All", chevron = true, onClick = {
                        // Fetch the list of the visitor's conversations.
                        ZohoSalesIQ.Chat.getList(object : ConversationListener {
                            override fun onSuccess(visitorChats: ArrayList<VisitorChat>) {
                                DetailCache.chats = visitorChats
                                chats = visitorChats
                                result = jsonOf("count" to visitorChats.size)
                                Toaster.show("Loaded ${visitorChats.size} chats", ToastTone.Success)
                            }

                            override fun onFailure(code: Int, message: String?) {
                                result = jsonOf("error" to (message ?: "Failed to load chats"))
                                failToast("Couldn't load chats", code, message)
                            }
                        })
                    })
                }
                row {
                    ListRow("End chat", titleTone = TitleTone.Danger, onClick = {
                        if (chatId.isBlank()) {
                            Toaster.show("Enter a chat ID first", ToastTone.Danger)
                            return@ListRow
                        }
                        // End the conversation with the given chat ID.
                        ZohoSalesIQ.Chat.endChat(chatId.trim())
                        result = jsonOf("ended" to chatId.trim())
                        Toaster.show("Chat ended", ToastTone.Default)
                    })
                }
            }
        }

        // ── Conversations (from Get chats) ────────────────────────────────
        if (chats.isNotEmpty()) {
            Section(title = "Conversations", footer = "Tap a conversation to open its detail.") {
                Card {
                    chats.forEach { chat ->
                        row {
                            ListRow(
                                chat.attenderName ?: chat.question ?: "Conversation",
                                subtitle = chat.chatStatus ?: chat.departmentName,
                                icon = AppIcon.Chat,
                                tint = IconTint.Primary,
                                chevron = true,
                                onClick = { chat.chatID?.let { nav.navigate(Routes.chatDetail(it)) } },
                            )
                        }
                    }
                }
            }
        }

        // ── Behavior ──────────────────────────────────────────────────────
        Section(title = "Behavior") {
            Card {
                row { Field("Online title", onlineTitle, { onlineTitle = it }, placeholder = "We're here") }
                row { Field("Offline title", offlineTitle, { offlineTitle = it }, placeholder = "Leave a message") }
                row { Field("Waiting time (s)", waitingTime, { waitingTime = it.filter { ch -> ch.isDigit() } }, placeholder = "30", autoCapitalize = false) }
                row {
                    SwitchRow("Hide queue time", hideQueueTime, {
                        hideQueueTime = it
                        // Hide/show the estimated wait time while a visitor is queued.
                        ZohoSalesIQ.Chat.hideQueueTime(it)
                    })
                }
                row {
                    SwitchRow("Show feedback after skip", feedbackAfterSkip, {
                        feedbackAfterSkip = it
                        // Still show the feedback form even when the visitor skips the rating.
                        ZohoSalesIQ.Chat.showFeedbackAfterSkip(it)
                    })
                }
                row {
                    SwitchRow("Show offline message", offlineMessage, {
                        offlineMessage = it
                        // Show the leave-a-message form when no operators are online.
                        ZohoSalesIQ.Chat.showOfflineMessage(it)
                    }, subtitle = "Show the leave-a-message form when offline")
                }
            }
            AppButton("Apply behavior", variant = ButtonVariant.Secondary, onClick = {
                // Set the chat window's header title for the online and offline states.
                ZohoSalesIQ.Chat.setTitle(onlineTitle.ifBlank { "We're here" }, offlineTitle.ifBlank { "Leave a message" })
                // Set how long (seconds) a visitor waits before the offline form appears.
                ZohoSalesIQ.Chat.setWaitingTime(waitingTime.toIntOrNull() ?: 30)
                // Show the feedback form this many seconds after a chat ends.
                ZohoSalesIQ.Chat.showFeedback(30)
                Toaster.show("Chat behavior applied", ToastTone.Success)
            })
        }

        // ── Visible components ────────────────────────────────────────────
        Section(title = "Visible components", footer = "Full ChatComponent set.") {
            Card {
                CHAT_COMPONENTS.forEach { entry ->
                    row {
                        SwitchRow(entry.title, componentState[entry.component] ?: false, {
                            componentState[entry.component] = it
                            // Show or hide one element of the chat window (rating, file share, etc.).
                            ZohoSalesIQ.Chat.setVisibility(entry.component, it)
                        })
                    }
                }
            }
        }

        // ── Operator image ────────────────────────────────────────────────
        Section(title = "Operator image") {
            Card {
                row { Field("Attender ID", attenderId, { attenderId = it }, placeholder = "operator id", autoCapitalize = false) }
                row {
                    ListRow(
                        "Fetched avatar",
                        subtitle = "fetchAttenderImage",
                        value = "Fetch",
                        icon = AppIcon.Visitor,
                        tint = IconTint.Secondary,
                        trailing = {
                            attenderImage?.let { drawable ->
                                DrawableAvatar(drawable)
                            }
                        },
                        onClick = {
                            if (attenderId.isBlank()) {
                                Toaster.show("Enter an attender ID first", ToastTone.Danger)
                                return@ListRow
                            }
                            // Fetch the operator's profile image by their attender ID.
                            ZohoSalesIQ.Chat.fetchAttenderImage(attenderId.trim(), true, object : OperatorImageListener {
                                override fun onSuccess(operatorImage: android.graphics.drawable.Drawable?) {
                                    attenderImage = operatorImage
                                    result = jsonOf("attenderId" to attenderId.trim(), "image" to "fetched")
                                    Toaster.show("Operator image fetched", ToastTone.Success)
                                }

                                override fun onFailure(code: Int, message: String?) {
                                    result = jsonOf("errorCode" to code, "message" to (message ?: "Unknown"))
                                    failToast("Couldn't fetch operator image", code, message)
                                }
                            })
                        },
                    )
                }
            }
        }

        result?.let { ResultBlock(text = it, label = "Last result") }
    }
}

/** Renders a fetched operator Drawable as a small 34dp avatar. */
@Composable
private fun DrawableAvatar(drawable: android.graphics.drawable.Drawable) {
    val bitmap = remember(drawable) {
        runCatching {
            val bmp = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888,
            )
            val canvas = android.graphics.Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp.asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = "Operator avatar",
            modifier = Modifier
                .size(34.dp)
                .clip(androidx.compose.foundation.shape.CircleShape),
        )
    } else {
        Box(Modifier.size(34.dp))
    }
}
