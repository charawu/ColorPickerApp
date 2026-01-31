package com.example.colorpickerapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.colorpickerapp.VibrationHelper.vibrateWithPredefinedEffect
import com.google.android.material.slider.Slider

class FlashControl : AppCompatActivity() {

    private var TAG = "fuckFlashControl"
    private lateinit var cameraManager: CameraManager
    private var cameraID = ""
    private var maxBrightness: Int = 1
    private var defaultBrightness = 1
    private var isFlashOn: Boolean = false

    companion object{
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }
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
        try {
            cameraID = cameraManager.cameraIdList.firstOrNull() ?:return

            val characteristics = cameraManager.getCameraCharacteristics(cameraID)

            //检查获取到的相机是否有闪光灯
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            if (!hasFlash){
                flashButton.isEnabled = false
                flashButton.setText("on flash!")
                return
            }

            //获取最大亮度
            maxBrightness = characteristics.get(
                CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1

            //获取默认亮度
             defaultBrightness = characteristics.get(
                 CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: 1

            lightnessSlide.valueTo = maxBrightness.toFloat()
            lightnessSlide.valueFrom = 1.toFloat()


            //设置brightnessTextView的值
            var brightnessText = (defaultBrightness * 100 / maxBrightness).toString()
            brightnessTextView.text = "$brightnessText%"
            lightnessSlide.value = defaultBrightness.toFloat()
        } catch (e: CameraAccessException) {
            Log.d(TAG,"$e.message")
        }

        //检查在MainActivity的打开活动
        val autoTurnOn = intent.getBooleanExtra("auto_turn_on",false)
        if (autoTurnOn){
            Handler(Looper.getMainLooper()).postDelayed({
                updateBrightness(defaultBrightness)
            },300)
        }
    }

    private fun updateUI(value:Int){
        var brightnessText  = (value * 100/ maxBrightness).toString()
        brightnessTextView.text = "$brightnessText%"
    }

    private fun changeFlashlight(){

        if (!isFlashOn){
            updateBrightness(defaultBrightness)
            flashButton.setText(R.string.buttonFlash_off)
        }else{
            turnOffFlash()
            flashButton.setText(R.string.buttonFlash_on)
        }

        updateUI(defaultBrightness)
        lightnessSlide.value = defaultBrightness.toFloat()
    }

    private fun updateBrightness(brightness: Int){
        try {
            if (brightness in 1..maxBrightness){
                cameraManager.turnOnTorchWithStrengthLevel(cameraID,brightness)
                isFlashOn = true
            }
        }catch (e: CameraAccessException){
            Log.d(TAG,"${e.message}")
        }
    }

    private fun turnOffFlash(){
        try {
            cameraManager.setTorchMode(cameraID,false)
            isFlashOn = false
        }catch (e: CameraAccessException){
            Log.d(TAG,"${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            turnOffFlash()
        } catch (e: CameraAccessException) {
            Log.d(TAG,"${e.message}" + "手电筒关闭失败")
        }
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

        //初始化相机管理器
        cameraManager = getSystemService(Context.CAMERA_SERVICE)as CameraManager

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
                updateUI(value.toInt())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //检查兼容新
            val windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())

            // 判断是否为深色模式
            val isDarkTheme = ((getResources().getConfiguration().uiMode and
                    Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES)

            // 设置状态栏图标颜色
            windowInsetsController.setAppearanceLightStatusBars(!isDarkTheme)
        }
    }
}