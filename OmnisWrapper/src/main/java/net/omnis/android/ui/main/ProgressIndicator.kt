package net.omnis.android.ui.main

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import net.omnis.android.R

internal class ProgressIndicator(activity: FragmentActivity) {

	private val progBar: ProgressBar = activity.findViewById(R.id.progressBar)

	fun updateProgress(amount: Int) {
		// If the progress bar is not visible, make it so:
		if (progBar.visibility != View.VISIBLE)
			progBar.visibility = View.VISIBLE

		progBar.progress = amount
		if (amount == 100) { //If the progress has reached 100, loading is finished and we should hide the progress bar
			hideProgress()
		}
	}

	fun hideProgress() {
		progBar.visibility = View.GONE
//		(mContext as OmnisAndroidClient).showSplashScreen(false)
	}
}
