package net.omnis.android.settings

import android.content.Context
import net.omnis.omnisinterface.ResourceRelay
import org.w3c.dom.Node

/**
 * Just a stub class to use as a common parent for Setting & SettingGroup
 */
open class SettingEntry(val exposed: Boolean = true) {

	companion object {

		fun buildChildrenFromNode(node: Node, context: Context): ArrayList<SettingEntry> {
			val children = ArrayList<SettingEntry>()

			val nodeList = node.childNodes
			val groupID = node.attributes.getNamedItem("id")?.textContent

			for (i in 0 until nodeList.length) {
				val item = nodeList.item(i)
				if (item.nodeName == "setting") {
					val setting = Setting.buildFromNode(item, context)
					if (setting != null) {
						setting.groupID = groupID ?: ""
						children.add(setting)
					}
				}
				else if (item.nodeName == "group") {
					val group = SettingGroup.buildFromNode(item, context)
					if (group != null)
						children.add(group)
				}
			}

			return children
		}


		fun getSettingsMapFromEntries(entries: ArrayList<SettingEntry>): MutableMap<String, Setting> {
			val map = mutableMapOf<String, Setting>()

			entries.forEach {
				if (it is Setting) {
					map[it.id] = it
				}
				else if (it is SettingGroup) {
					getSettingsMapFromEntries(it.children).forEach { child ->
						map[child.key] = child.value
					}
				}
			}

			return map
		}

		fun getLocalizedString(context: Context, string: String): String {
			return if (string.startsWith("@string/")) {
				val stringID = string.substring(8) // 8 = length of "@string/"
				ResourceRelay.getString(context, stringID, stringID)
			}
			else
				string
		}

	}

}