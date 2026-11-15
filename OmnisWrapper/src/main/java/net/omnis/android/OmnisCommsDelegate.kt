package net.omnis.android

import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes
import net.omnis.android.settings.OMSettings
import net.omnis.android.notifications.PushRegistrationManager
import net.omnis.omnisinterface.Features
import net.omnis.omnisinterface.OMCommsDelegate
import net.omnis.omnisinterface.OmnisInterface
import org.json.JSONObject

class OmnisCommsDelegate(omnisInterface: OmnisInterface): OMCommsDelegate {

	private var mPushRegistrationManager: PushRegistrationManager? = null

	companion object {
		private const val PREF_SETTING_PREFIX = "_SETTING_"
	}

	init {
		if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(omnisInterface.activityContext) == CommonStatusCodes.SUCCESS) {
			omnisInterface.features.addFeature(Features.PUSH_NOTIFICATIONS)
			mPushRegistrationManager = PushRegistrationManager(omnisInterface.activityContext)
		}
	}

	override fun messageFromJSClient(data: JSONObject, omnisInterface: OmnisInterface): Boolean {
		when (data["ID"]) {

			OMCommsDelegate.ACTION_LOAD_PREF -> {
				val dataObj = data["Data"] as JSONObject
				var key = dataObj["key"] as? String ?: ""

				if (key.startsWith(PREF_SETTING_PREFIX)) {
					key = key.substringAfter(PREF_SETTING_PREFIX)
					val setting = OMSettings.getSetting(key)
					omnisInterface.callJS("window.wrapperInterface.setInstanceVar(${dataObj.getString("omnisIndex")}, ${dataObj.getString("formIndex")}, ${dataObj.getString("dataIndex")}, \"${setting?.stringValue}\");")
					return true
				}
				// Else fall through to base-class implementation
			}

			OMCommsDelegate.ACTION_SAVE_PREF -> {
				val dataObj = data.getJSONObject("Data")
				var key = dataObj.getString("key")

				if (key.startsWith(PREF_SETTING_PREFIX)) {
					key = key.substringAfter(PREF_SETTING_PREFIX)
					val newStringValue = dataObj.getString("value")

					OMSettings.setSetting(key, newStringValue, omnisInterface.activityContext)
					return true
				}
				// Else fall through to base-class implementation
			}

			OMCommsDelegate.ACTION_ENABLE_PUSH_NOTIFICATIONS -> {
				val enable = data.getBoolean("Data")
				mPushRegistrationManager?.enablePushNotifications(enable)
			}
		}

		return false
	}
}