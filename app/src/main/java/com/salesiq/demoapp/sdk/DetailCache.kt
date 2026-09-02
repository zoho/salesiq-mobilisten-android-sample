package com.salesiq.demoapp.sdk

import com.zoho.livechat.android.VisitorChat
import com.zoho.livechat.android.modules.conversations.models.SalesIQConversation
import com.zoho.livechat.android.modules.knowledgebase.ui.entities.Resource

/**
 * In-memory hand-off between a list screen and its detail screen. Navigation only
 * carries an id; the rich fetched object is looked up here so detail screens can
 * render real fields without re-fetching.
 */
object DetailCache {
    var chats: List<VisitorChat> = emptyList()
    var calls: List<SalesIQConversation> = emptyList()
    var resources: List<Resource> = emptyList()

    fun chat(id: String): VisitorChat? = chats.firstOrNull { it.chatID == id }
    fun call(id: String): SalesIQConversation? = calls.firstOrNull { it.id == id }
    fun resource(id: String): Resource? = resources.firstOrNull { it.id == id }
}
