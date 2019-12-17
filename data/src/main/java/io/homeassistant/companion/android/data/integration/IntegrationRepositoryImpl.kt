package io.homeassistant.companion.android.data.integration

import io.homeassistant.companion.android.data.LocalStorage
import io.homeassistant.companion.android.domain.authentication.AuthenticationRepository
import io.homeassistant.companion.android.domain.integration.*
import javax.inject.Inject
import javax.inject.Named
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class IntegrationRepositoryImpl @Inject constructor(
    private val integrationService: IntegrationService,
    private val authenticationRepository: AuthenticationRepository,
    @Named("integration") private val localStorage: LocalStorage
) : IntegrationRepository {

    companion object {
        private const val PREF_CLOUD_URL = "cloud_url"
        private const val PREF_REMOTE_UI_URL = "remote_ui_url"
        private const val PREF_SECRET = "secret"
        private const val PREF_WEBHOOK_ID = "webhook_id"
    }

    override suspend fun registerDevice(deviceRegistration: DeviceRegistration) {
        val response =
            integrationService.registerDevice(
                authenticationRepository.buildBearerToken(),
                createRegisterDeviceRequest(deviceRegistration)
            )

        localStorage.putString(PREF_CLOUD_URL, response.cloudhookUrl)
        localStorage.putString(PREF_REMOTE_UI_URL, response.remoteUiUrl)
        localStorage.putString(PREF_SECRET, response.secret)
        localStorage.putString(PREF_WEBHOOK_ID, response.webhookId)

        val nextAlarmSensorRegistration = SensorRegistration(
            null,
            "timestamp",
            "mdi:alarm",
            "Next Alarm",
            "0",
            "sensor",
            "next_alarm",
            null
        )
        try {
            registerSensor(nextAlarmSensorRegistration)
        } catch (e: Exception) {

        }
    }

    override suspend fun isRegistered(): Boolean {
        return localStorage.getString(PREF_WEBHOOK_ID) != null
    }

    override suspend fun registerSensor(sensorRegistration: SensorRegistration) {
        for (it in getUrls()) {
            var wasSuccess = false
            try {
                wasSuccess = integrationService.updateIntegration(
                    it,
                    createRegisterSensorRequest(sensorRegistration)
                ).isSuccessful
            } catch (e: Exception) {
                // Ignore failure until we are out of URLS to try!
            }
            // if we had a successful call we can return
            if (wasSuccess)
                return
        }

        throw IntegrationException()
    }

    override suspend fun updateLocation(updateLocation: UpdateLocation) {
        val updateLocationRequest = createUpdateLocation(updateLocation)
        for (it in getUrls()) {
            var wasSuccess = false
            try {
                wasSuccess = integrationService.updateIntegration(it, updateLocationRequest).isSuccessful
            } catch (e: Exception) {
                // Ignore failure until we are out of URLS to try!
            }
            // if we had a successful call we can return
            if (wasSuccess)
                return
        }

        throw IntegrationException()
    }

    override suspend fun updateNextAlarm(updateSensor: UpdateSensor) {
        val updateNextAlarmRequest = createUpdateNextAlarm(updateSensor)
        for (it in getUrls()) {
            var wasSuccess = false
            try {
                wasSuccess = integrationService.updateIntegration(it, updateNextAlarmRequest).isSuccessful
            } catch (e: Exception) {
                // Ignore failure until we are out of URLS to try!
            }
            // if we had a successful call we can return
            if (wasSuccess)
                return
        }

        throw IntegrationException()
    }

    // https://developers.home-assistant.io/docs/en/app_integration_sending_data.html#short-note-on-instance-urls
    private suspend fun getUrls(): Array<HttpUrl> {
        val retVal = ArrayList<HttpUrl>()
        val webhook = localStorage.getString(PREF_WEBHOOK_ID)

        localStorage.getString(PREF_CLOUD_URL)?.let {
            retVal.add(it.toHttpUrl())
        }

        localStorage.getString(PREF_REMOTE_UI_URL)?.let {
            retVal.add(
                it.toHttpUrl().newBuilder()
                    .addPathSegments("api/webhook/$webhook")
                    .build()
            )
        }

        authenticationRepository.getUrl().toString().let {
            retVal.add(
                it.toHttpUrl().newBuilder()
                    .addPathSegments("api/webhook/$webhook")
                    .build()
            )
        }

        return retVal.toTypedArray()
    }

    private fun createRegisterDeviceRequest(deviceRegistration: DeviceRegistration): RegisterDeviceRequest {
        return RegisterDeviceRequest(
            deviceRegistration.appId,
            deviceRegistration.appName,
            deviceRegistration.appVersion,
            deviceRegistration.deviceName,
            deviceRegistration.manufacturer,
            deviceRegistration.model,
            deviceRegistration.osName,
            deviceRegistration.osVersion,
            deviceRegistration.supportsEncryption,
            deviceRegistration.appData
        )
    }

    private fun createRegisterSensorRequest(sensorRegistration: SensorRegistration): IntegrationRequest {
        return IntegrationRequest(
            "register_sensor",
            SensorRegistration(
                sensorRegistration.attributes,
                sensorRegistration.deviceClass,
                sensorRegistration.icon,
                sensorRegistration.name,
                sensorRegistration.state,
                sensorRegistration.type,
                sensorRegistration.uniqueId,
                sensorRegistration.unitOfMeasurement
            )
        )
    }

    private fun createUpdateLocation(updateLocation: UpdateLocation): IntegrationRequest {
        return IntegrationRequest(
            "update_location",
            UpdateLocationRequest(
                updateLocation.locationName,
                updateLocation.gps,
                updateLocation.gpsAccuracy,
                updateLocation.battery,
                updateLocation.speed,
                updateLocation.altitude,
                updateLocation.course,
                updateLocation.verticalAccuracy
            )
        )
    }

    private fun createUpdateNextAlarm(updateSensor: UpdateSensor): IntegrationRequest {
        return IntegrationRequest(
            "update_sensor_states",
            listOf(UpdateSensor(
                updateSensor.attributes,
                updateSensor.state,
                updateSensor.type,
                updateSensor.uniqueId
            ))
        )
    }
}
