package com.example.fingerprintauthentication

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private var compassImg: ImageView? = null
    private var txtCompass: TextView? = null
    var mAzimuth: Int = 0
    private lateinit var mSensorManager: SensorManager
    private var mRotationV: Sensor? = null
    var mAccelerometer: Sensor? = null
    var mMagnetometer: Sensor? = null
    var haveSensor = false
    var haveSensor2 = false
    var rMat = FloatArray(9)
    var orientation = FloatArray(3)
    private val mLastAccelerometer = FloatArray(3)
    private val mLastMagnetometer = FloatArray(3)
    private var mLastAccelerometerSet = false
    private var mLastMagnetometerSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        compassImg =  findViewById(R.id.imageViewCompass)
        txtCompass = findViewById(R.id.tvHeading)

        start()
    }

    fun start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null || mSensorManager.getDefaultSensor(
                    Sensor.TYPE_MAGNETIC_FIELD
                ) == null
            ) {
                noSensorsAlert()
            } else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                haveSensor = mSensorManager.registerListener(
                    this,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI
                )
                haveSensor2 = mSensorManager.registerListener(
                    this,
                    mMagnetometer,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        } else {
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensor =
                mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun noSensorsAlert() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Your device doesn't support the Compass.")
            .setCancelable(false)
            .setNegativeButton("Close"
            ) { _, _ -> finish() }
        alertDialog.show()
    }

    fun stop() {
        if (haveSensor && haveSensor2) {
            mSensorManager.unregisterListener(this, mAccelerometer)
            mSensorManager.unregisterListener(this, mMagnetometer)
        } else {
            if (haveSensor)
                mSensorManager.unregisterListener(this, mRotationV)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {


    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type === Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values)
            mAzimuth = (Math.toDegrees(
                SensorManager.getOrientation(
                    rMat,
                    orientation
                )[0].toDouble()
            ) + 360).toInt() % 360
        }

        if (event?.sensor?.type === Sensor.TYPE_ACCELEROMETER) {
            event.values?.let { System.arraycopy(it, 0, mLastAccelerometer, 0, event.values.size) }
            mLastAccelerometerSet = true
        } else if (event?.sensor?.type === Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.size)
            mLastMagnetometerSet = true
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer)
            SensorManager.getOrientation(rMat, orientation)
            mAzimuth = (Math.toDegrees(
                SensorManager.getOrientation(
                    rMat,
                    orientation
                )[0].toDouble()
            ) + 360).toInt() % 360
        }

        mAzimuth = Math.round(mAzimuth.toFloat())
        compassImg?.rotation = (-mAzimuth).toFloat()

        var where = "NW"

        if (mAzimuth >= 350 || mAzimuth <= 10)
            where = "N"
        if (mAzimuth in 281..349)
            where = "NW"
        if (mAzimuth in 261..280)
            where = "W"
        if (mAzimuth in 191..260)
            where = "SW"
        if (mAzimuth in 171..190)
            where = "S"
        if (mAzimuth in 101..170)
            where = "SE"
        if (mAzimuth in 81..100)
            where = "E"
        if (mAzimuth in 11..80)
            where = "NE"
        txtCompass?.text = "$mAzimuth° $where"
    }


    override fun onResume() {
        super.onResume()
        start()
    }

    override fun onStop() {
        super.onStop()
        stop()
    }
}
