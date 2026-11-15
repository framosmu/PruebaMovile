package net.omnis.android.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class NotificationEvent {
	data object TokenRenewed : NotificationEvent()
	data class DataReceived(val data: Map<String, String>) : NotificationEvent()
}

object NotificationEventBus {
	private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 10)
	val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

	fun emitEvent(event: NotificationEvent) {
		_events.tryEmit(event)
	}
}
