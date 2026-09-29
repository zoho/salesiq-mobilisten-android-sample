package com.salesiq.demoapp.ui.screens

import android.util.Log
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
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

private const val ACTION_LOG_TAG = "mobilisten:action"

/**
 * The real reason (message, else code) to append to a user-action failure toast, so the user
 * sees WHY it failed rather than a bare "Failed". Returns "" when there is no detail.
 * Kotlin/Compose counterpart of the Cordova sample's App.reason.
 */
fun reason(error: SalesIQError?): String {
    val detail = error?.message?.takeIf { it.isNotBlank() } ?: error?.code?.toString()
    return if (!detail.isNullOrBlank()) ": $detail" else ""
}

/** [reason] for listener callbacks that deliver a raw (code, message) instead of a SalesIQError. */
fun reason(code: Int, message: String?): String {
    val detail = message?.takeIf { it.isNotBlank() } ?: code.toString()
    return ": $detail"
}

/** [reason] for a caught exception thrown by a synchronous SDK/platform call. */
fun reason(error: Throwable?): String {
    val detail = error?.message?.takeIf { it.isNotBlank() } ?: error?.let { it::class.simpleName }
    return if (!detail.isNullOrBlank()) ": $detail" else ""
}

/** Log a user-action failure and show it (with the real reason) in a Danger toast. */
fun failToast(message: String, error: SalesIQError?) {
    val full = message + reason(error)
    Log.w(ACTION_LOG_TAG, full)
    Toaster.show(full, ToastTone.Danger)
}

/** [failToast] for listener callbacks that deliver a raw (code, message). */
fun failToast(message: String, code: Int, detailMessage: String?) {
    val full = message + reason(code, detailMessage)
    Log.w(ACTION_LOG_TAG, full)
    Toaster.show(full, ToastTone.Danger)
}

/** [failToast] for a caught exception. */
fun failToast(message: String, error: Throwable?) {
    val full = message + reason(error)
    Log.w(ACTION_LOG_TAG, full, error)
    Toaster.show(full, ToastTone.Danger)
}
