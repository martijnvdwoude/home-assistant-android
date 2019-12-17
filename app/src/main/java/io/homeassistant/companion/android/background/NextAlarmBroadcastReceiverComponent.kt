package io.homeassistant.companion.android.background

import dagger.Component
import io.homeassistant.companion.android.common.dagger.AppComponent

@Component(dependencies = [AppComponent::class])
interface NextAlarmBroadcastReceiverComponent {

    fun inject(receiver: NextAlarmBroadcastReceiver)
}