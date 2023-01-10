package com.salesiq.demoapp

import com.zoho.livechat.android.constants.ConversationType

object MobilistenUtil {

    private const val TAG = "mobilisten:util"

    @JvmStatic
    fun getConversationType(index: Int): ConversationType {
        return ConversationType.values().get(index)
    }
}