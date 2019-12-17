package io.homeassistant.companion.android.domain.integration

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Dictionary
import java.util.Objects

data class UpdateSensor(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val attributes: Dictionary<String, Objects>?,
    val state: String,
    val type: String,
    val uniqueId: String
)