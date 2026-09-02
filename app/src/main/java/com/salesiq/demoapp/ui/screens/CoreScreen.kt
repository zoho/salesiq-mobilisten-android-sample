package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.ConversationDataProviderStore
import com.salesiq.demoapp.sdk.SampleConversationDataProvider
import com.salesiq.demoapp.state.AuthMode
import com.salesiq.demoapp.state.ConfigStore
import com.salesiq.demoapp.state.SDKInitStatus
import com.salesiq.demoapp.state.SettingsStore
import com.salesiq.demoapp.state.nextLanguage
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Badge
import com.salesiq.demoapp.ui.components.BadgeTone
import com.salesiq.demoapp.ui.components.BannerVariant
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.ColumnScopeRows
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.StatusBanner
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.components.TitleTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.livechat.android.exception.InvalidEmailException
import com.zoho.livechat.android.modules.common.ui.entities.PresentOptions
import com.zoho.livechat.android.modules.conversations.models.CommunicationMode
import com.zoho.salesiq.core.config.SalesIQConfig
import com.zoho.salesiq.core.models.SalesIQTimeoutType
import com.zoho.salesiqembed.ZohoSalesIQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private enum class ScreenType { Conversation, KnowledgeBase }
private enum class SessionKind { Chat, Call }
private enum class Behavior { None, AlwaysNew, ContinueOrNew }
private enum class ListType { Chat, Call, All, None }
private enum class ListFilter { Ongoing, Ended, All }
private enum class TimeoutTarget { Secret, Display }

