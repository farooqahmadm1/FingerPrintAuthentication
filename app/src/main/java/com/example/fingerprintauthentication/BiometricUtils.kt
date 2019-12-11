package com.example.fingerprintauthentication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

object BiometricUtils{

    @JvmStatic
    fun isBiometricPromptEnabled() : Boolean{
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
    }
    /*
    * Condition I: Check if the android version in device is greater than
    * Marshmallow, since fingerprint authentication is only supported
    * from Android 6.0.
    * Note: If your project's minSdkversion is 23 or higher,
    * then you won't need to perform this check.
    *
    * */
    @JvmStatic
    fun isSdkVersionSupported() : Boolean{
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    }

    /*
     * Condition II: Check if the device has fingerprint sensors.
     * Note: If you marked android.hardware.fingerprint as something that
     * your app requires (android:required="true"), then you don't need
     * to perform this check.
     *
     * */
    @JvmStatic
    fun isHardwareSuppported(context: Context): Boolean{
        val managerCompat: FingerprintManagerCompat = FingerprintManagerCompat.from(context)
        return managerCompat.isHardwareDetected()
    }

    /*
     * Condition III: Fingerprint authentication can be matched with a
     * registered fingerprint of the user. So we need to perform this check
     * in order to enable fingerprint authentication
     *
     * */

    @JvmStatic
    fun isFingerprintAvailable(context: Context) : Boolean {
        val manager : FingerprintManagerCompat = FingerprintManagerCompat.from(context)
        return manager.hasEnrolledFingerprints()
    }

    /*
     * Condition IV: Check if the permission has been added to
     * the app. This permission will be granted as soon as the user
     * installs the app on their device.
     *
     * */

    @JvmStatic
    fun isPermissionGranted(context: Context) : Boolean{
        return ActivityCompat.checkSelfPermission(context,Manifest.permission.USE_FINGERPRINT) ==
                PackageManager.PERMISSION_GRANTED
    }
}