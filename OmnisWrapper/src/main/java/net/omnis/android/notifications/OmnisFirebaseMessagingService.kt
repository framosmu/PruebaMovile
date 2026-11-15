/***** Changes ***********
 * Date				Edit				Bug					Description
 * 05-Jun-17	jmg0596			WR/WR/272		Crash when sent a notification with no visual component data.
 * 26-May-17	jmg0587			WR/WR/263		Notifications lost some customisation if they were received while the wrapper was open.
 *
 */

package net.omnis.android.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by jgissing on 13/12/2016.
 */
class OmnisFirebaseMessagingService : FirebaseMessagingService() {

	companion object {
		private var nextLocalID = 0 // jmg0587
	}

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		NotificationEventBus.emitEvent(NotificationEvent.TokenRenewed)
	}

	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		super.onMessageReceived(remoteMessage)
		val data = remoteMessage.data
		val notification = remoteMessage.notification

		if (notification != null) {
			// jmg0596: notification may be null if there is no visual component specified (data-only notification)
			OmnisNotification(notification, data, nextLocalID++).showNotification(this) // jmg0587
		}

		NotificationEventBus.emitEvent(NotificationEvent.DataReceived(data))
		Log.d("OmnisService", "Data-only notification received and forwarded via NotificationEventBus.")
	}
}
