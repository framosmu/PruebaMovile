package net.omnis.android.settings

import android.app.Application
import android.content.Context
import org.w3c.dom.Node
import org.xml.sax.InputSource
import javax.xml.parsers.DocumentBuilderFactory

object OMSettings {

	const val PREFS_NAME = "OMNIS_SETTINGS"

	object IDs { // IDs here should match those in settings.xml
		const val OFFLINE_FORM = "OfflineFormName"
		const val SCAF_NAME = "AppSCAFName"
		const val OMNIS_WEB_URL = "WebServerURL"
		const val ONLINE_FORM = "OnlineForm"
		const val OMNIS_SERVER = "OmnisServer"
		const val OMNIS_PLUGIN = "OmnisPlugin"
		const val START_OFFLINE = "StartInOfflineMode"
		const val LOCAL_DB_NAME = "LocalDBName"
		const val PUSH_NOTIFICATION_SERVER = "PushNotificationServer"
		const val PUSH_NOTIFICATION_GROUPMASK = "PushNotificationGroupMask"
		const val PUSH_NOTIFICATION_CHANNELS_GROUP = "pushnotification_channels"
		const val USE_LOCAL_TIME = "UseLocalTime" // jmg0890

		const val IMMERSIVE_MODE = "ImmersiveMode" // jmg0950
		const val DEBUG_WEBVIEW = "DebugWebview" // jmg1263

		const val MENU_INCLUDE_OFFLINE = "MenuIncludeOffline"
		const val MENU_INCLUDE_UPDATE = "MenuIncludeUpdate"
		const val MENU_INCLUDE_SETTINGS = "MenuIncludeSettings"
		const val MENU_INCLUDE_ABOUT = "MenuIncludeAbout"
	}


	private var mSettings = mutableMapOf<String, Setting>() // A map of the current settings values. Used when querying setting values.
	private var mSettingsEntries = ArrayList<SettingEntry>() // A hierarchical list of Settings and Groups


	fun loadSettings(application: Application) {
		mSettings.clear()
		mSettingsEntries.clear()

		loadXMLSettings(application)
		loadSettingPrefs(application)
	}


	fun getRootSettingEntries(): ArrayList<SettingEntry> {
			return mSettingsEntries
	}

	fun getSetting(settingID: String): Setting? {
		return mSettings[settingID]
	}

	fun setSetting(newValue: Setting, context: Context) {
		mSettings[newValue.id] = newValue
		newValue.saveValue(context)
	}

	/**
	 * Sets the value for this setting, creating it if it doesn't already exist.
	 * Saves the new value to disk.
	 */
	fun setSetting(id: String, newValue: Any?, context: Context) {
		if (mSettings.containsKey(id)) { // Setting already exists
			if (newValue is String) // If we're assigning a string, it may have come from the JS client, in which case we don't want to change the data type
				mSettings[id]?.stringValue = newValue
			else
				mSettings[id]?.value = newValue
		}
		else { // New setting
			val newSetting = Setting(id, "", Setting.dataTypeFromValue(newValue), "")
			newSetting.value = newValue
			mSettings[id] = newSetting
		}

		mSettings[id]?.saveValue(context)
	}

	fun getSettingString(settingID: String): String? {
		return getSetting(settingID)?.stringValue
	}

	fun getSettingBool(settingID: String): Boolean? {
		return getSetting(settingID)?.value as? Boolean
	}

	fun getSettingInt(settingID: String): Int? {
		return getSetting(settingID)?.value as? Int
	}

	fun getSettingFloat(settingID: String): Float? {
		return getSetting(settingID)?.value as? Float
	}

	/**
	 * Get a map of all settings.
	 * @param inGroupId		If supplied, limit the results to the (direct) children of the group with this ID.
	 */
	fun getAllSettingValues(inGroupId: String? = null): Map<String, Any?> {
		val allSettings = mutableMapOf<String, Any?>()

		mSettings.forEach {
			if (inGroupId == null || it.value.groupID == inGroupId)
			allSettings[it.key] = it.value.value
		}

		return allSettings
	}


	/**
	 * Get a list of all of the settings which are direct children of the specified group.
	 */
	fun getSettingsForGroup(groupId: String): ArrayList<Setting> {
		val groupSettings = ArrayList<Setting>()

		mSettings.forEach {
			if (it.value.groupID == groupId)
				groupSettings.add(it.value)
		}

		return groupSettings
	}


	private fun loadXMLSettings(application: Application) {

		val dbFactory = DocumentBuilderFactory.newInstance()
		val dBuilder = dbFactory.newDocumentBuilder()
		var rootNode: Node? = null
		try {
			val assetManager = application.assets
			val inputSource = InputSource(assetManager.open("settings/settings.xml"))

			val document = dBuilder.parse(inputSource)
			rootNode = document.getElementsByTagName("settings").item(0)
		}
		catch (e: Exception) {
			e.printStackTrace()
		}

		if (rootNode != null) {
			mSettingsEntries = SettingEntry.buildChildrenFromNode(rootNode, application)
			mSettings = SettingEntry.getSettingsMapFromEntries(mSettingsEntries)
		}

	}

	private fun loadSettingPrefs(context: Context) {
		val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

		val allSavedPrefs = sharedPrefs.all
		for (pref in allSavedPrefs) {

			if (mSettings.containsKey(pref.key)) { // If the setting was already defined in XML (so is exposed as a setting)
				mSettings[pref.key]?.value = pref.value
			}
			else { // Setting does not exist in XML, so must be a non-exposed setting
				val setting = Setting(pref.key, "", Setting.dataTypeFromValue(pref.value), null)
				setting.value = pref.value
				mSettings[pref.key] = setting
			}
		}

	}


}

