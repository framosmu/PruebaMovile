package net.omnis.android.settings

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import io.noties.markwon.Markwon

class OMListPreference(context: Context, val setting: Setting) : ListPreference(context) {

	var onChangeHandler: ((newValue: Any?) -> Unit)? = null

	init {
		isPersistent = false
		key = setting.id
		entryValues = setting.options
		entries = setting.optionNames
		title = setting.displayName
		dialogTitle = setting.displayName
		value = setting.value.toString()


		onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
			setting.stringValue = newValue as String
			OMSettings.setSetting(preference.key, newValue, context) // Save the new value
			(preference as ListPreference).value = newValue

			onChangeHandler?.invoke(newValue)
			true
		}

	}

	override fun getSummary(): CharSequence {

		// If the description is set, use that as the summary (parsed as Markdown). Can contain %s etc placeholders for selected value.
		var description = if (!setting.description.isNullOrEmpty())
			setting.description
		else
			"%s" // If no description provided, just display the current display value

		description = description!!.format(getDisplayNameForValue()) // Replace formatting placeholder with current value.

		return Markwon.create(context).toMarkdown(description)
	}


	private fun getDisplayNameForValue(value: Any = this.value): String? {
		if (entryValues != null && entries != null)
			return entries!![entryValues!!.indexOf(value)]?.toString()

		return null
	}

}