/** Screen 02 — deepest screen: present() builder, config auth/fonts, advanced, flags, provider, status. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoreScreen(nav: NavController) {
    val initStatus by SettingsStore.initStatus.collectAsState()

    // Configuration
    var language by remember { mutableStateOf("English") }
    var department by remember { mutableStateOf("Support") }
    var operatorEmail by remember { mutableStateOf("") }
    var pageTitle by remember { mutableStateOf("Checkout screen") }
    var sessionId by remember { mutableStateOf("") }

    // present() builder
    var screenType by remember { mutableStateOf(ScreenType.Conversation) }
    var sessionKind by remember { mutableStateOf(SessionKind.Chat) }
    var behavior by remember { mutableStateOf(Behavior.ContinueOrNew) }
    var listType by remember { mutableStateOf(ListType.All) }
    var listFilter by remember { mutableStateOf(ListFilter.Ongoing) }
    // Knowledge base screen: optional article id to open directly.
    var articleId by remember { mutableStateOf("") }

    // Configuration builder
    val authMode by ConfigStore.authMode.collectAsState()
    val authValue by ConfigStore.authValue.collectAsState()
    val customFonts by ConfigStore.customFonts.collectAsState()

    // Advanced
    var themeSourcePortal by remember { mutableStateOf(true) }
    var timeoutTarget by remember { mutableStateOf(TimeoutTarget.Secret) }
    var timeoutMs by remember { mutableStateOf("30000") }

    // Configuration flags (setConfig)
    var neutralRatingDisabled by remember { mutableStateOf(false) }
    var includeVisitorInfo by remember { mutableStateOf(true) }
    var replyEnabled by remember { mutableStateOf(true) }

    // Data provider — key/value editors backed by the store's JSON.
    val providerRegistered by ConversationDataProviderStore.registered.collectAsState()
    val displayEntries = remember {
        mutableStateListOf<KvEntry>().also { list ->
            val fields = ConversationDataProviderStore.displayFields()
            if (fields.isEmpty()) list.add(KvEntry("order_id", "A-1024")) else fields.forEach { list.add(KvEntry(it.key, it.value)) }
        }
    }
    val secretEntries = remember {
        mutableStateListOf<KvEntry>().also { list ->
            val fields = ConversationDataProviderStore.secretFields()
            if (fields.isEmpty()) list.add(KvEntry("auth_token", "jwt-sample")) else fields.forEach { list.add(KvEntry(it.key, it.value)) }
        }
    }
    fun syncProviderFields() {
        ConversationDataProviderStore.setDisplayFieldsJson(entriesToJson(displayEntries))
        ConversationDataProviderStore.setSecretFieldsJson(entriesToJson(secretEntries))
    }

    var result by remember { mutableStateOf<String?>(null) }

    // Snapshot the SDK status once, off the main thread. Reading these directly in the composable
    // body re-ran them on every recomposition, and getBadgeCount() does a synchronous DB query —
    // an ANR/jank risk on the main thread. produceState fetches them on Dispatchers.IO and caches
    // the result as state.
    val sdkStatus by produceState(initialValue = CoreSdkStatus()) {
        value = withContext(Dispatchers.IO) {
            CoreSdkStatus(
                brandOnline = ZohoSalesIQ.isBrandOnline(),
                liveChatAvailable = ZohoSalesIQ.isLiveChatAvailable(),
                unread = ZohoSalesIQ.Notification.getBadgeCount(),
                communicationMode = ZohoSalesIQ.getCommunicationMode(),
                multipleOpenRestricted = ZohoSalesIQ.Chat.isMultipleOpenRestricted(),
            )
        }
    }
    val brandOnline = sdkStatus.brandOnline
    val liveChatAvailable = sdkStatus.liveChatAvailable
    val unread = sdkStatus.unread
    val communicationMode = sdkStatus.communicationMode
    val multipleOpenRestricted = sdkStatus.multipleOpenRestricted

    fun buildPresentOptions(): PresentOptions {
        val builder = PresentOptions.Builder()
        if (screenType == ScreenType.KnowledgeBase) {
            builder.setScreen(
                PresentOptions.Screen.KnowledgeBase(
                    PresentOptions.Screen.KnowledgeBase.ResourceType.ARTICLES,
                    articleId.ifBlank { null },
                ),
            )
            return builder.build()
        }
        val type = if (sessionKind == SessionKind.Call) {
            PresentOptions.Screen.Conversation.SessionType.CALL
        } else {
            PresentOptions.Screen.Conversation.SessionType.CHAT
        }
        val filter = when (listFilter) {
            ListFilter.Ongoing -> PresentOptions.ConversationsListFilter.ONGOING
            ListFilter.Ended -> PresentOptions.ConversationsListFilter.ENDED
            ListFilter.All -> PresentOptions.ConversationsListFilter.ALL
        }
        val list: PresentOptions.ConversationList = when (listType) {
            ListType.Chat -> PresentOptions.ConversationList.Chat(filter)
            ListType.Call -> PresentOptions.ConversationList.Call(filter)
            ListType.All -> PresentOptions.ConversationList.All(filter)
            ListType.None -> PresentOptions.ConversationList.None
        }
        val sessionBehavior = when (behavior) {
            Behavior.None -> PresentOptions.Screen.Conversation.SessionBehavior.None
            Behavior.AlwaysNew -> PresentOptions.Screen.Conversation.SessionBehavior.AlwaysNew(type)
            Behavior.ContinueOrNew -> PresentOptions.Screen.Conversation.SessionBehavior.ContinueOrNew(type)
        }
        builder.setScreen(PresentOptions.Screen.Conversation(list, sessionBehavior))
        return builder.build()
    }

    ScreenScaffold(
        title = "Core & Config",
        subtitle = "Configuration, navigation & status",
        onBack = { nav.popBackStack() },
    ) {
        val (bannerVariant, bannerTitle, bannerBody) = when (initStatus) {
            SDKInitStatus.Initialized -> Triple(BannerVariant.Ok, "Initialized at app launch", "Automatic with your stored keys — no manual init.")
            SDKInitStatus.KeysRequired -> Triple(BannerVariant.Info, "Keys required", "Add your app key and access key in Settings — the SDK initializes on next launch.")
            SDKInitStatus.Failed -> Triple(BannerVariant.Danger, "Initialization failed", "Automatic with your stored keys — no manual init.")
            SDKInitStatus.Pending -> Triple(BannerVariant.Info, "Initializing…", "Automatic with your stored keys — no manual init.")
        }
        StatusBanner(title = bannerTitle, body = bannerBody, variant = bannerVariant)

        Section(title = "Status") {
            Card(separated = false) {
                row {
                    FlowRow(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Badge(if (brandOnline) "Brand online" else "Brand offline", if (brandOnline) BadgeTone.Success else BadgeTone.Warning, AppIcon.Check)
                        // Whether live chat can be started right now.
                        Badge(if (liveChatAvailable) "Chat available" else "Chat unavailable", BadgeTone.Primary)
                        Badge("Mode: ${communicationMode?.name?.lowercase() ?: "unknown"}", BadgeTone.Primary)
                        Badge("$unread unread", BadgeTone.Warning)
                        Badge(if (multipleOpenRestricted) "Single-chat: on" else "Single-chat: off", BadgeTone.Primary)
                    }
                }
            }
        }

        // ── present() builder ─────────────────────────────────────────────
        Section(title = "Present a screen") {
            SegmentedControl(
                segments = listOf(ScreenType.Conversation to "Conversation", ScreenType.KnowledgeBase to "Knowledge base"),
                selected = screenType,
                onSelect = { screenType = it },
            )
            if (screenType == ScreenType.Conversation) {
                Card {
                    row {
                        ListRow("Session type", subtitle = "SIQConversationScreen", value = if (sessionKind == SessionKind.Chat) "Chat" else "Call", chevron = true, onClick = {
                            sessionKind = if (sessionKind == SessionKind.Chat) SessionKind.Call else SessionKind.Chat
                        })
                    }
                    row {
                        val label = when (behavior) {
                            Behavior.None -> "None"
                            Behavior.AlwaysNew -> "Always new"
                            Behavior.ContinueOrNew -> "Continue / new"
                        }
                        ListRow("Session behavior", value = label, chevron = true, onClick = {
                            behavior = when (behavior) {
                                Behavior.None -> Behavior.AlwaysNew
                                Behavior.AlwaysNew -> Behavior.ContinueOrNew
                                Behavior.ContinueOrNew -> Behavior.None
                            }
                        })
                    }
                    row {
                        ListRow("List type", subtitle = "withList(type, filter)", value = listType.name, chevron = true, onClick = {
                            listType = when (listType) {
                                ListType.Chat -> ListType.Call
                                ListType.Call -> ListType.All
                                ListType.All -> ListType.None
                                ListType.None -> ListType.Chat
                            }
                        })
                    }
                    row {
                        ListRow("List filter", value = listFilter.name, chevron = true, onClick = {
                            listFilter = when (listFilter) {
                                ListFilter.Ongoing -> ListFilter.Ended
                                ListFilter.Ended -> ListFilter.All
                                ListFilter.All -> ListFilter.Ongoing
                            }
                        })
                    }
                }
            } else {
                // Knowledge base: only the ARTICLES resource type exists; an
                // optional article id opens that article directly.
                Card {
                    row { ListRow("Type", subtitle = "ResourceType.ARTICLES", value = "Articles") }
                    row {
                        Field(
                            "Article ID",
                            articleId,
                            { articleId = it },
                            placeholder = "Optional — open a specific article",
                            autoCapitalize = false
                        )
                    }
                }
            }
            AppButton("present(PresentOptions)", icon = AppIcon.Nav, onClick = {
                // Open the SDK's chat/knowledge-base UI configured by the options built above.
                ZohoSalesIQ.present(buildPresentOptions()) { res ->
                    result = res.toResultString { jsonOf("action" to "present", "status" to "presented") }
                }
                Toaster.show("Presenting SDK screen", ToastTone.Success)
            })
            AppButton("Dismiss UI", variant = ButtonVariant.Secondary, onClick = {
                // Close any SalesIQ screen the SDK is currently showing.
                ZohoSalesIQ.dismissUI()
                Toaster.show("SDK UI dismissed")
            })
        }

        // ── Configuration ─────────────────────────────────────────────────
        Section(title = "Configuration") {
            Card {
                row {
                    ListRow("Language", icon = AppIcon.Globe, tint = IconTint.Primary, value = language, chevron = true, onClick = {
                        val next = nextLanguage(language)
                        language = next.label
                        // Force the chat UI to display in the chosen language code.
                        ZohoSalesIQ.Chat.setLanguage(next.code)
                        Toaster.show("Language set to ${next.label}", ToastTone.Success)
                    })
                }
                row {
                    ListRow("Department", subtitle = "Route new chats", value = department, chevron = true, onClick = {
                        nav.navigate(Routes.DEPARTMENT_PICKER)
                    })
                }
                row { Field("Operator email", operatorEmail, { operatorEmail = it }, placeholder = "agent@brand.com", autoCapitalize = false) }
                row { Field("Page title", pageTitle, { pageTitle = it }) }
                row { Field("Session ID", sessionId, { sessionId = it }, placeholder = "Optional identifier", autoCapitalize = false) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton("Apply page title", modifier = Modifier.weight(1f), variant = ButtonVariant.Secondary, onClick = {
                    // Tell the SDK which screen the visitor is on, for operator context.
                    ZohoSalesIQ.Tracking.setPageTitle(pageTitle)
                    Toaster.show("Page title updated", ToastTone.Success)
                })
                AppButton("Set operator email", modifier = Modifier.weight(1f), variant = ButtonVariant.Secondary, onClick = {
                    try {
                        // Route new chats to a specific operator by their email address.
                        ZohoSalesIQ.Chat.setOperatorEmail(operatorEmail.trim())
                        result = jsonOf("operatorEmail" to operatorEmail.trim())
                        Toaster.show("Operator email set", ToastTone.Success)
                    } catch (e: InvalidEmailException) {
                        result = jsonOf("error" to (e.message ?: "Invalid email"))
                        Toaster.show("Invalid email address", ToastTone.Danger)
                    }
                })
            }
        }

        // ── Configuration builder ─────────────────────────────────────────
        Section(title = "Configuration builder", footer = "Applied at the automatic init call — restart the app to take effect.") {
            SegmentedControl(
                segments = listOf(AuthMode.Guest to "Guest", AuthMode.UserId to "User ID", AuthMode.Jwt to "JWT"),
                selected = authMode,
                onSelect = { ConfigStore.setAuthMode(it) },
            )
            Card {
                if (authMode != AuthMode.Guest) {
                    row {
                        Field(
                            if (authMode == AuthMode.UserId) "User ID" else "JWT token",
                            authValue,
                            { ConfigStore.setAuthValue(it) },
                            placeholder = "Provide for registered visitor",
                            autoCapitalize = false,
                        )
                    }
                }
                row {
                    SwitchRow("Custom fonts", customFonts, { ConfigStore.setCustomFonts(it) }, subtitle = "regular · medium", icon = AppIcon.Logs, tint = IconTint.Primary)
                }
            }
        }

        // ── Advanced ──────────────────────────────────────────────────────
        Section(title = "Advanced") {
            Card {
                row {
                    ListRow("Theme source", subtitle = "setThemeSource", icon = AppIcon.Sun, tint = IconTint.Primary, value = if (themeSourcePortal) "Portal" else "SDK", onClick = {
                        themeSourcePortal = !themeSourcePortal
                        // Choose whether chat theming comes from the SalesIQ portal or SDK config.
                        ZohoSalesIQ.setThemeSource(
                            if (themeSourcePortal) ZohoSalesIQ.ThemeSource.SalesIQPortalConfiguration else ZohoSalesIQ.ThemeSource.SdkConfiguration,
                        )
                        Toaster.show("Theme source: ${if (themeSourcePortal) "Portal" else "SDK"}", ToastTone.Success)
                    })
                }
                row {
                    ListRow("Refresh data", subtitle = "display / secret fields", icon = AppIcon.Refresh, tint = IconTint.Secondary, chevron = true, onClick = {
                        nav.navigate(Routes.REFRESH_DATA)
                    })
                }
                row {
                    val target = when (timeoutTarget) {
                        TimeoutTarget.Secret -> "Secret"
                        TimeoutTarget.Display -> "Display"
                    }
                    ListRow(
                        "Timeout",
                        subtitle = "secret / display",
                        icon = AppIcon.Timer,
                        tint = IconTint.Accent,
                        value = "$target · ${timeoutMs}ms",
                        onClick = {
                        timeoutTarget = when (timeoutTarget) {
                            TimeoutTarget.Secret -> TimeoutTarget.Display
                            TimeoutTarget.Display -> TimeoutTarget.Secret
                        }
                    })
                }
                row { Field("Timeout (ms)", timeoutMs, { timeoutMs = it.filter { ch -> ch.isDigit() } }, placeholder = "30000", autoCapitalize = false) }
                row {
                    ListRow("URI scheme", subtitle = "setUriScheme", icon = AppIcon.Link, tint = IconTint.Primary, chevron = true, onClick = {
                        nav.navigate(Routes.URI_SCHEME)
                    })
                }
            }
            AppButton("Apply timeout", variant = ButtonVariant.Secondary, onClick = {
                val ms = timeoutMs.toLongOrNull() ?: 30000L
                val type = when (timeoutTarget) {
                    TimeoutTarget.Secret -> SalesIQTimeoutType.SECRET_FIELDS
                    TimeoutTarget.Display -> SalesIQTimeoutType.DISPLAY_FIELDS
                }
                // Set how long the SDK waits for the app to supply display/secret fields.
                ZohoSalesIQ.setTimeout(type, ms)
                result = jsonOf("timeoutType" to type.name, "millis" to ms)
                Toaster.show("Timeout applied", ToastTone.Success)
            })
        }

        // ── Configuration flags (setConfig) ───────────────────────────────
        Section(title = "Configuration flags", footer = "Runtime SalesIQConfig flags applied via setConfig.") {
            Card {
                row {
                    SwitchRow("Neutral rating disabled", neutralRatingDisabled, {
                        neutralRatingDisabled = it
                        // Toggle a runtime SDK flag: use like/dislike rating instead of a neutral option.
                        ZohoSalesIQ.setConfig(SalesIQConfig.BinaryRating(it))
                    })
                }
                row {
                    SwitchRow("Include visitor info", includeVisitorInfo, {
                        includeVisitorInfo = it
                        // Toggle a runtime SDK flag: send visitor info along with display fields.
                        ZohoSalesIQ.setConfig(SalesIQConfig.IncludeVisitorInfoWithDisplayFields(it))
                    })
                }
                row {
                    SwitchRow("Message reply enabled", replyEnabled, {
                        replyEnabled = it
                        // Toggle a runtime SDK flag: allow visitors to reply to messages.
                        ZohoSalesIQ.setConfig(SalesIQConfig.ReplyEnabled(it))
                    })
                }
            }
        }

        // ── Conversation data provider ────────────────────────────────────
        Section(title = "Conversation data provider", footer = "The SDK invokes this to fetch display / secret fields; refreshData triggers it and each request is logged to Events.") {
            Card {
                row {
                    SwitchRow("Register data provider", providerRegistered, { enabled ->
                        ConversationDataProviderStore.setRegistered(enabled)
                        if (enabled) {
                            // Register a provider so the SDK can pull custom display/secret fields on demand.
                            ZohoSalesIQ.Conversation.setDataProvider(SampleConversationDataProvider)
                        } else {
                            // Remove the provider so the SDK stops requesting custom fields.
                            ZohoSalesIQ.Conversation.setDataProvider(null)
                        }
                        Toaster.show(if (enabled) "Data provider registered" else "Data provider removed", ToastTone.Success)
                    }, subtitle = "setDataProvider", icon = AppIcon.Refresh, tint = IconTint.Secondary)
                }
            }
            AppText("Display fields", style = AppType.caption, tone = TextTone.Secondary, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
            Card { keyValueRows(displayEntries) { syncProviderFields() } }
            AppText("Secret fields", style = AppType.caption, tone = TextTone.Secondary, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
            Card { keyValueRows(secretEntries) { syncProviderFields() } }
        }

        Section(title = "Response") {
            ResultBlock(
                text = result ?: jsonOf(
                    "isSDKInitialized" to (initStatus == SDKInitStatus.Initialized),
                    "communicationMode" to (communicationMode?.name?.lowercase() ?: "unknown"),
                ),
            )
        }
    }
}

/** One editable key/value pair for the data-provider field editors. */
private data class KvEntry(val key: String, val value: String)

