/***** Changes ***********
 * Date				Edit				Bug					Description
 * 22-Nov-21	jmg1175			WR/JS/2802	Updated Push Notification support to work with latest Firebase packages.
 * 06-Jun-17	jmg0600			WR/WR/273		Push Notification 'tag' was not respected when notification received with app in foreground.
 * 05-Jun-17	jmg0597			WR/WR/271		Default & No sounds were mixed up when notification received in the foreground.
 * 02-Jun-17	jmg0593			WR/WR/270		Extraneous Firebase data was returned to Omnis with the notification data payload.
 * 01-Jun-17	jmg0592			WR/WR/270		Extraneous Firebase data was returned to Omnis with the notification data payload.
 * 26-May-17	jmg0587			WR/WR/263		Notifications lost some customisation if they were received while the wrapper was open.
 *
 */

package net.omnis.android.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.RemoteMessage
import net.omnis.android.MainActivity

import net.omnis.omnisinterface.ResourceRelay

/**
 * jmg0587: Created by jgissing on 26/05/2017.
 */

class OmnisNotification {


	private var message = ""
	private var title = ""
	private var resultActivity: Class<*> = MainActivity::class.java
	private var dataPayload: Map<String, String>? = null
	private var color: String? = null
	private var icon: String? = null
	private var sound: String? = null
	private var tag: String? = null // jmg0600
	private var id = 0
	private var channelId: String = OmnisNotificationChannels.DEFAULT_CHANNEL_ID

	constructor(firebaseNotification: RemoteMessage.Notification, data: Map<String, String>, id: Int) {
		title = firebaseNotification.title ?: ""
		message = firebaseNotification.body ?: ""
		color = firebaseNotification.color
		icon = firebaseNotification.icon
		sound = firebaseNotification.sound
		channelId = firebaseNotification.channelId ?: OmnisNotificationChannels.DEFAULT_CHANNEL_ID
		dataPayload = data
		tag = firebaseNotification.tag // jmg0600
		this.id = id
	}

	constructor(message: String, title: String, data: Map<String, String>, id: Int) {
		this.title = title
		this.message = message
		this.dataPayload = data
		this.id = id
	}

	fun setSound(sound: String) {
		this.sound = sound
	}

	fun setColor(colorString: String) {
		this.color = colorString
	}

	fun setIcon(iconName: String) {
		this.icon = iconName
	}

	fun setTag(tag: String) {
		this.tag = tag
	} // jmg0600

	fun setResultActivity(resultActivityClass: Class<*>) {
		this.resultActivity = resultActivityClass
	}


	fun showNotification(context: Context) {
		OmnisNotificationChannels.addChannelsFromSettings(context)
		val intent = Intent(context, resultActivity)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)


		if (dataPayload != null && !dataPayload!!.keys.isEmpty()) {
			intent.putExtra("_OMNIS_NOTIFICATION_", "true")
			for (key in dataPayload!!.keys) {
				intent.putExtra(key, dataPayload!![key])
			}
		}

		// Start jmg1175
		var flags = PendingIntent.FLAG_ONE_SHOT
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flags = flags or PendingIntent.FLAG_IMMUTABLE
		}

		val pendingIntent = PendingIntent.getActivity(
			context, id /* Request code */, intent,
			flags
		)
		// End jmg1175

		val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
		val notificationBuilder = NotificationCompat.Builder(context, channelId)
					.setSmallIcon(ResourceRelay.getID(context, "ic_notification", "drawable"))
					.setContentTitle(title)
					.setContentText(message)
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)

		if (sound != null)
		// jmg0597
			notificationBuilder.setSound(if (sound == "default") defaultSoundUri else Uri.parse("android.resource://" + context.packageName + "/raw/" + sound)) // jmg0597

		if (icon != null)
			notificationBuilder.setSmallIcon(ResourceRelay.getID(context, icon!!, "drawable"))

		if (color != null)
			notificationBuilder.setColor(Color.parseColor(color))

		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		// Start jmg0600
		var notificationId = id

		// If a tag was specified, set the ID to 0 (only 1 notification should be shown for each tag).
		if (tag != null)
			notificationId = 0

		notificationManager.notify(tag, notificationId, notificationBuilder.build())
		// End jmg0600
	}

	companion object {

		private val reservedDataPayloadKeys = arrayOf(// jmg0593
					"from", "notification", "collapse_key")

		// jmg0593
		fun removeExtraDataFromPayload(data: Bundle): Bundle {

			// Remove any keys which match entries in reservedDataPayloadKeys:
			for (key in reservedDataPayloadKeys) {
				if (data.containsKey(key))
					data.remove(key)
			}

			// Also remove any keys which Start with "google." or "gcm.":
			val it = data.keySet().iterator()
			while (it.hasNext()) {
				val key = it.next()
				if (key.startsWith("google.") || key.startsWith("gcm."))
					it.remove()
			}

			return data
		}
	}
}
