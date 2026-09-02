package com.salesiq.demoapp.sdk

import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.zoho.livechat.android.modules.conversations.models.SalesIQConversation
import com.zoho.livechat.android.modules.conversations.providers.DataProviderCallback
import com.zoho.livechat.android.modules.conversations.providers.SalesIQConversationDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Holds the display / secret field inputs from the Core screen and, while registered,
 * feeds them to the SDK through [SampleConversationDataProvider]. Every provider
 * invocation is logged to the Events console so the demo shows the SDK pulling data.
 */
object ConversationDataProviderStore {
    private val _registered = MutableStateFlow(false)
    val registered: StateFlow<Boolean> = _registered.asStateFlow()

    private val _displayFieldsJson = MutableStateFlow("{ \"order_id\": \"A-1024\" }")
    val displayFieldsJson: StateFlow<String> = _displayFieldsJson.asStateFlow()

    private val _secretFieldsJson = MutableStateFlow("{ \"auth_token\": \"jwt-sample\" }")
    val secretFieldsJson: StateFlow<String> = _secretFieldsJson.asStateFlow()

    fun setDisplayFieldsJson(value: String) { _displayFieldsJson.value = value }
    fun setSecretFieldsJson(value: String) { _secretFieldsJson.value = value }
    fun setRegistered(value: Boolean) { _registered.value = value }

    /** Parses the current JSON into a map, ignoring malformed input. */
    fun displayFields(): Map<String, String> = parse(_displayFieldsJson.value)
    fun secretFields(): Map<String, String> = parse(_secretFieldsJson.value)

    private fun parse(raw: String): Map<String, String> = runCatching {
        val obj = JSONObject(raw)
        buildMap { obj.keys().forEach { put(it, obj.optString(it)) } }
    }.getOrDefault(emptyMap())
}

/** Sample provider the SDK invokes on-demand for display (custom info) / secret fields. */
object SampleConversationDataProvider : SalesIQConversationDataProvider {

    override fun getDisplayFields(
        conversation: SalesIQConversation,
        callback: DataProviderCallback<Map<String, String>>,
    ) {
        val fields = ConversationDataProviderStore.displayFields()
        EventStore.push(
            "Provider display fields",
            "{\"conversationId\":\"${conversation.id}\",\"count\":${fields.size}}",
            EventSource.System,
        )
        // Hand the display fields back to the SDK to attach to the conversation.
        callback.onResult(fields)
    }

    override fun getSecretFields(
        conversation: SalesIQConversation,
        callback: DataProviderCallback<Map<String, String>>,
    ) {
        val fields = ConversationDataProviderStore.secretFields()
        EventStore.push(
            "Provider secret fields",
            "{\"conversationId\":\"${conversation.id}\",\"count\":${fields.size}}",
            EventSource.System,
        )
        // Hand the secret fields back to the SDK to attach to the conversation.
        callback.onResult(fields)
    }
}
