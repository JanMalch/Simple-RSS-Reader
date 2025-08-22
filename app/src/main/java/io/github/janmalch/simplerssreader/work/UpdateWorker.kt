package io.github.janmalch.simplerssreader.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.janmalch.simplerssreader.MainActivity
import io.github.janmalch.simplerssreader.R
import io.github.janmalch.simplerssreader.core.FeedItemRepository
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private const val TAG = "UpdateWorker"

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val itemRepository: FeedItemRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Timber.tag(TAG).d("Starting to update feeds from worker.")
            itemRepository.update()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(
                e,
                "Failed to update feeds. Attempt #%d.",
                runAttemptCount + 1
            )
            if (runAttemptCount < 3) return Result.retry()
        }
        val unreadCount = itemRepository.countUnread()
        Timber.tag(TAG).d("Counted %d unread feed items after updating.", unreadCount)
        try {
            if (unreadCount > 0) {
                createNotificationChannel()
                postNotification(unreadCount)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).w(
                e,
                "Failed to post notification.",
            )
        }
        return if (runAttemptCount <= 3) Result.success() else Result.failure()
    }

    private fun postNotification(unreadCount: Int) {
        val ctx = applicationContext
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // FIXME
            .setContentTitle(ctx.getString(R.string.notification_title))
            .setContentText(ctx.getString(R.string.notification_text, unreadCount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(ctx)) {
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            notify(8697, builder.build())
        }
    }

    private fun createNotificationChannel() {
        val ctx = applicationContext
        val name = ctx.getString(R.string.app_name)
        val descriptionText = ctx.getString(R.string.channel_description)
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object Work {
        private const val CHANNEL_ID = "Simple RSS Reader"
        private const val UNIQUE_WORK_NAME = "SimpleRssReader_UpdateWorker"

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        fun enqueue(context: Context, repeatInterval: Duration) {
            val workManager = WorkManager.getInstance(context)
            Timber.tag(TAG).d("Re-enqueuing periodic work with repeat interval of %s.", repeatInterval)
            val periodicWorkRequest =
                PeriodicWorkRequest.Builder(
                    UpdateWorker::class.java,
                    repeatInterval.toJavaDuration()
                )
                    .addTag("SimpleRssReader")
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        1.minutes.toJavaDuration(),
                    )
                    .build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
        }
    }
}