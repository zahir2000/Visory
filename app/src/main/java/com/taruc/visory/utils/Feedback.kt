package com.taruc.visory.utils

class Feedback(
    var feedbackDateTime: String = "",
    var customFeedback: String = "",
    var userID: String = "",
    var callID: String = ""
) {

    override fun toString(): String {
        return "Feedback Date Time: $feedbackDateTime\nCustom Feedback: $customFeedback\nUser Id: $userID\nCall Id: $callID\n"
    }
}