package io.homeassistant.companion.android.domain.integration

import javax.inject.Inject

class IntegrationUseCaseImpl @Inject constructor(
    private val integrationRepository: IntegrationRepository
) : IntegrationUseCase {

    override suspend fun registerDevice(deviceRegistration: DeviceRegistration) {
        integrationRepository.registerDevice(deviceRegistration)
    }

    override suspend fun isRegistered(): Boolean {
        return integrationRepository.isRegistered()
    }

    override suspend fun registerSensor(sensorRegistration: SensorRegistration) {
        return integrationRepository.registerSensor(sensorRegistration)
    }

    override suspend fun updateLocation(updateLocation: UpdateLocation) {
        return integrationRepository.updateLocation(updateLocation)
    }

    override suspend fun updateNextAlarm(updateSensor: UpdateSensor) {
        return integrationRepository.updateNextAlarm(updateSensor)
    }
}
