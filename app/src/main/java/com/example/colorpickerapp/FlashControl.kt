package com.example.colorpickerapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.colorpickerapp.VibrationHelper.vibrateWithPredefinedEffect
import com.google.android.material.slider.Slider

class FlashControl : AppCompatActivity() {

    companion object{
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }
    private lateinit var flashlightManger: AdvancedFlashlightManager
    private lateinit var flashButton: Button
    private lateinit var lightnessSlide: Slider
    private lateinit var brightnessTextView: TextView

    private fun initView(){
        flashButton = findViewById(R.id.flashButton)
        lightnessSlide = findViewById(R.id.flashSlider)
        brightnessTextView = findViewById(R.id.brightnessTextView)
    }

    private fun checkCameraPermission(): Boolean{
        return if (ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED){
            true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            false
        }
    }

    private fun initFlashlightManager(){
        flashlightManger = AdvancedFlashlightManager(this)

        if (!flashlightManger.isFlashlightAvailable()){
            Toast.makeText(
                this,
                "设备不支持手电筒",
                Toast.LENGTH_SHORT
            ).show()

            flashButton.isEnabled = false
            return
        }

        //检查在Activity的打开活动
        val autoTurnOn = intent.getBooleanExtra("auto_turn_on",false)
        if (autoTurnOn){
            Handler(Looper.getMainLooper()).postDelayed({
                changeFlashlight()
            },300)
        }
    }

    private fun updateUI(){

    }

    private fun updateBrightness(brightness: Int){
        val success = flashlightManger.setBrightness(brightness)

        if (success){
            brightnessTextView.text = "${brightness}%"

            if (flashlightManger.isFlashlightOn()){
                flashlightManger.turnOnFlashlight(brightness)
            }
        }
    }

    private fun changeFlashlight(){
        val success = flashlightManger.changeFlashlight()

        if (success){
            updateUI()

        }else{
            Toast.makeText(
                this,
                "手电筒打开失败",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::flashlightManger.isInitialized){
            flashlightManger.release()
        }
    }

    private fun showToast(message: String){
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //判断有了权限,初始化手电筒
                initFlashlightManager()
            } else {
                //拒绝了授权
                Toast.makeText(
                    this,
                    "需要相机权限才能使用",
                    Toast.LENGTH_SHORT
                ).show()
                flashButton.isEnabled = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash_control)

        initView()
        initFlashlightManager()

        //检查并申请相机权限
        if (checkCameraPermission())

        flashButton.setOnClickListener { view: View ->
            vibrateWithPredefinedEffect(
                this,
                VibrationEffect.EFFECT_CLICK
            )

            changeFlashlight()
        }

        lightnessSlide.addOnChangeListener { slider: Slider, value: Float, fromUser: Boolean ->
            if (fromUser) {
                vibrateWithPredefinedEffect(
                    this,
                    VibrationEffect.EFFECT_CLICK
                )

                updateBrightness((value.toInt()))

            }
        }
    }
}