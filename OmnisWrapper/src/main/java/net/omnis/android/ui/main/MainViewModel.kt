package net.omnis.android.ui.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import net.omnis.android.OmnisController
import net.omnis.android.settings.OMSettings
import net.omnis.android.settings.SettingsActivity
import net.omnis.omnisinterface.IntentHelper
import net.omnis.omnisinterface.SettingNames

class MainViewModel(application: Application) : AndroidViewModel(application) {

	private lateinit var mOmnisController: OmnisController

	init {
		OMSettings.loadSettings(application)
	}

	fun setOmnisController(omnisController: OmnisController) {
		mOmnisController = omnisController
	}


	fun showSettingsScreen(activity: Activity) {
		val intent = Intent(activity, SettingsActivity::class.java)

		IntentHelper.startActivityForResult(activity, 1, intent, null) { requestCode, resultCode, data ->
			if (data!= null) {
				val changedSettings = data.getStringArrayExtra("changedSettings")
				val changedGroups = data.getStringArrayExtra("changedGroups")

				// If any of the changes made were related to offline mode, reinitialise offline mode:
				if (changedGroups?.contains("offlinemode_settings") == true || changedSettings?.containsAnyOf(OMSettings.IDs.OMNIS_WEB_URL) == true)
					mOmnisController.reinitOfflineMode()

				// If the local DB name was changed, reinitialise the local database:
				if (changedSettings?.contains(OMSettings.IDs.LOCAL_DB_NAME) == true)
					mOmnisController.reinitLocalDB()

				// jmg1263: Set the DebugWebview setting (this isn't exposed by default though)
				if (changedSettings?.contains(OMSettings.IDs.DEBUG_WEBVIEW) == true)
					mOmnisController.getOmnisInterface().settings.setSetting(SettingNames.DEBUG_WEBVIEW, OMSettings.getSettingBool(OMSettings.IDs.DEBUG_WEBVIEW) == true)
			}
		}
	}

	/**
	 * Extension function to check if an array contains any of the passed parameters.
	 */
	private fun <T> Array<T>.containsAnyOf(vararg tokens: T): Boolean
	{
		return this.any(tokens::contains)
	}
}
