package com.taruc.visory.utils

class Feedback(
    var feedbackDateTime: MutableMap<String, String>,
    var customFeedback: String = "",
    var userId: String = "",
    var reportedUserId: String = "",
    var callId: String = "",
    var status: String = ""
) {

    override fun toString(): String {
        return "Feedback Date Time: $feedbackDateTime\n" +
                "Custom Feedback: $customFeedback\n" +
                "User Id: $userId\n" +
                "Reported User Id: $reportedUserId\n" +
                "Call Id: $callId\n" +
                "Status: $status\n"
    }
}