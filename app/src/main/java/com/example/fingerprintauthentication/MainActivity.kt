package com.example.fingerprintauthentication

import android.app.KeyguardManager
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    private val KEY_NAME = "yourKey"

    private val username = "test"
    private val password = "test123"

    private lateinit var textView: TextView
    private lateinit var userName: EditText
    private lateinit var passWord: EditText
    private lateinit var loginButton: Button

    private var cipher: Cipher? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null
    private var fingerprintManager: FingerprintManager? = null
    private var keyguardManager: KeyguardManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textview)
        userName = findViewById(R.id.username)
        passWord = findViewById(R.id.password)
        loginButton = findViewById(R.id.login)

        loginButton.setOnClickListener {
            if (userName.text.toString() == username && passWord.text.toString() == password) {
                startActivity(Intent(this, CompassActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Credentials are not Valid", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        if (!BiometricUtils.isSdkVersionSupported()) {

        } else if (!BiometricUtils.isHardwareSuppported(this)) {
            textView.text = "Your device doesn't support fingerprint authentication"
        } else if (!BiometricUtils.isFingerprintAvailable(this)) {
            textView.text =
                "No fingerprint configured. Please register at least one fingerprint in your device's Settings"
        } else if (!BiometricUtils.isPermissionGranted(this)) {
            textView.text = "Please enable the fingerprint permission"
//        } else if (BiometricUtils.isBiometricPromptEnabled()) {
        } else {
            keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = getSystemService(FINGERPRINT_SERVICE) as FingerprintManager
            if (!keyguardManager?.isKeyguardSecure!!) {
                textView.text = "Please enable lockscreen security in your device's Settings"
            } else {
                textView.text = "Place your Fingerprint to Login"
                try {
                    generateKey()
                } catch (e: FingerprintException) {
                    e.printStackTrace()
                }

                if (initCipher()) {
                    cryptoObject = cipher?.let { FingerprintManager.CryptoObject(it) }
                    val helper = FingerprintHandler(this)
                    helper.startAuth(fingerprintManager!!, cryptoObject!!)
                }
            }
        }
    }


    @Throws(FingerprintException::class)
    private fun generateKey() {
        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyStore?.load(null)
            keyGenerator?.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )

            keyGenerator?.generateKey()
        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: CertificateException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: IOException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }


    }


    private fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore?.load(
                null
            )
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }
    private inner class FingerprintException(e: Exception) : Exception(e)
}
