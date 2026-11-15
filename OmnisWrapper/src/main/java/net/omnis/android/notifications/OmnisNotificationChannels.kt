package net.omnis.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import net.omnis.android.settings.OMSettings
import net.omnis.omnisinterface.ResourceRelay

object OmnisNotificationChannels {

	const val DEFAULT_CHANNEL_ID = "channel_default"

	/**
	 * Registers the push notification channels defined in settings.xml.
	 */
	fun addChannelsFromSettings(context: Context) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

			val channels = OMSettings.getSettingsForGroup(OMSettings.IDs.PUSH_NOTIFICATION_CHANNELS_GROUP)

			channels.forEach { setting ->

				val name = if (setting.stringValue?.startsWith("@string/") == true) {
					val stringID = setting.stringValue!!.substringAfter("@string/")
					ResourceRelay.getString(context, stringID, stringID)
				}
				else
					setting.stringValue

				val description = if (setting.description?.startsWith("@string/") == true) {
					val stringID = setting.description!!.substringAfter("@string/")
					ResourceRelay.getString(context, stringID, stringID)
				}
				else
					setting.description ?: ""

				val importance = setting.otherData?.get("importance")?.toInt() ?: NotificationManager.IMPORTANCE_DEFAULT

				// Create the NotificationChannel
				val channel = NotificationChannel(setting.id, name, importance)
				channel.description = description

				/** You can use the setting.otherData to pass data in to set other aspects of the Channel, if needed. */

				// Register the channel with the system; you can't change the importance
				// or other notification behaviors after this
				notificationManager.createNotificationChannel(channel)
			}
		}
	}

}