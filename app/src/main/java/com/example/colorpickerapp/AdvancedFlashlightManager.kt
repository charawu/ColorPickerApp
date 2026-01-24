package com.example.colorpickerapp

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

class AdvancedFlashlightManager(private val context: Context) {
    companion object{
        private const val TAG = "fuckAdvancedFlashingManager"
        private const val MAX_BRIGHTNESS = 100
        private const val MIN_BRIGHTNESS = 1
    }

    private lateinit var cameraManager: CameraManager
    private var isFlashlightOn = false
    private var defaultBrightness = 50  //默认亮度50
    private var cameraId: String? = null

    init {
        initializeCamera()
    }

    fun isFlashlightAvailable(): Boolean{
        return cameraId != null && context.packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun turnOnFlashlight(brightnessPercent: Int = defaultBrightness): Boolean {
        val safeBrightness = brightnessPercent.coerceIn(MIN_BRIGHTNESS,MAX_BRIGHTNESS)
        defaultBrightness = safeBrightness

        return try {
            //(?.let{...})一种安全的非空处理方法
            cameraId?.let { cameraManager.setTorchMode(it,true) }
            isFlashlightOn = true
            Log.d(TAG,"手电筒已打开,亮度为:${defaultBrightness}%")
            true
        }catch (e: CameraAccessException){
            Log.d(TAG,"无法访问相机:${e.message}")
            false
        }catch (e: Exception){
            Log.d(TAG,"打开手电筒时发生错误:${e.message}")
            false
        }
    }

    private fun initializeCamera(){
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as
                    CameraManager

            //查找有闪光灯的相机
            for (id in cameraManager.cameraIdList){
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE) ?:false

                if (hasFlash){
                    cameraId = id
                    Log.d(TAG,"查找到有闪光灯的相机${id}")
                    break
                }
            }

            if (cameraId == null){
                Log.d(TAG,"未找到有闪光灯的相机")
            }
        } catch (e: Exception){
            Log.d(TAG,"初始化失败:${e.message}")
        }
    }

    fun setBrightness(brightnessPercent: Int): Boolean {
        val safeBrightness = brightnessPercent.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS) / 100

        if (!isFlashlightOn) {
            Log.w(TAG, "手电筒未打开，无法调节亮度")
            return false
        }

        // 模拟亮度变化
        defaultBrightness = safeBrightness
        Log.d(TAG, "亮度设置为: ${defaultBrightness}% (模拟)")

        cameraId?.let { cameraManager.turnOnTorchWithStrengthLevel(it,1) }

        return true
    }

    fun turnOffFlashlight(): Boolean {
        return try {
            if (isFlashlightOn && cameraId != null) {
                cameraId?.let { cameraManager.setTorchMode(it,false) }
            }
            isFlashlightOn = false
            Log.d(TAG, "手电筒已关闭")
            true
        } catch (e: CameraAccessException) {
            Log.e(TAG, "无法访问相机: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "关闭手电筒时发生错误: ${e.message}")
            false
        }
    }

    fun changeFlashlight(): Boolean{
        return if (isFlashlightOn){
            turnOffFlashlight()
        }else{
            turnOnFlashlight(defaultBrightness)
        }
    }

    fun getDefaultBrightness():Int = defaultBrightness

    fun isFlashlightOn(): Boolean = isFlashlightOn

    fun release(){
        if (isFlashlightOn){
            turnOffFlashlight()
        }
    }

}