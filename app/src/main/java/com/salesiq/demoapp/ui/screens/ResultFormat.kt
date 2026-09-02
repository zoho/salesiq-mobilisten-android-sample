package com.salesiq.demoapp.ui.screens

import com.zoho.livechat.android.modules.common.ui.result.entities.SalesIQError
import com.zoho.livechat.android.modules.common.ui.result.entities.SalesIQResult
import org.json.JSONObject

/** Renders a small JSON object as a pretty string for a ResultBlock. */
fun jsonOf(vararg pairs: Pair<String, Any?>): String {
    val obj = JSONObject()
    pairs.forEach { (k, v) -> obj.put(k, v ?: JSONObject.NULL) }
    return obj.toString(2)
}

/** Formats a SalesIQError as an error ResultBlock string with real code + message. */
fun SalesIQError.asResult(): String = jsonOf("errorCode" to code, "message" to (message ?: "Unknown error"))

/** Convenience for turning a SalesIQResult into a ResultBlock string via a success mapper. */
inline fun <T> SalesIQResult<T>.toResultString(success: (T?) -> String): String =
    if (isSuccess) success(data) else (error?.asResult() ?: jsonOf("error" to "Unknown"))
