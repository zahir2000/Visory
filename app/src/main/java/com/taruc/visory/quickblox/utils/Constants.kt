package com.taruc.visory.quickblox.utils

import android.Manifest

val PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

const val MAX_OPPONENTS_COUNT = 6

const val EXTRA_LOGIN_RESULT = "login_result"

const val STOP_CALLING = "stop_calling"

const val HANG_UP = "hang_up"

const val CHECK_PERMISSIONS = "check_permissions"

const val CONNECTED_TO_USER = "connected_to_user"

const val EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message"

const val EXTRA_LOGIN_RESULT_CODE = 1002

const val EXTRA_IS_INCOMING_CALL = "conversation_reason"

const val MAX_LOGIN_LENGTH = 15

const val MAX_FULLNAME_LENGTH = 20

const val SERVICE_ID = 787

const val CHANNEL_ID = "Quickblox channel"

const val CHANNEL_NAME = "Quickblox background service"

const val EXTRA_COMMAND_TO_SERVICE = "command_for_service"

const val EXTRA_QB_USER = "qb_user"

const val COMMAND_NOT_FOUND = 0

const val COMMAND_LOGIN = 1

const val COMMAND_LOGOUT = 2

const val EXTRA_PENDING_INTENT = "pending_Intent"