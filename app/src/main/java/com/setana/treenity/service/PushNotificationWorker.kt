package com.setana.treenity.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@HiltWorker
class PushNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val treeRepository: TreeRepository
) : CoroutineWorker(appContext, workerParams) {

    private val cancellationTokenSource = CancellationTokenSource()
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private var appContext = appContext

    companion object {
        const val WORK_NAME = "PERIODIC_PUSH_NOTIFICATION_WORK"
        const val CHANNEL_ID = "treenity_push"
        const val NOTIFICATION_ID = 1
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result  = coroutineScope {
        withContext(Dispatchers.IO){Log.d("bimoon", "doWork: Success function called")
            val result = runCatching {
                //val fusedLocationClient = fusedLocationClient
                val task = fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS)
            }
            Log.d("bimoon", "result :$result")
            val location = result.getOrNull()
            val userId = PREFS.getLong(USER_ID_KEY, -1)
            Log.d("bimoon", "branch :$userId and $location")
            return@withContext if (userId != -1L && location != null) {
                val response = treeRepository.getAroundTrees(location.latitude, location.longitude, userId)
                Log.d("bimoon", "response :$response")
                if (response.isSuccessful) {
                    response.body()?.let { treeList ->
                        showNotification(treeList.size, treeList.maxOf { it.distance }.toInt())
                    }
                    Result.success()
                } else {
                    Result.failure()
                }
            } else {
                Result.failure()
            }
        }
    }

    private fun showNotification(numTrees: Int, maxDistance: Int) {
        val intent = Intent(appContext, LoadingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            appContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.store_tree_icon)
            .setContentTitle("Treenity Alarm")
            .setContentText("There are $numTrees trees within ${maxDistance}M") // TODO text 안의 ~는 각각 변수와 숫자로 대입해주세요 :)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val channelName = "Channel Name"
        val channelDescription = "Channel Description"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
            description = channelDescription
        }

        val notificationManager = appContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.createNotificationChannel(channel)

        with(NotificationManagerCompat.from(appContext)) {
            notify(NOTIFICATION_ID, notification.build())
        }

    }
}