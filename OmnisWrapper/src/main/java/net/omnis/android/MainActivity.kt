package net.omnis.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging
import net.omnis.android.ui.main.MainFragment
import net.omnis.omnisinterface.IntentHelper
import net.omnis.omnisinterface.omdevice.OMPermissions

class MainActivity : AppCompatActivity() {

	companion object {
		const val preventRooted = false // jmg1264: Set to true to prevent the app running on rooted devices
	}

	private var mMainFragment: MainFragment? = null // jmg0950

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { view, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

		if (savedInstanceState == null) {
			mMainFragment = MainFragment.newInstance() // jmg0950
			supportFragmentManager.beginTransaction()
				.replace(R.id.container, mMainFragment!!) // jmg0950
				.commitNow()
		}

		obtenerTokenFCM()
	}

	private fun obtenerTokenFCM() {
		FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
			if (task.isSuccessful) {
				val token = task.result
				Log.e("FCM_TEST", "==================================================")
				Log.e("FCM_TEST", "TOKEN FCM PIXEL: $token")
				Log.e("FCM_TEST", "==================================================")
			} else {
				Log.e("FCM_TEST", "Error al obtener el token de FCM", task.exception)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		OMPermissions.permissionRequestResults(permissions, grantResults)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		IntentHelper.handleActivityResult(requestCode, resultCode, data)
	}
}
