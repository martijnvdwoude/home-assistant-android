package io.homeassistant.companion.android.launch

import android.os.Build
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import io.homeassistant.companion.android.BuildConfig
import io.homeassistant.companion.android.domain.authentication.AuthenticationUseCase
import io.homeassistant.companion.android.domain.authentication.SessionState
import io.homeassistant.companion.android.domain.integration.IntegrationUseCase
import io.homeassistant.companion.android.notifications.MessagingService
import java.lang.Exception
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LaunchPresenterImpl @Inject constructor(
    private val view: LaunchView,
    private val authenticationUseCase: AuthenticationUseCase,
    private val integrationUseCase: IntegrationUseCase
) : LaunchPresenter {

    companion object {
        const val TAG = "LaunchPresenter"
    }

    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onViewReady() {
        mainScope.launch {
            if (authenticationUseCase.getSessionState() == SessionState.CONNECTED) {
                resyncNotificationIds()
                view.displayWebview()
            } else {
                view.displayOnBoarding()
            }
        }
    }

    override fun onFinish() {
        mainScope.cancel()
    }

    // TODO: This should probably go in settings?
    private fun resyncNotificationIds() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            mainScope.launch {
                try {
                    integrationUseCase.updateRegistration(
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        Build.MODEL ?: "UNKNOWN",
                        Build.MANUFACTURER ?: "UNKNOWN",
                        Build.MODEL ?: "UNKNOWN",
                        Build.VERSION.SDK_INT.toString(),
                        MessagingService.generateAppData(it.token)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Issue updating Registration", e)
                }
            }
        }
    }
}
