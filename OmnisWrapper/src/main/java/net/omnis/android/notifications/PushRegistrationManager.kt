/***** Changes ***********
 * Date					Edit				Bug					Description
 * 11-Jul-23		jmg1292			WR/WR/383		Request Notifications permission.
 * 23-Nov-21		jmg1177									Don't attempt to register with Push Notification Server if setting is empty.
 * 22-Nov-21		jmg1175			WR/JS/2802	Updated Push Notification support to work with latest Firebase packages.
 * 02-Jun-17		jmg0594									Added setting to specify Push Notification Server.
 *
 */

package net.omnis.android.notifications

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import net.omnis.android.settings.OMSettings
import net.omnis.omnisinterface.OMDialogs
import net.omnis.omnisinterface.OmnisInterface
import net.omnis.omnisinterface.ResourceRelay
import net.omnis.omnisinterface.omdevice.OMPermissions
import org.json.JSONObject
import java.io.IOException

/**
 * Created by jgissing on 14/12/2016.
 */

class PushRegistrationManager(private val mContext: ComponentActivity) { // jmg1292

	private var tokenSendPending: Boolean
		get() {
			val prefs = mContext.getSharedPreferences("PUSH_SETTINGS", Context.MODE_PRIVATE)
			return prefs.getBoolean(PREF_TOKEN_PENDING, false)
		}
		set(pending) {
			val prefs = mContext.getSharedPreferences("PUSH_SETTINGS", Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.putBoolean(PREF_TOKEN_PENDING, pending)
			editor.apply()
		}

	private var pushNotificationsEnabled: Boolean
		get() {
			val prefs = mContext.getSharedPreferences("PUSH_SETTINGS", Context.MODE_PRIVATE)
			return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, false)
		}
		set(enabled) {
			val prefs = mContext.getSharedPreferences("PUSH_SETTINGS", Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled)
			editor.apply()
		}

	init {
		mContext.lifecycleScope.launch {
			mContext.repeatOnLifecycle(Lifecycle.State.STARTED) {
				NotificationEventBus.events.collect { event ->
					if (event is NotificationEvent.TokenRenewed && pushNotificationsEnabled) {
						sendToken()
					}
				}
			}
		}

		if (pushNotificationsEnabled) {
			enablePushNotifications(true)

			// If we previously failed to send the firebase token to the server, try again now:
			if (tokenSendPending)
				sendToken()
		}
	}

	fun enablePushNotifications(enable: Boolean) {

		// Start jmg1292
		val requiredPermissions = if (enable) arrayOf("android.permission.POST_NOTIFICATIONS") else emptyArray()

		OMPermissions.checkAndRequestPermissions(1, mContext, requiredPermissions, ResourceRelay.getString(mContext, "permission_notifications", "Enable push notifications")) { results ->
			if (results[OMPermissions.ALL_REQUESTED_PERMISSIONS] == true) {

				// Enable/disable the Firebase Messaging Service:
				val pm = mContext.packageManager
				val componentName = ComponentName(mContext, OmnisFirebaseMessagingService::class.java)
				pm.setComponentEnabledSetting(componentName,
					if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP)

				if (enable == pushNotificationsEnabled)
					return@checkAndRequestPermissions  // Nothing more to do, as not changing enabled state.

				// If we just enabled notifications, send the token (in case notifications were disabled when we first got the token).
				if (enable) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
						OmnisNotificationChannels.addChannelsFromSettings(mContext)

					sendToken()
				}

				pushNotificationsEnabled = enable
			}
			else {
				pushNotificationsEnabled = false
			}

			if (!pushNotificationsEnabled) {
				// Notifications are not enabled - delete the instanceID, so Firebase doesn't try to send this new notifications.
				try {
					FirebaseMessaging.getInstance().deleteToken()
				}
				catch (e: IOException) {
					OMDialogs.showOKMessage(mContext, e.localizedMessage, "Error")
				}
			}
		}
		// End jmg1292

	}

	private fun sendToken() {
		tokenSendPending = true
		// Get updated InstanceID token.
		FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
			if (!task.isSuccessful) {
				Log.w(OmnisInterface.TAG, "Fetching FCM registration token failed", task.exception)
				return@OnCompleteListener
			}

			// Get new FCM registration token
			val token = task.result
			Log.i("FCM_TOKEN", "FCM Registration Token: $token")

			SendTokenTask().execute(token)
		})

	}

	private fun sendRegistrationToServer(token: String) {

		// Start jmg1177
		val pushNotificationServer = OMSettings.getSettingString(OMSettings.IDs.PUSH_NOTIFICATION_SERVER)

		if (pushNotificationServer?.length ?: 0 == 0) {
			Log.i(OmnisInterface.TAG, "Push Notification Server setting is not configured.")
			return
		}

		val pushServerUrl = "${pushNotificationServer}/api/pushnotifications/pushnotifications/register" // jmg0594
		// End jmg1177

		val groupsMask = OMSettings.getSettingInt(OMSettings.IDs.PUSH_NOTIFICATION_GROUPMASK)

		// Don't seem to be able to get the Project ID directly. This seems to be the first part of storageBucket though, so get it via that:
		val storageBucket = FirebaseApp.getInstance().options.storageBucket
		val projectName = storageBucket?.substring(0, storageBucket.indexOf("."))

		// Instantiate the RequestQueue.
		val queue = Volley.newRequestQueue(mContext)


		// Send a POST to the Omnis server
		val stringRequest = object : StringRequest(Method.POST, pushServerUrl,
					Response.Listener { response ->
						if (response == "OK")
							tokenSendPending = false
					},
					Response.ErrorListener { Log.e("VOLLEY ERROR", "Error") }
		) {

			override fun getBodyContentType(): String {
				return "application/json"
			}

			@Throws(AuthFailureError::class)
			override fun getBody(): ByteArray? {
				val data = JSONObject()
				try {
					data.put("HWID", OmnisInterface.DEVICE_ID)
					data.put("TOKEN", token)
					data.put("GROUPS_MASK", groupsMask)
					data.put("APP_ID", projectName)
				}
				catch (e: Exception) {
					Log.e(OmnisInterface.TAG, "JSON Exception: " + e.message)
				}


				return data.toString().toByteArray()
			}
		}
		// Add the request to the RequestQueue.
		queue.add(stringRequest)
	}

	private inner class SendTokenTask : AsyncTask<String, Void, Void>() {
		override fun doInBackground(vararg params: String): Void? {
			sendRegistrationToServer(params[0])
			return null
		}
	}


	companion object {

		const val PREF_TOKEN_PENDING = "token_pending"
		const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"

	}
}
