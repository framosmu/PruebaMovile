package net.omnis.android.settings

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import net.omnis.android.R
import net.omnis.omnisinterface.OmnisInterface
import net.omnis.omnisinterface.Utils
import io.noties.markwon.Markwon

private const val INPUT_TYPE_UNSPECIFIED = -1 // Means set the inputType automatically from the dataType

class OMEditTextPreference(context: Context, val setting: Setting) : DialogPreference(context) {


	init {
		isPersistent = false // Don't use default preference saving to sharedPrefs - we handle this ourselves.
		title = setting.displayName
		dialogTitle = setting.displayName
		dialogMessage = setting.description
		key = setting.id
	}

	override fun getDialogLayoutResource(): Int {
		return R.layout.omedittextpreferencedialog
	}

	override fun getSummary(): CharSequence {
		return setting.stringValue ?: "" // Show the text value as the Summary
	}

	override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
		return a.getString(index) ?: ""
	}


	fun saveValue() {
		summary = setting.stringValue ?: "" // Update the preference summary
		OMSettings.setSetting(setting, context) // Save the value to sharedPrefs (if it can be)
	}


	class OMEditTextPreferenceDialog : PreferenceDialogFragmentCompat() {

		private lateinit var mEditText: AppCompatEditText

		private lateinit var pref: OMEditTextPreference


		companion object {

			fun newInstance(key: String, inputType: Int = INPUT_TYPE_UNSPECIFIED): OMEditTextPreferenceDialog {
				val fragment = OMEditTextPreferenceDialog()
				val b = Bundle(1)
				b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
				b.putInt("inputType", inputType)
				fragment.arguments = b

				return fragment
			}

		}


		override fun onBindDialogView(view: View) {
			super.onBindDialogView(view)

			pref = preference as OMEditTextPreference
			val setting = pref.setting

			val description = view.findViewById<TextView>(R.id.setting_edit_dialog_description)
			description?.movementMethod = ScrollingMovementMethod()

			// Parse & render the description as Markdown:
			if (description != null && setting.description != null)
				Markwon.create(requireContext()).setMarkdown(description, setting.getLocalizedDescription(requireContext())!!)

			val inputType = requireArguments().getInt("inputType", INPUT_TYPE_UNSPECIFIED)


			mEditText = view.findViewById(R.id.setting_edit_dialog_edit)!!
			mEditText.setSelectAllOnFocus(true)

			mEditText.setText(pref.setting.stringValue)

			// Set the dialog's entry field's input type, based on specified value (from xml), or based on data type:
			mEditText.inputType = if (inputType != INPUT_TYPE_UNSPECIFIED)
				inputType
			else when (pref.setting.dataType) {
				Setting.DataType.INT -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
				Setting.DataType.FLOAT -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
				else -> InputType.TYPE_CLASS_TEXT
			}

			Utils.showKeyboard(context, mEditText)
		}

		override fun onDialogClosed(positiveResult: Boolean) {
			Log.d(OmnisInterface.TAG, "Dialog closed: $positiveResult")
			if (positiveResult) {
				val setting = (preference as? OMEditTextPreference)
				setting?.setting?.stringValue = mEditText.text.toString() // Update the setting by assigning its stringValue (this converts to the actual value internally)
				setting?.saveValue()
				preference.callChangeListener(setting)
			}
		}

	}


}