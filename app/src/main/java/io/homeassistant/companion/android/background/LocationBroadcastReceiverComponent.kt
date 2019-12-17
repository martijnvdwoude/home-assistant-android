package io.homeassistant.companion.android.background

import dagger.Component
import io.homeassistant.companion.android.common.dagger.AppComponent

@Component(dependencies = [AppComponent::class])
interface LocationBroadcastReceiverComponent {

    fun inject(receiver: LocationBroadcastReceiver)
}
