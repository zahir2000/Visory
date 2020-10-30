package com.taruc.visory.utils

class CallHistoryClass(
    var callerId: String = "",
    var calleeId: String = "",
    var callDateTime: String = "",
    var callTime: String = ""
) {
    override fun toString(): String {
        return "Caller Id: $callerId\nCallee Id: $calleeId\nCall Date Time: $callDateTime\nCall Time: $callTime\n"
    }
}