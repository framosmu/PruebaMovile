/***** Changes ***********
 * Date					Edit				Bug					Description
 * 21-Feb-23		jmg1242			WR/WR/373		Immersive mode didn't work well on newer Android versions. Migrated to newer API.
 *************************/

package net.omnis.android.ui.main

import android.content.res.Configuration
import android.os.Build
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import net.omnis.android.MainActivity
import net.omnis.android.R
import net.omnis.android.settings.OMSettings

abstract class OMImmersiveFragment: androidx.fragment.app.Fragment() {

	private var mImmersive = false
	private var mShowingImmersiveOverlay = false
	private var mOverlayID = 0
	private var mInMultiWindowMode = false


	fun checkImmersiveMode() {
		mImmersive = OMSettings.getSettingBool(OMSettings.IDs.IMMERSIVE_MODE) ?: false

		mInMultiWindowMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			activity?.isInMultiWindowMode ?: false
		else
			false

		if (isImmersive())
			enableImmersiveMode()
		else
			disableImmersiveMode()
	}

	private fun enableImmersiveMode() {
		mImmersive = true

		if (isImmersive()) {
			// Start jmg1242
			hideSystemUI(true)

			ViewCompat.setOnApplyWindowInsetsListener(requireView()) {v, insets ->
				if (insets.isVisible(Type.statusBars()))
					showSystemUI()

				WindowInsetsCompat.CONSUMED
			}
			// End jmg1242
		}
	}

	private fun disableImmersiveMode() {
		mImmersive = false
		removeImmersiveOverlay()
		// Start jmg1242
		WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)

		ViewCompat.setOnApplyWindowInsetsListener(requireView()) {v, insets ->
			ViewCompat.onApplyWindowInsets(v, insets)
			WindowInsetsCompat.CONSUMED
		}
		// End jmg1242
	}

	private fun addImmersiveOverlay() {
		val layout = view?.findViewById<ConstraintLayout>(R.id.main)
		if (layout != null) {
			// Create an overlay view:
			val overlay = View(context)
			mOverlayID = ViewCompat.generateViewId()
			overlay.id = mOverlayID
			overlay.isClickable = true
			layout.addView(overlay)

			// Set its constraints:
			val constraints = ConstraintSet()
			constraints.connect(mOverlayID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
			constraints.connect(mOverlayID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
			constraints.connect(mOverlayID, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
			constraints.connect(mOverlayID, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
			constraints.applyTo(layout)

			mShowingImmersiveOverlay = true

			// Add a touchdown listener to hide the UI again:
			val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
				override fun onDown(e: MotionEvent): Boolean {
					hideSystemUI()
					return super.onSingleTapUp(e)
				}
			})
			overlay.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
		}
	}

	private fun removeImmersiveOverlay() {
		val layout = view?.findViewById<ConstraintLayout>(R.id.main)
		if (layout != null) {
			val overlay = layout.findViewById<View>(mOverlayID)
			if (overlay != null)
				layout.removeView(overlay)

			mShowingImmersiveOverlay = false
		}
	}


// Start jmg1242
	private fun hideSystemUI(force: Boolean = false) {

		if (this.mShowingImmersiveOverlay || force) {

			val window = requireActivity().window
			WindowCompat.setDecorFitsSystemWindows(window, false)

			WindowInsetsControllerCompat(window, requireView()).let { controller ->
				controller.hide(Type.systemBars())
				controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
			}

			(requireActivity() as MainActivity).supportActionBar?.hide()
			removeImmersiveOverlay()
		}

	}

	private fun showSystemUI() {

		if (!this.mShowingImmersiveOverlay) {
			(requireActivity() as MainActivity).supportActionBar?.show()
			addImmersiveOverlay()
			WindowInsetsControllerCompat(requireActivity().window, requireView()).show(Type.systemBars())
		}

	}

	// End jmg1242

	private fun isImmersive(): Boolean {
		return mImmersive && !mInMultiWindowMode
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		mInMultiWindowMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			activity?.isInMultiWindowMode ?: false
		else
			false

		checkImmersiveMode()
	}

}