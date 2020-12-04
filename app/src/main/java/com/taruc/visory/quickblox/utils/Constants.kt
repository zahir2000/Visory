package com.taruc.visory.quickblox.utils

import android.Manifest

val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)

const val EXTRA_LOGIN_RESULT = "login_result"

const val STOP_CALLING = "stop_calling"

const val HANG_UP = "hang_up"

const val IS_CURRENTLY_CALLING = "currently_calling"

const val CHECK_PERMISSIONS = "check_permissions"

const val CONNECTED_TO_USER = "connected_to_user"

const val EXTRA_LOGIN_RESULT_CODE = 1002

const val EXTRA_IS_INCOMING_CALL = "conversation_reason"

const val SERVICE_ID = 787

const val CHANNEL_ID = "Quickblox channel"

const val CHANNEL_NAME = "Quickblox background service"

const val EXTRA_COMMAND_TO_SERVICE = "command_for_service"

const val EXTRA_QB_USER = "qb_user"

const val COMMAND_NOT_FOUND = 0

const val COMMAND_LOGIN = 1

const val COMMAND_LOGOUT = 2

const val EXTRA_PENDING_INTENT = "pending_Intent"

const val MLKIT_TEXT_DETECTION = "mlkit_text_detection"

const val MLKIT_IMAGE_LABELING = "mlkit_image_labeling"

const val TOPIC = "/topics/blindLocation"

const val CALL_TOPIC = "/topics/calling"

const val CALL_TOPIC_END = "/topics/endcalls"

const val VOLUNTEER_RESPONDED = "volunteer_responded"

const val VOLUNTEER_RESPONDED_ID = "volunteer_responded_id"