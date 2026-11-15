package net.omnis.android.settings

import android.content.Context
import android.util.Log
import android.widget.Button
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.omnis.android.OmnisOfflineController
import net.omnis.android.R
import net.omnis.omnisinterface.OMDialogs
import net.omnis.omnisinterface.offline.ScafHandler

class OMPrefButton(context: Context, val setting: Setting): Preference(context) {

	init {
		isPersistent = false // Don't use default preference saving to sharedPrefs - we handle this ourselves.
		title = setting.displayName
		key = setting.id

		layoutResource = R.layout.ombuttonpreference
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		val button = holder.findViewById(R.id.omprefbutton) as? Button
		button?.text = setting.displayName
		button?.setOnClickListener {
			onButtonClicked { hasChanged ->
				if (hasChanged)
					callChangeListener(null)
			}
		}
	}

	/**
	 * Any custom Pref buttons added to settings should have their click events handled here
	 */
	fun onButtonClicked(callback: (hasChanged: Boolean) -> Unit) {

		when (setting.id) {
			"clear_offline_files" -> {
				OMDialogs.showYesNoMessage(context, R.string.setting_button_clear_offline_files_message, R.string.setting_button_clear_offline_files, {
					ScafHandler.deleteOfflineFiles(OmnisOfflineController.OFFLINE_FILES_FOLDER, context)
					callback(true)
				})
			}

			else -> {
				Log.w("OMNIS PREF BUTTON", "Unhandled setting button click event: ${setting.stringValue}")
				return
			}

		}
	}

}