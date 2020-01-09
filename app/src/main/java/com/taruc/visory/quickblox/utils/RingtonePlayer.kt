package com.taruc.visory.quickblox.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import java.io.IOException

class RingtonePlayer {

    private var mediaPlayer: MediaPlayer? = null

    constructor(context: Context) {
        val notification = getNotification()
        if (notification != null) {
            mediaPlayer = MediaPlayer()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributesBuilder = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)

                mediaPlayer?.setAudioAttributes(audioAttributesBuilder.build())
            } else {
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_RING)
            }

            try {
                mediaPlayer?.setDataSource(context, notification)
                mediaPlayer?.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    constructor(context: Context, resource: Int) {
        val beepUri = Uri.parse("android.resource://" + context.packageName + "/" + resource)
        mediaPlayer = MediaPlayer()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributesBuilder = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)

            mediaPlayer?.setAudioAttributes(audioAttributesBuilder.build())
        } else {
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
        }

        try {
            mediaPlayer?.setDataSource(context, beepUri)
            mediaPlayer?.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getNotification(): Uri? {
        var notification: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (notification == null) {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
        return notification
    }

    fun play(looping: Boolean) {
        mediaPlayer?.let {
            it.isLooping = looping
            it.start()
        } ?: run {}
    }

    @Synchronized
    fun stop() {
        mediaPlayer?.let {
            try {
                it.stop()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            it.release()
            mediaPlayer = null
        }
    }
}