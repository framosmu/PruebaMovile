package net.omnis.android.settings

import android.content.Context
import android.util.Log
import net.omnis.omnisinterface.OmnisInterface
import org.w3c.dom.Node

class SettingGroup(val id: String, val displayName: String, exposed: Boolean = true, val newPage: Boolean = false): SettingEntry(exposed) {

	var children = ArrayList<SettingEntry>()
	var description: String? = null


	companion object {

		fun buildFromNode(groupNode: Node, context: Context): SettingGroup? {

			if (groupNode.nodeName == "group") {
				try {
					val group = SettingGroup(
								groupNode.attributes.getNamedItem("id")?.nodeValue
											?: throw Exception("Setting Group in xml without an 'id' attribute"),
								getLocalizedString(context, groupNode.attributes.getNamedItem("displayName")?.nodeValue ?: ""),
								(groupNode.attributes.getNamedItem("exposed")?.nodeValue ?: "true") == "true",
								groupNode.attributes.getNamedItem("newPage")?.nodeValue == "true")
					group.description = getLocalizedString(context, groupNode.attributes.getNamedItem("description")?.nodeValue ?: "")
					group.children = SettingEntry.buildChildrenFromNode(groupNode, context)
					return group
				}
				catch (e: Exception) {
					Log.e(OmnisInterface.TAG, e.localizedMessage)
					return null
				}
			}
			else
				return null
		}

	}
}