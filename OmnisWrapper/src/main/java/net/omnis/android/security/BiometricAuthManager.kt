package net.omnis.android.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import net.omnis.android.R

object BiometricAuthManager {

	fun canAuthenticate(context: Context): Boolean {
		val biometricManager = BiometricManager.from(context)
		val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
		return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
	}

	fun authenticate(
		activity: FragmentActivity,
		onSuccess: () -> Unit,
		onError: (String) -> Unit
	) {
		val executor = ContextCompat.getMainExecutor(activity)
		val callback = object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				onSuccess()
			}

			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				onError(errString.toString())
			}

			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
			}
		}

		try {
			val promptInfo = BiometricPrompt.PromptInfo.Builder()
				.setTitle(activity.getString(R.string.biometric_prompt_title))
				.setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
				.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
				.build()

			val biometricPrompt = BiometricPrompt(activity, executor, callback)
			biometricPrompt.authenticate(promptInfo)
		} catch (e: Exception) {
			Log.e("BiometricAuth", "Error launching BiometricPrompt: ${e.message}", e)
			onSuccess()
		}
	}
}
