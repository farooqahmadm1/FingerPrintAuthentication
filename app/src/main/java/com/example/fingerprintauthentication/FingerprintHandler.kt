package com.example.fingerprintauthentication


import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat


@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler(private val context: Context) :
    FingerprintManager.AuthenticationCallback() {

    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(Build.VERSION_CODES.M)
    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    override fun onAuthenticationError(
        errMsgId: Int,
        errString: CharSequence
    ) {
        Toast.makeText(
            context,
            "Authentication error\n$errString",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAuthenticationFailed() {
        Toast.makeText(
            context,
            "Authentication failed",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onAuthenticationHelp(
        helpMsgId: Int,
        helpString: CharSequence
    ) {
        Toast.makeText(
            context,
            "Authentication help\n$helpString",
            Toast.LENGTH_LONG
        ).show()
    }


    override fun onAuthenticationSucceeded(
        result: FingerprintManager.AuthenticationResult
    ) {
        context.startActivity(Intent(context,CompassActivity::class.java))
        Toast.makeText(
            context,
            "Success!",
            Toast.LENGTH_LONG
        ).show()
    }
}
