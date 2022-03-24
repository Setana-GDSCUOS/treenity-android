package com.setana.treenity.di

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.ui.loading.LoadingActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    fun provideNotification(@ApplicationContext app: Context): Notification =
        NotificationCompat.Builder(
            app,
            "CHANNEL_ID"
        ).apply {
            val notificationIntent = Intent(app, LoadingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent =
                PendingIntent.getActivity(app, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            setContentTitle("Treenity")
            setContentText("Pedometer service is running")
            setSmallIcon(com.setana.treenity.R.drawable.mypage_settings_alarm_icon)
            priority = NotificationCompat.PRIORITY_HIGH
            setContentIntent(pendingIntent)
            setDefaults(Notification.FLAG_NO_CLEAR) // swipe 해도 지워지지 않음
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 잠금화면에도 보여줌
            val channelName = "Treenity Channel"
            val channelDescription = "Treenity"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                TreenityForegroundService.CHANNEL_ID,
                channelName,
                channelImportance
            ).apply {
                description = channelDescription
            }
            val notificationManager = app.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }.build()
}