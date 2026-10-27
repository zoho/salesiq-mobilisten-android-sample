package com.salesiq.demoapp.sdk

import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.zoho.livechat.android.modules.conversations.providers.DataProviderCallback
import com.zoho.livechat.android.modules.triggers.models.SalesIQTrigger
import com.zoho.livechat.android.modules.triggers.models.SalesIQTriggerSource
import com.zoho.livechat.android.modules.triggers.providers.SalesIQTriggerDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Holds the display-field input from the Core screen and, while registered, feeds it to the SDK
 * through [SampleTriggersDataProvider] when a trigger is initiated. Every provider invocation is
 * logged to the Events console so the demo shows the SDK pulling trigger-scoped data.
 */
object TriggerDataProviderStore {
    private val _registered = MutableStateFlow(false)
    val registered: StateFlow<Boolean> = _registered.asStateFlow()

    private val _displayFieldsJson = MutableStateFlow("{ \"plan\": \"pro\" }")
    val displayFieldsJson: StateFlow<String> = _displayFieldsJson.asStateFlow()

    fun setDisplayFieldsJson(value: String) { _displayFieldsJson.value = value }
    fun setRegistered(value: Boolean) { _registered.value = value }

    /** Parses the current JSON into a map, ignoring malformed input. */
    fun displayFields(): Map<String, String> = runCatching {
        val obj = JSONObject(_displayFieldsJson.value)
        buildMap { obj.keys().forEach { put(it, obj.optString(it)) } }
    }.getOrDefault(emptyMap())
}

/** Sample provider the SDK invokes on-demand for trigger display fields, before a trigger request. */
object SampleTriggersDataProvider : SalesIQTriggerDataProvider {

    override fun getDisplayFields(
        trigger: SalesIQTrigger,
        callback: DataProviderCallback<Map<String, String>>,
    ) {
        val fields = sampleDisplayFields(trigger.source)
        EventStore.push(
            "Trigger display fields",
            "{\"source\":\"${trigger.source.describe()}\"," +
                "\"customChatId\":\"${trigger.customChatId}\",\"count\":${fields.size}}",
            EventSource.System,
        )
        // Hand the display fields back to the SDK to include in the trigger payload (visitor_info).
        callback.onResult(fields)
    }

    /**
     * Distinct sample display fields per trigger source, so each case is recognizable while testing.
     * The user-configurable JSON from the Core screen is used as a common base; the per-source fields
     * are merged on top (and win on key conflicts).
     */
    private fun sampleDisplayFields(source: SalesIQTriggerSource): Map<String, String> {
        val configurableBaseFields = TriggerDataProviderStore.displayFields()
        val perSourceFields = when (source) {
            is SalesIQTriggerSource.LandedOnApp -> mapOf(
                "trigger_source" to "landed_on_app",
                "plan" to "free",
                "entry_point" to "app_launch",
                "lifecycle_stage" to "new_visitor",
            )

            is SalesIQTriggerSource.AccessedPage -> mapOf(
                "trigger_source" to "accessed_page",
                "page_title" to (source.pageTitle ?: "unknown"),
                "plan" to "pro",
                "section" to "catalog",
            )

            is SalesIQTriggerSource.ClickedOnChatWidget -> mapOf(
                "trigger_source" to "clicked_on_chat_widget",
                "plan" to "pro",
                "intent" to "support",
                "cart_value" to "1299",
            )

            is SalesIQTriggerSource.PerformedCustomAction -> mapOf(
                "trigger_source" to "performed_custom_action",
                "action_name" to source.actionName,
                "plan" to "enterprise",
                "account_tier" to "vip",
            )
        }
        return configurableBaseFields + perSourceFields
    }

    private fun SalesIQTriggerSource.describe(): String = when (this) {
        is SalesIQTriggerSource.LandedOnApp -> "LandedOnApp"
        is SalesIQTriggerSource.AccessedPage -> "AccessedPage(" + pageTitle + ")"
        is SalesIQTriggerSource.ClickedOnChatWidget -> "ClickedOnChatWidget"
        is SalesIQTriggerSource.PerformedCustomAction -> "PerformedCustomAction(" + actionName + ")"
    }
}
