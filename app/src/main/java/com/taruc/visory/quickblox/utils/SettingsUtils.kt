package com.taruc.visory.quickblox.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.quickblox.videochat.webrtc.QBRTCConfig
import com.quickblox.videochat.webrtc.QBRTCMediaConfig
import com.taruc.visory.R

private const val LIMIT_MEMBERS = 4

private fun setSettingsForMultiCall(users: List<Int>) {
    if (users.size <= LIMIT_MEMBERS) {
        setDefaultVideoQuality()
    } else {
        //set to minimum settings
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width)
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height)
        QBRTCMediaConfig.setVideoHWAcceleration(false)
        QBRTCMediaConfig.setVideoCodec(null)
    }
}

fun setSettingsStrategy(users: List<Int>, sharedPref: SharedPreferences, context: Context) {
    setCommonSettings(sharedPref, context)
    if (users.size == 1) {
        setSettingsFromPreferences(sharedPref, context)
    } else {
        setSettingsForMultiCall(users)
    }
}

fun configRTCTimers(context: Context) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    val answerTimeInterval = getPreferenceInt(sharedPref, context,
        R.string.pref_answer_time_interval_key,
        R.string.pref_answer_time_interval_default_value).toLong()
    QBRTCConfig.setAnswerTimeInterval(10)

    val disconnectTimeInterval = getPreferenceInt(sharedPref, context,
        R.string.pref_disconnect_time_interval_key,
        R.string.pref_disconnect_time_interval_default_value)
    QBRTCConfig.setDisconnectTime(disconnectTimeInterval)

    val dialingTimeInterval = getPreferenceInt(sharedPref, context,
        R.string.pref_dialing_time_interval_key,
        R.string.pref_dialing_time_interval_default_value).toLong()
    QBRTCConfig.setDialingTimeInterval(dialingTimeInterval)
}

fun isManageSpeakerPhoneByProximity(context: Context): Boolean {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    return sharedPref.getBoolean(context.getString(R.string.pref_manage_speakerphone_by_proximity_key),
        java.lang.Boolean.valueOf(context.getString(R.string.pref_manage_speakerphone_by_proximity_default)))
}

fun getPreferenceInt(sharedPref: SharedPreferences, context: Context, strResKey: Int, strResDefValue: Int): Int {
    return sharedPref.getInt(context.getString(strResKey), Integer.valueOf(context.getString(strResDefValue)))
}

private fun setCommonSettings(sharedPref: SharedPreferences, context: Context) {
    val audioCodecDescription = getPreferenceString(sharedPref, context, R.string.pref_audiocodec_key,
        R.string.pref_audiocodec_def)
    val audioCodec = if (QBRTCMediaConfig.AudioCodec.ISAC.description == audioCodecDescription) {
        QBRTCMediaConfig.AudioCodec.ISAC
    } else {
        QBRTCMediaConfig.AudioCodec.OPUS
    }
    QBRTCMediaConfig.setAudioCodec(audioCodec)

    // Check Disable built-in AEC flag.
    val disableBuiltInAEC = getPreferenceBoolean(sharedPref, context,
        R.string.pref_disable_built_in_aec_key,
        R.string.pref_disable_built_in_aec_default)
    QBRTCMediaConfig.setUseBuildInAEC(!disableBuiltInAEC)

    // Check Disable Audio Processing flag.
    val noAudioProcessing = getPreferenceBoolean(sharedPref, context,
        R.string.pref_noaudioprocessing_key,
        R.string.pref_noaudioprocessing_default)
    QBRTCMediaConfig.setAudioProcessingEnabled(!noAudioProcessing)

    // Check OpenSL ES enabled flag.
    val useOpenSLES = getPreferenceBoolean(sharedPref, context,
        R.string.pref_opensles_key,
        R.string.pref_opensles_default)
    QBRTCMediaConfig.setUseOpenSLES(useOpenSLES)
}

private fun setSettingsFromPreferences(sharedPref: SharedPreferences, context: Context) {

    // Check HW codec flag.
    val hwCodec = sharedPref.getBoolean(context.getString(R.string.pref_hwcodec_key),
        java.lang.Boolean.valueOf(context.getString(R.string.pref_hwcodec_default)))

    QBRTCMediaConfig.setVideoHWAcceleration(hwCodec)

    // Get video resolution from settings.
    val resolutionDefaultValue = "0"
    val resolutionItem = Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_resolution_key), resolutionDefaultValue)
        ?: resolutionDefaultValue)
    setVideoQuality(resolutionItem)

    // Get start bitrate.
    val startBitrate = getPreferenceInt(sharedPref, context,
        R.string.pref_startbitratevalue_key,
        R.string.pref_startbitratevalue_default)
    QBRTCMediaConfig.setVideoStartBitrate(startBitrate)

    val videoCodecDefaultValue = "0"
    val videoCodecItem = Integer.parseInt(getPreferenceString(sharedPref, context, R.string.pref_videocodec_key, videoCodecDefaultValue)
        ?: videoCodecDefaultValue)
    for (codec in QBRTCMediaConfig.VideoCodec.values()) {
        if (codec.ordinal == videoCodecItem) {
            QBRTCMediaConfig.setVideoCodec(codec)
            break
        }
    }

    // Get camera fps from settings.
    val cameraFps = getPreferenceInt(sharedPref, context, R.string.pref_frame_rate_key, R.string.pref_frame_rate_default)
    QBRTCMediaConfig.setVideoFps(cameraFps)
}

private fun setVideoQuality(resolutionItem: Int) {
    if (resolutionItem != -1) {
        setVideoFromLibraryPreferences(resolutionItem)
    } else {
        setDefaultVideoQuality()
    }
}

private fun setDefaultVideoQuality() {
    QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width)
    QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height)
}

private fun setVideoFromLibraryPreferences(resolutionItem: Int) {
    for (quality in QBRTCMediaConfig.VideoQuality.values()) {
        if (quality.ordinal == resolutionItem) {
            QBRTCMediaConfig.setVideoHeight(quality.height)
            QBRTCMediaConfig.setVideoWidth(quality.width)
        }
    }
}

private fun getPreferenceString(sharedPref: SharedPreferences, context: Context, strResKey: Int, strResDefValue: Int): String {
    val defaultValue = context.getString(strResDefValue)
    return sharedPref.getString(context.getString(strResKey), defaultValue) ?: defaultValue
}

private fun getPreferenceString(sharedPref: SharedPreferences, context: Context, strResKey: Int, strResDefValue: String): String? {
    return sharedPref.getString(context.getString(strResKey), strResDefValue)
}

private fun getPreferenceBoolean(sharedPref: SharedPreferences, context: Context, StrRes: Int, strResDefValue: Int): Boolean {
    return sharedPref.getBoolean(context.getString(StrRes), java.lang.Boolean.valueOf(context.getString(strResDefValue)))
}