/** Serializes non-blank key/value entries into the JSON the store/provider expects. */
private fun entriesToJson(entries: List<KvEntry>): String {
    val obj = JSONObject()
    entries.filter { it.key.isNotBlank() }.forEach { obj.put(it.key.trim(), it.value) }
    return obj.toString()
}

/**
 * Dynamic key/value editor rows (same UX as Visitor's custom info), reused for the
 * data provider's display and secret fields. Calls [onChange] after every edit so the
 * caller can re-serialize into the store.
 */
@Composable
private fun ColumnScopeRows.keyValueRows(
    entries: SnapshotStateList<KvEntry>,
    onChange: () -> Unit,
) {
    val c = LocalAppColors.current
    entries.forEachIndexed { index, entry ->
        row {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Field(
                    if (index == 0) "Key" else "Key ${index + 1}",
                    entry.key,
                    { entries[index] = entry.copy(key = it); onChange() },
                    modifier = Modifier.weight(1f),
                    placeholder = "e.g. order_id",
                    autoCapitalize = false,
                )
                Field(
                    if (index == 0) "Value" else "Value ${index + 1}",
                    entry.value,
                    { entries[index] = entry.copy(value = it); onChange() },
                    modifier = Modifier.weight(1f),
                    placeholder = "e.g. A-1024",
                )
                Icon(
                    AppIcon.Close.vector,
                    contentDescription = "Remove field",
                    tint = c.danger,
                    modifier = Modifier
                        .padding(start = 8.dp, top = 6.dp)
                        .size(20.dp)
                        .clickable { entries.removeAt(index); onChange() },
                )
            }
        }
    }
    row {
        ListRow(
            "Add key / value",
            icon = AppIcon.Plus,
            tint = IconTint.Primary,
            titleTone = TitleTone.Brand,
            onClick = { entries.add(KvEntry("", "")); onChange() },
        )
    }
}

/** One-shot snapshot of SDK status shown on the Core screen (fetched off the main thread). */
private data class CoreSdkStatus(
    val brandOnline: Boolean = false,
    val liveChatAvailable: Boolean = false,
    val unread: Int = 0,
    val communicationMode: CommunicationMode? = null,
    val multipleOpenRestricted: Boolean = false,
)
