package com.salesiq.demoapp.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/** Source module of an SDK event — drives the colored stripe in the Events Console. */
enum class EventSource { Chat, Calls, Launcher, KnowledgeBase, Notification, System }

data class SDKEvent(
    val id: Long,
    val ts: Long,
    val name: String,
    val payload: String,
    val source: EventSource,
)

/**
 * In-memory event store fed by ALL SDK listeners (global + Chat + KnowledgeBase +
 * Notification + Launcher + Calls state). Backs the Events Console with an unseen badge.
 */
object EventStore {
    private const val MAX_EVENTS = 200

    private val _events = MutableStateFlow<List<SDKEvent>>(emptyList())
    val events: StateFlow<List<SDKEvent>> = _events.asStateFlow()

    private val _unseen = MutableStateFlow(0)
    val unseen: StateFlow<Int> = _unseen.asStateFlow()

    /** Monotonically increasing id so events can be referenced by stable identity, not list position. */
    private val nextId = AtomicLong(0L)

    fun push(name: String, payload: String, source: EventSource) {
        val event = SDKEvent(nextId.getAndIncrement(), System.currentTimeMillis(), name, payload, source)
        _events.value = (listOf(event) + _events.value).take(MAX_EVENTS)
        _unseen.value = _unseen.value + 1
    }

    fun clear() {
        _events.value = emptyList()
    }

    fun markSeen() {
        _unseen.value = 0
    }

    /** Most recent event from a given source, if any (used by Notifications screen). */
    fun latestFrom(source: EventSource): SDKEvent? =
        _events.value.firstOrNull { it.source == source }
}
