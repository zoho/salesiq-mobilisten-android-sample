package com.salesiq.demoapp

data class Result(val status: MobilistenInitStatus, val data: String) {
    enum class MobilistenInitStatus {
        SUCCESS, FAILURE
    }
}