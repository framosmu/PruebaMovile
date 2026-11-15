package net.omnis.android.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import android.util.TypedValue
import androidx.preference.*
import net.omnis.android.R
import net.omnis.omnisinterface.ResourceRelay
import io.noties.markwon.Markwon

class OMPrefsFragment : PreferenceFragmentCompat() {


	private lateinit var contextThemeWrapper: ContextThemeWrapper
	private val mGroups = mutableMapOf<String, SettingGroup>()

	private var rootGroup: SettingGroup? = null


	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

		preferenceManager.sharedPreferencesName = OMSettings.PREFS_NAME

		val rootPrefsScreen = preferenceManager.createPreferenceScreen(requireContext())
		preferenceScreen = rootPrefsScreen

		val themeTypeValue = TypedValue()
		val themeResId = R.attr.preferenceTheme
		activity?.theme?.resolveAttribute(themeResId, themeTypeValue, true)
		contextThemeWrapper = ContextThemeWrapper(activity, themeTypeValue.resourceId)


		setupActionBar()

		val settingEntries = if (rootGroup != null) rootGroup?.children else OMSettings.getRootSettingEntries()

		settingEntries?.forEach {
			if (it.exposed)
				addSettingEntry(it, preferenceScreen)
		}

	}

	fun setupActionBar() {
		val actionBar = (activity as AppCompatActivity).supportActionBar
		actionBar?.title = arguments?.getString("title") ?: ResourceRelay.getString(requireContext(), "settings_title", "Settings")

		actionBar?.setDisplayHomeAsUpEnabled(true)
	}


	private fun addSettingEntry(settingEntry: SettingEntry, preferenceGroup: PreferenceGroup, dontRecurse: Boolean = false) {
		if (settingEntry is SettingGroup && !dontRecurse) {
			addSettingGroup(settingEntry)
		}
		else if (settingEntry is Setting)
			addSetting(settingEntry, preferenceGroup)
	}


	private fun addSettingGroup(group: SettingGroup) {

		mGroups[group.id] = group // Store a list of the current groups, so that we can access them when we switch to a new group page

		val prefCategory = if (group.newPage) preferenceManager.createPreferenceScreen(contextThemeWrapper) else PreferenceCategory(contextThemeWrapper)
		prefCategory.key = group.id
		prefCategory.title = group.displayName
		prefCategory.isPersistent = false
		if (!group.description.isNullOrEmpty())
			prefCategory.summary = Markwon.create(requireContext()).toMarkdown(group.description!!)

		// It's REALLY IMPORTANT to add Preferences with child Preferences to the Preference Hierarchy first
		// Otherwise, the PreferenceManager will fail to load their keys
		preferenceScreen.addPreference(prefCategory)

		// Now add the individual settings to this group:
		group.children.forEach {
			if (it.exposed)
				addSettingEntry(it, prefCategory, true)
		}


	}

	private fun addSetting(setting: Setting, group: PreferenceGroup?) {

		val pref = if (setting.options != null) {
			createListSetting(setting)
		}
		else when (setting.dataType) {

			Setting.DataType.STRING,
			Setting.DataType.INT,
			Setting.DataType.FLOAT -> createEditTextSetting(setting)

			Setting.DataType.BOOL -> createBoolSetting(setting)

			Setting.DataType.BUTTON -> createButtonSetting(setting)
		}

		if (group != null)
			group.addPreference(pref)
		else
			preferenceScreen.addPreference(pref)
	}

	private fun createEditTextSetting(setting: Setting): OMEditTextPreference {
		val pref =  OMEditTextPreference(contextThemeWrapper, setting)
		pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			addChangedSetting(setting)
			true
		}
		return pref
	}


	private fun createBoolSetting(setting: Setting): SwitchPreferenceCompat {
		val pref = SwitchPreferenceCompat(contextThemeWrapper)
		pref.isPersistent = false
		pref.title = setting.displayName
		pref.isChecked = setting.value as? Boolean ?: false
		pref.key = setting.id
		if (setting.description != null)
			pref.summary = Markwon.create(requireContext()).toMarkdown(setting.description!!)

		pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			setting.value = newValue
			OMSettings.setSetting(setting, requireContext()) // Save the new value of this setting
			addChangedSetting(setting)
			true
		}

		return pref
	}


	private fun createListSetting(setting: Setting): OMListPreference {
		val pref = OMListPreference(contextThemeWrapper, setting)
		pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			addChangedSetting(setting)
			true
		}
		return pref
	}

	private fun createButtonSetting(setting: Setting): OMPrefButton {
		val pref = OMPrefButton(contextThemeWrapper, setting)
		pref.setOnPreferenceChangeListener { _, _ ->
			addChangedSetting(setting)
			true
		}
		return pref
	}

	private fun addChangedSetting(setting: Setting) {
		(activity as SettingsActivity).addChangedSetting(setting.id, setting.groupID)
	}


	override fun onDisplayPreferenceDialog(preference: Preference) {

		// If the preference is one of our custom ones, we need to handle opening it's dialog:
		if (preference is OMEditTextPreference) {
			val dialog = OMEditTextPreference.OMEditTextPreferenceDialog.newInstance(preference.key)
			dialog.setTargetFragment(this, 0)
			dialog.show(requireFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG")
		}
		else
			super.onDisplayPreferenceDialog(preference)
	}

	override fun onNavigateToScreen(preferenceScreen: PreferenceScreen) {
		val omPrefsFragment = OMPrefsFragment()
		omPrefsFragment.rootGroup = mGroups[preferenceScreen.key]

		val args = Bundle()
		args.putString("title", preferenceScreen.title.toString())
		omPrefsFragment.arguments = args
		requireFragmentManager()
					.beginTransaction()
					.replace(id, omPrefsFragment)
					.addToBackStack(null)
					.commit()
	}

}