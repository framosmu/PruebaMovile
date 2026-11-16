package net.omnis.android.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import net.omnis.android.MainActivity
import net.omnis.android.OmnisController
import net.omnis.android.R
import net.omnis.android.about.About
import net.omnis.android.about.CreditsActivity
import net.omnis.android.notifications.OmnisNotification
import net.omnis.android.security.BiometricAuthManager
import net.omnis.android.settings.OMSettings
import net.omnis.omnisinterface.OMDialogs
import net.omnis.omnisinterface.Security
import kotlin.system.exitProcess

class MainFragment : OMImmersiveFragment() { // jmg0950

	private lateinit var mOmnisController: OmnisController
	private var networkMonitor: NetworkConnectivityMonitor? = null
	private var mBiometricAuthenticated = false

	companion object {
		fun newInstance() = MainFragment()
	}

	private lateinit var viewModel: MainViewModel

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
														savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.main_fragment, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

		setHasOptionsMenu(true)
		checkImmersiveMode() // jmg0950
		setupKeyboardDetection()
	}

	override fun onStart() {
		super.onStart()
		if (!this::mOmnisController.isInitialized) {
			mOmnisController = OmnisController(requireActivity(), requireView().findViewById(R.id.omweb_container))
			viewModel.setOmnisController(mOmnisController)

			if (MainActivity.preventRooted && Security.isDeviceRooted()) {
				OMDialogs.showOKMessage(requireContext(), R.string.disabled_due_to_rooted, R.string.disabled_due_to_rooted_title) { ->
					exitProcess(0)
				}
				return
			}

			val useBiometrics = OMSettings.getSettingBool(OMSettings.IDs.USE_BIOMETRICS) == true
			if (useBiometrics && BiometricAuthManager.canAuthenticate(requireContext()) && !mBiometricAuthenticated) {
				val webContainer = requireView().findViewById<View>(R.id.omweb_container)
				webContainer.visibility = View.INVISIBLE

				promptBiometricAuth(webContainer)
			} else {
				mOmnisController.loadForm()
			}
		}

		if (networkMonitor == null) {
			networkMonitor = NetworkConnectivityMonitor(
				context = requireContext(),
				onNetworkLost = {
					activity?.runOnUiThread {
						handleNetworkLost()
					}
				},
				onNetworkAvailable = {
					activity?.runOnUiThread {
						handleNetworkAvailable()
					}
				}
			)
		}
		networkMonitor?.startMonitoring()
	}

	override fun onStop() {
		super.onStop()
		networkMonitor?.stopMonitoring()
	}

	override fun onResume() {
		super.onResume()
		checkForNotificationIntent()
	}

	private fun promptBiometricAuth(webContainer: View) {
		BiometricAuthManager.authenticate(
			activity = requireActivity(),
			onSuccess = {
				mBiometricAuthenticated = true
				webContainer.visibility = View.VISIBLE
				mOmnisController.loadForm()
			},
			onError = { _ ->
				if (isAdded) {
					AlertDialog.Builder(requireContext())
						.setTitle(R.string.biometric_prompt_title)
						.setMessage(R.string.biometric_prompt_subtitle)
						.setCancelable(false)
						.setPositiveButton(R.string.retry) { _, _ ->
							promptBiometricAuth(webContainer)
						}
						.show()
				}
			}
		)
	}

	private fun handleNetworkLost() {
		if (!this::mOmnisController.isInitialized) return
		if (mOmnisController.getOnlineMode() && isAdded) {
			AlertDialog.Builder(requireContext())
				.setTitle(R.string.connection_lost_title)
				.setMessage(R.string.connection_lost_message)
				.setPositiveButton(R.string.change_to_offline) { _, _ ->
					mOmnisController.setOnlineMode(false)
					mOmnisController.loadForm()
					activity?.invalidateOptionsMenu()
				}
				.setNegativeButton(android.R.string.cancel, null)
				.show()
		}
	}

	private fun handleNetworkAvailable() {
		if (!this::mOmnisController.isInitialized) return
		if (!mOmnisController.getOnlineMode() && isAdded) {
			AlertDialog.Builder(requireContext())
				.setTitle(R.string.connection_restored_title)
				.setMessage(R.string.connection_restored_message)
				.setPositiveButton(R.string.change_to_online) { _, _ ->
					mOmnisController.setOnlineMode(true)
					mOmnisController.loadForm()
					activity?.invalidateOptionsMenu()
				}
				.setNegativeButton(android.R.string.cancel, null)
				.show()
		}
	}

	private fun setupKeyboardDetection() {
		val rootView: View? = activity?.findViewById(R.id.container)
		rootView?.viewTreeObserver?.addOnGlobalLayoutListener {
			val heightDiff: Int = rootView.rootView.height - rootView.height
			if (heightDiff > dpToPx(requireContext(), 200F)) { // if more than 200 dp, it is probably the software keyboard appearing
				onSoftKeyboardShown()
			}
		}
	}

	/**
	 * Called when the software keyboard appears.
	 */
	private fun onSoftKeyboardShown() {
		mOmnisController.getOmnisInterface().callJS("setTimeout( () => { if (document.activeElement) document.activeElement.scrollIntoView(); }, 300);")
	}

	private fun dpToPx(context: Context, dpValue: Float): Float {
		val metrics = context.resources.displayMetrics
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
	}

	private fun checkForNotificationIntent() {
		val intent = activity?.intent

		if (intent != null) {

			if (intent.hasExtra("_OMNIS_NOTIFICATION_")) { // This was triggered by clicking on a notification which had a data payload.
				intent.removeExtra("_OMNIS_NOTIFICATION_")

				val urlParams = mutableMapOf<String, String>()
				val extras = OmnisNotification.removeExtraDataFromPayload(intent.extras!!) // jmg0593

				for (key in extras.keySet()) {
					urlParams[key] = extras.getString(key) ?: ""
				}

				mOmnisController.loadForm(urlParams)
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.menu, menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu) {
		super.onPrepareOptionsMenu(menu)

		menu.findItem(R.id.menuOnlineMode)?.isChecked = mOmnisController.getOnlineMode()

		menu.findItem(R.id.menuUpdateApp)?.isVisible = (OMSettings.getSettingBool(OMSettings.IDs.MENU_INCLUDE_UPDATE) ?: true) && !mOmnisController.getOnlineMode() // Only show the Update Application option in Offline mode. // jmg0890
		menu.findItem(R.id.menuOnlineMode)?.isVisible = OMSettings.getSettingBool(OMSettings.IDs.MENU_INCLUDE_OFFLINE) ?: true // jmg0890

		val includeAbout = OMSettings.getSettingBool(OMSettings.IDs.MENU_INCLUDE_ABOUT) ?: true // jmg0890
		menu.findItem(R.id.menuAbout)?.isVisible = includeAbout
		menu.findItem(R.id.menuCredits)?.isVisible = !includeAbout
		menu.findItem(R.id.menuSettings)?.isVisible = OMSettings.getSettingBool(OMSettings.IDs.MENU_INCLUDE_SETTINGS) ?: true // jmg0890
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {

		when (item.itemId) {
			R.id.reload -> {
				mOmnisController.loadForm()
				return true
			}

			R.id.menuSettings -> {
				viewModel.showSettingsScreen(activity as MainActivity)
				return true
			}

			R.id.menuUpdateApp -> {
				mOmnisController.updateSCAFs()
			}

			R.id.menuOnlineMode -> {
				mOmnisController.setOnlineMode(!item.isChecked)
				mOmnisController.loadForm()
			}

			R.id.menuAbout -> {
				val aboutIntent = Intent(activity, About::class.java)
				startActivity(aboutIntent)
			}

			R.id.menuCredits -> {
				val creditsIntent = Intent(activity, CreditsActivity::class.java)
				startActivity(creditsIntent)
			}

		}

		return super.onOptionsItemSelected(item)
	}

}
