package net.omnis.android

import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import net.omnis.android.BuildConfig
import net.omnis.android.settings.OMSettings
import net.omnis.android.ui.main.ProgressIndicator
import net.omnis.omnisinterface.*
import net.omnis.omnisinterface.omdevice.OMImages
import net.omnis.omnisinterface.webview.OMWebContainer
import java.net.MalformedURLException
import java.net.URL

import net.omnis.android.OmnisOfflineController

class OmnisController(activity: ComponentActivity, webContainer: OMWebContainer) : OMWebNavigationDelegate { // jmg1290

	private val mActivity = activity
	private val mOmnisInterface = OmnisInterface(webContainer, activity)
	private val mCommsDelegate = OmnisCommsDelegate(mOmnisInterface)
	private val mOfflineController = net.omnis.android.OmnisOfflineController(mOmnisInterface)
	private var mOnlineMode = true
	private val mProgressBar = ProgressIndicator(activity as FragmentActivity)

	init {
		mOmnisInterface.features.addFeature(Features.STORED_PREFS)
		mOmnisInterface.settings.setSetting(SettingNames.USE_LOCAL_TIME, OMSettings.getSettingBool(OMSettings.IDs.USE_LOCAL_TIME) ?: false) // jmg0890
		mOmnisInterface.settings.setSetting(SettingNames.DEBUG_WEBVIEW, OMSettings.getSettingBool(OMSettings.IDs.DEBUG_WEBVIEW) ?: false) // jmg1263
		mOmnisInterface.commsDelegate = mCommsDelegate
		mOmnisInterface.webNavigationDelegate = this
		OMImages.FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID +".fileprovider" // Especificamos la autoridad del proveedor de archivos como "${applicationId}.fileprovider" en el manifiesto
		reinitLocalDB()

		mOnlineMode = OMSettings.getSettingBool(OMSettings.IDs.START_OFFLINE) == false
	}

	fun loadForm(urlParams: Map<String, String>? = null) {
		if (mOnlineMode)
			loadOnlineForm(urlParams)
		else
			mOfflineController.loadFormOnceReady()
	}

	private fun loadOnlineForm(urlParams: Map<String, String>? = null) {
		val urlString = "${OMSettings.getSettingString(OMSettings.IDs.OMNIS_WEB_URL)
					?: ""}${OMSettings.getSettingString(OMSettings.IDs.ONLINE_FORM) ?: ""}" // jmg0890
		try {
			val url = URL(urlString)
			mOmnisInterface.loadURL(url, urlParams)
		}
		catch (e: MalformedURLException) {
			OMDialogs.showOKMessage(mActivity, e.localizedMessage, "Bad URL")
		}
	}

	fun getOmnisInterface(): OmnisInterface {
		return mOmnisInterface
	}

	fun setOnlineMode(online: Boolean) {
		mOnlineMode = online
		OMSettings.setSetting(OMSettings.IDs.START_OFFLINE, !online, mActivity)
	}

	fun getOnlineMode(): Boolean {
		return mOnlineMode
	}

	fun reinitOfflineMode() {
		mOfflineController.reinitialize()
	}

	fun updateSCAFs() {
		mOfflineController.updateSCAFs()
	}

	fun reinitLocalDB() {
		mOmnisInterface.database.initLocalDatabase(OMSettings.getSettingString(OMSettings.IDs.LOCAL_DB_NAME) ?: "local.db")
	}

	// región métodos webNavigationDelegate
	override fun omnisBeginLoading(webview: WebView?, url: String?): Boolean {
		mProgressBar.updateProgress(0)
		OMDialogs.showLoadingOverlay(mActivity, R.id.omweb_container, mActivity.findViewById(R.id.main), mActivity.resources.getString(R.string.loading_page_overlay_message), 2000) {
			webview?.stopLoading()
			false
		}
		return true
	}

	override fun omnisLoadingProgress(webview: WebView?, progress: Int): Boolean {
		mProgressBar.updateProgress(progress)
		return true
	}

	override fun omnisLoadingComplete(webview: WebView?, url: String?): Boolean {
		mProgressBar.hideProgress()
		OMDialogs.removeLoadingOverlay(R.id.omweb_container,  mActivity.findViewById(R.id.main))
		return true
	}

	override fun omnisLoadingFailed(webview: WebView?, url: String?, error: String?): Boolean {
		mProgressBar.hideProgress()
		OMDialogs.removeLoadingOverlay(R.id.omweb_container,  mActivity.findViewById(R.id.main))
		return false
	}
	// endregion

}