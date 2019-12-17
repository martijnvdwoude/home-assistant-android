package io.homeassistant.companion.android.domain.integration

interface IntegrationRepository {

    suspend fun registerDevice(deviceRegistration: DeviceRegistration)
    suspend fun updateRegistration(deviceRegistration: DeviceRegistration)

    suspend fun isRegistered(): Boolean

    suspend fun registerSensor(sensorRegistration: SensorRegistration)

    suspend fun updateLocation(updateLocation: UpdateLocation)

    suspend fun updateNextAlarm(updateSensor: UpdateSensor)

    suspend fun getZones(): Array<Entity<ZoneAttributes>>

    suspend fun setZoneTrackingEnabled(enabled: Boolean)
    suspend fun isZoneTrackingEnabled(): Boolean

    suspend fun setBackgroundTrackingEnabled(enabled: Boolean)
    suspend fun isBackgroundTrackingEnabled(): Boolean
}
