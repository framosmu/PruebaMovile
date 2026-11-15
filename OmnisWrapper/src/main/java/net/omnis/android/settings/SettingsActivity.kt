package net.omnis.android.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import net.omnis.android.R

class SettingsActivity: AppCompatActivity() {

	private val mChangedSettings = HashSet<String>()
	private val mChangedGroups = HashSet<String>()

	override fun onCreate(savedInstanceState: Bundle?) {
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)

		setContentView(R.layout.settings)

		val container = findViewById<View>(R.id.omnis_settings_fragment_container)
		if (container != null) {
			ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
				val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
				view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
				insets
			}

			if (savedInstanceState != null) // If we're being restored from a previous state, don't add another fragment.
				return

			val fragment = OMPrefsFragment()
			fragment.arguments = intent.extras

			supportFragmentManager.beginTransaction().add(R.id.omnis_settings_fragment_container, fragment).commit()

			supportFragmentManager.addOnBackStackChangedListener {
				val frag = supportFragmentManager.fragments.last()
				(frag as? OMPrefsFragment)?.setupActionBar()
			}
		}

	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> { // ActionBar back button was pressed - pop the current entry off the stack (or leave settings if at the top)
				if (supportFragmentManager.backStackEntryCount == 0)
					onBackPressed()
				else
					supportFragmentManager.popBackStack()
				return true
			}
		}

		return super.onOptionsItemSelected(item)
	}

	override fun onBackPressed() {
		if (supportFragmentManager.backStackEntryCount == 0) // We are leaving the Settings Activity
		{
			val returnIntent = Intent()
			returnIntent.putExtra("changedSettings", mChangedSettings.toTypedArray())
			returnIntent.putExtra("changedGroups", mChangedGroups.toTypedArray())
			setResult(Activity.RESULT_OK, returnIntent)
			finish()
		}

		super.onBackPressed()
	}

	fun addChangedSetting(settingID: String, groupID: String) {
		mChangedSettings.add(settingID)
		mChangedGroups.add(groupID)
	}

}
