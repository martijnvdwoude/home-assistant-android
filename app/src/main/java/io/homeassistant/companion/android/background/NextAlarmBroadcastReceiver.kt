package io.homeassistant.companion.android.background

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.homeassistant.companion.android.common.dagger.GraphComponentAccessor
import io.homeassistant.companion.android.domain.integration.IntegrationUseCase
import io.homeassistant.companion.android.domain.integration.UpdateSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class NextAlarmBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NAlarmBroadcastReceiver"
    }

    @Inject
    lateinit var integrationUseCase: IntegrationUseCase

    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onReceive(context: Context, intent: Intent) {
        ensureInjected(context)

        when (intent.action) {
            AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED -> handleUpdate(context)
            else -> Log.w(TAG, "Unknown intent action: ${intent.action}!")
        }
    }

    private fun ensureInjected(context: Context) {
        if (context.applicationContext is GraphComponentAccessor) {
            DaggerNextAlarmBroadcastReceiverComponent.builder()
                .appComponent((context.applicationContext as GraphComponentAccessor).appComponent)
                .build()
                .inject(this)
        } else {
            throw Exception("Application Context passed is not of our application!")
        }
    }

    private fun handleUpdate(context: Context) {
        Log.d(TAG, "Received next alarm update.")
        val updateNextAlarm = UpdateSensor(
            null,
            getNextAlarmTimestamp(context).toString(),
            "sensor",
            "next_alarm"
        )

        mainScope.launch {
            try {
                integrationUseCase.updateNextAlarm(updateNextAlarm)
            } catch (e: Exception) {
                Log.e(TAG, "Could not update next alarm.", e)
            }
        }
    }

    private fun getNextAlarmTimestamp(context: Context) : Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return if(alarmManager.nextAlarmClock == null){
            0
        } else {
            alarmManager.nextAlarmClock.triggerTime / 1000
        }
    }

}
