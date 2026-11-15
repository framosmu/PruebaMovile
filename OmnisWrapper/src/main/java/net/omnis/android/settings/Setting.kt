package net.omnis.android.settings

import android.content.Context
import android.util.Log
import net.omnis.omnisinterface.OmnisInterface
import net.omnis.omnisinterface.ResourceRelay
import org.w3c.dom.Node

class Setting(val id: String, val displayName: String, var dataType: DataType, stringValue: String?, exposed: Boolean = false) : SettingEntry(exposed) {

	var groupID: String = ""
	var preventDataTypeChanges = false
	var otherData: Map<String, String?>? = null

	var value: Any? = null
		set(value) {
			field = value
			if (!preventDataTypeChanges)
				dataType = dataTypeFromValue(value)
		}

	var stringValue = stringValue
		set (value) {
			field = value
			if (value != null)
				this.value = stringValueToActual(value)
			else
				this.value = null
		}
		get() {
			return this.valueToString()
		}

	var description: String? = null
	var options: Array<String>? = null
		private set(value) {
			field = value
			optionCount = value?.size ?: 0
			optionNames = value // Default optionNames to the optionValues. So this must be set before optionNames
		}
	private var optionCount = 0

	var optionNames: Array<String>? = null

	init {
		if (dataType == DataType.BUTTON)
			preventDataTypeChanges = true // Button types should ignore the data type of their value - they should always be buttons.

		if (stringValue != null)
			value = stringValueToActual(stringValue)
	}

	fun stringValueToActual(stringValue: String): Any? {
		return when (dataType) {
			DataType.INT ->
				stringValue.toIntOrNull()

			DataType.BOOL ->
				try {
					stringValue.toInt() > 0
				}
				catch (e: Exception) {
					stringValue.lowercase() == "true"
				}

			DataType.FLOAT ->
				stringValue.toFloatOrNull()

			else ->
				stringValue
		}
	}

	private fun valueToString(): String {
		return value?.toString() ?: ""
	}

	fun saveValue(context: Context) {

		val sharedPrefs = context.getSharedPreferences(OMSettings.PREFS_NAME, Context.MODE_PRIVATE)
		val editor = sharedPrefs.edit()

		try {
			when (dataType) {
				DataType.STRING -> editor.putString(id, value as String)
				DataType.BOOL -> editor.putBoolean(id, value as Boolean)
				DataType.INT -> editor.putInt(id, value as Int)
				DataType.FLOAT -> editor.putFloat(id, value as Float)
				else -> {
					// Ignore
				}
			}
		}
		catch (e: Exception) {
			Log.e(OmnisInterface.TAG, "Setting.saveValue() value $value does not match dataType: $dataType")
		}

		editor.apply()
	}

	fun getLocalizedDescription(context: Context): String? {
		return ResourceRelay.getString(context, description, description)
	}


	enum class DataType {
		INT,
		STRING,
		BOOL,
		FLOAT,
		BUTTON
	}


	companion object {

		fun dataTypeFromValue(value: Any?): DataType {
			return when (value) {
				is Int -> DataType.INT
				is Float -> DataType.FLOAT
				is String -> DataType.STRING
				is Boolean -> DataType.BOOL
				null -> throw Exception("A Setting's value was set to null - this is not allowed")
				else -> throw Exception("Data type ${value::class.java} not supported for Omnis Settings")
			}
		}

		fun buildFromNode(node: Node, context: Context): Setting? {

			try {
				if (node.nodeName == "setting") {
					val dataType = node.attributes.getNamedItem("dataType")?.nodeValue ?: DataType.STRING.name
					val displayName = getLocalizedString(context,
								node.attributes.getNamedItem("displayName")?.nodeValue ?: "")
					var stringValue = ""
					var description: String? = null
					var options: ArrayList<String>? = null
					var optionsNames: ArrayList<String>? = null
					var otherData: MutableMap<String, String?>? = null

					val isExposed = if (node.attributes.getNamedItem("exposed") != null)
						node.attributes.getNamedItem("exposed").nodeValue == "true"
					else // If the 'exposed' attribute is not set, mark it as exposed if the displayName is not empty
						displayName.isNotEmpty()

					// Go through child nodes, looking for <value> and <description> nodes
					for (i in 0 until node.childNodes.length) {
						val childNode = node.childNodes.item(i)
						when (childNode.nodeName) {
							"value" -> stringValue = childNode.textContent.trimIndent()
							"description" -> description = getLocalizedString(context, childNode.textContent.trimIndent())
							"options" -> {
								val optionsMap = parseOptionsNode(childNode, context)
								options = ArrayList(optionsMap.size)
								optionsNames = ArrayList(optionsMap.size)
								optionsMap.forEach {
									options.add(it.key)
									optionsNames.add(it.value)
								}
							}
							"other" -> {
								if (otherData == null)
									otherData = HashMap()
								val key = childNode.attributes.getNamedItem("name").nodeValue
								otherData[key] = childNode.textContent.trimIndent()
							}
							else -> Log.d(OmnisInterface.TAG, "Setting XML node contains unexpected child node")
						}
					}

					// Create the Setting:
					val setting = Setting(
								node.attributes.getNamedItem("id")?.nodeValue
											?: throw Exception("Setting in xml without an 'id' attribute"),
								displayName,
								DataType.valueOf(dataType.uppercase()),
								stringValue,
								isExposed
					)
					setting.description = description
					setting.options = options?.toTypedArray()
					setting.optionNames = optionsNames?.toTypedArray()
					setting.otherData = otherData
					return setting
				}
			}
			catch (e: Exception) {
				e.printStackTrace()
			}

			return null
		}

		private fun parseOptionsNode(optionsNode: Node, context: Context): Map<String, String> {
			val options = mutableMapOf<String, String>()

			for (i in 0 until optionsNode.childNodes.length) {
				val option = optionsNode.childNodes.item(i)
				if (option.nodeName == "option") {
					val optionValue = option.textContent.trimIndent()
					val optionDisplayName = getLocalizedString(context,option.attributes.getNamedItem("displayName")?.textContent ?: optionValue)
					options[optionValue] = optionDisplayName
				}
			}

			return options
		}
	}
}