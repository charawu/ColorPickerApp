package com.example.colorpickerapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

private var TGA = "fuckVibrationHelper"

//震动封装类,使用object声明不用创建示例,可以直接使用类名调用
object VibrationHelper {

    @SuppressLint("MissingPermission")
    fun vibrateWithPredefinedEffect(context: Context, effectId: Int) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return

        try {
            // 核心修改：创建预定义的震动效果，而非自定义时长
            val effect = VibrationEffect.createPredefined(effectId)
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            // 异常处理
        }
    }

private fun getVibrator(context: Context): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        //判断是否android12以上
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)as Vibrator
    }
}

    //用于常规按钮震动
    @SuppressLint("MissingPermission")  //@SuppressLint用于忽略特定警告
    fun vibrate(context: Context,milliseconds:Long,
                amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE){
        //获取震动服务
        val vibrator = getVibrator(context)

        try {
            //判断系统版本,android8以上使用VibrationEffect
            val effect = VibrationEffect.createOneShot(milliseconds,amplitude)
            vibrator.vibrate(effect)
        }catch (e: Exception) {
            Log.d(TGA,"执行震动发生错误:${e.message}")
        }
    }

    //用于滑块震动
    @SuppressLint("MissingPermission")  //@SuppressLint用于忽略特定警告
    fun vibratePattern(context: Context,pattern: LongArray,repeat:Int = -1){
        val vibrator = getVibrator(context)

        try {
            val effect = VibrationEffect.createWaveform(pattern,repeat)
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            Log.d(TGA,"执行震动模式发生错误${e.message}")
        }
    }

    //取消所有震动
    fun cancel(context: Context){
        getVibrator(context).cancel()
    }

}