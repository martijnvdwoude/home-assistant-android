package io.homeassistant.companion.android.domain.integration

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Dictionary
import java.util.Objects

data class SensorRegistration(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val attributes: Dictionary<String, Objects>?,
    val deviceClass: String?,
    val icon: String?,
    val name: String,
    val state: String,
    val type: String,
    val uniqueId: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val unitOfMeasurement: String?
)