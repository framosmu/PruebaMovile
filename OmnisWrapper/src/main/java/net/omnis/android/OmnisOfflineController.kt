package net.omnis.android

import net.omnis.android.settings.OMSettings
import net.omnis.omnisinterface.OMDialogs
import net.omnis.omnisinterface.OmnisInterface
import net.omnis.omnisinterface.ResourceRelay
import net.omnis.omnisinterface.offline.OMScafHandlerDelegate
import net.omnis.omnisinterface.offline.ScafAction
import net.omnis.omnisinterface.offline.ScafHandler

// Controlador de Scaf que maneja la lógica de carga de contenido offline.

class OmnisOfflineController(private val omnisInterface: OmnisInterface) : OMScafHandlerDelegate {

	private var mHtmlPath = ""
	private var mLoadOnceReady = false
	private val mScafHandler = omnisInterface.scafHandler
	private var mUrlParams: Map<String, String>? = null

	init {
		reinitialize()
	}

	companion object {
		const val OFFLINE_FILES_FOLDER = "offlinemode"
	}

	/**
	 * Reinitalize the ScafHandler, with the current Settings values.
	 */
	fun reinitialize() {
		mHtmlPath = ""

		omnisInterface.scafHandler.delegate = this
		omnisInterface.scafHandler.initScafController(OFFLINE_FILES_FOLDER,
					OMSettings.getSettingString(OMSettings.IDs.OFFLINE_FORM),
					OMSettings.getSettingString(OMSettings.IDs.SCAF_NAME),
					OMSettings.getSettingString(OMSettings.IDs.OMNIS_WEB_URL),
					OMSettings.getSettingString(OMSettings.IDs.OMNIS_SERVER),
					OMSettings.getSettingString(OMSettings.IDs.OMNIS_PLUGIN))
	}

	override fun onOfflineModeConfigured(htmlPath: String) {
		mHtmlPath = htmlPath
		if (mLoadOnceReady) {
			mLoadOnceReady = false
			loadOfflineForm()
		}
	}

	override fun onScafUpdateCompleted(didUpdate: Boolean, errors: Array<String>, newHtmlPath: String): Boolean {
		dismissUpdatingDialog()

		if (mHtmlPath.isEmpty() && didUpdate) // If we had not previously been able to load offline mode, do it now.
			loadOfflineForm()

		mHtmlPath = newHtmlPath
		return super.onScafUpdateCompleted(didUpdate, errors, newHtmlPath)
	}


	override fun onScafHandlerError(errorText: String, action: ScafAction): Boolean {
		OMDialogs.showOKMessage(omnisInterface.activityContext, errorText,
				ResourceRelay.getString(omnisInterface.activityContext, R.string.error_title_scaferror)
		)
		return false
	} // jmg1116


	fun loadFormOnceReady(urlParams: Map<String, String>? = null) {

		mUrlParams = urlParams

		if (!ScafHandler.offlineFilesExist(OFFLINE_FILES_FOLDER, omnisInterface.activityContext)) {
			OMDialogs.showDialog(
						omnisInterface.activityContext,
						omnisInterface.activityContext.resources.getString(R.string.updateneededtext),
						omnisInterface.activityContext.resources.getString(R.string.updateneededtitle),
						OMDialogs.DialogButton(1, OMDialogs.DialogButton.Type.POSITIVE,
									omnisInterface.activityContext.resources.getString(R.string.updateneededyes)) {
							updateSCAFs()
						},
						OMDialogs.DialogButton(2, OMDialogs.DialogButton.Type.NEUTRAL, omnisInterface.activityContext.resources.getString(R.string.updateneededno))
			)
			return
		}
		if (mHtmlPath.isNotEmpty())
			loadOfflineForm()
		else
			mLoadOnceReady = true
	}

	private fun loadOfflineForm() {
		mScafHandler.loadOfflineForm(mUrlParams)
	}

	fun updateSCAFs() {
		showUpdatingDialog()
		mScafHandler.updateScafs()
	}

	private fun showUpdatingDialog() {
		val activity = omnisInterface.activityContext
		OMDialogs.showLoadingOverlay(activity, R.id.omweb_container, activity.findViewById(R.id.main), activity.resources.getString(R.string.scaf_updating_message))
	}

	private fun dismissUpdatingDialog() {
		OMDialogs.removeLoadingOverlay(R.id.omweb_container, omnisInterface.activityContext.findViewById(R.id.main))
	}
}