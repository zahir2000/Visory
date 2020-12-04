package com.taruc.visory.jalal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCTypes
import com.taruc.visory.BlindHomeActivity
import com.taruc.visory.R
import com.taruc.visory.VolunteerHomeActivity
import com.taruc.visory.blind.BlindHomeFragment
import com.taruc.visory.blind.ShowLocationActivity
import com.taruc.visory.quickblox.activities.CallActivity
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.utils.*
import com.taruc.visory.ui.GetHelpActivity
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"

class FirebaseService: FirebaseMessagingService() {
    companion object {
        var sharedPref: SharedPreferences? = null

        var token: String?
        get() {
            return sharedPref?.getString("token", "")
        }
        set(value) {
            sharedPref?.edit()?.putString("token", value)?.apply()
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("HelpActivity", "Received!")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        Log.d("HelpActivity", message.from.toString())
        Log.d("HelpActivity", message.data["key"].toString())

        when (message.from) {
            TOPIC -> {
                val intent = Intent(this, ShowLocationActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.data["title"])
                    .setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(notificationId, notification)
            }
            CALL_TOPIC -> {
                val intent = Intent(this, VolunteerHomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("BVI_CALL", message.data["key"].toString())
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.data["title"])
                    .setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTimeoutAfter(60000)
                    .build()

                notificationManager.notify(notificationId, notification)
            }
            CALL_TOPIC_END -> {
                notificationManager.cancelAll()
            }
            else -> {
                val usersFromDb = QbUsersDbManager.allUsers
                val dbSize = usersFromDb.size
                var found = false
                var qbUser: QBUser? = null

                for (i in 0 until dbSize) {
                    Log.d("HelpActivity", usersFromDb[i].login.toString())
                    if (usersFromDb[i].login.toString() == message.data["key"].toString()) {
                        qbUser = usersFromDb[i]
                        found = true
                        break
                    }
                }

                if (found) {
                    Helper.save(VOLUNTEER_RESPONDED_ID, message.data["key"])
                    Log.d("HelpActivity", "VOLUNTEER_RESPONDED_ID is" + message.data["key"])
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.RED
        }
        notificationManager.createNotificationChannel(channel)
    }
}