package com.example.colorpickerapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {
    //申明颜色预览相关
    private View colorPreviewView;
    //16进制颜色预览
    private TextView hexCodeTextView;

    //申明RGB滑块和数值
    private Slider redSlider,greenSlider,blueSlider;
    private TextView redValueText,greenValueText,blueValueText;

    //声明饱和度和透明度滑块及相关值
    private Slider alphaSlider;
    private TextView alphaValueText;

    //声明按钮
    private Button copyButton;
    private Button restButton;

    //声明颜色值
    private int red = 0,green = 0,blue = 0;
    private int alpha = 255;

    protected void initView() {  //初始化视图方法
        //获取颜色预览视图
        //findViewById()用于通过id查找相应的视图
        colorPreviewView = findViewById(R.id.colorPreview);
        hexCodeTextView = findViewById(R.id.hexCodeTextView);

        //RGB滑块和数值视图
        redSlider = findViewById(R.id.redSeekBar);
        greenSlider = findViewById(R.id.greenSeekBar);
        blueSlider = findViewById(R.id.blueSeekBar);
        redValueText = findViewById(R.id.redValue);
        greenValueText = findViewById(R.id.greenValue);
        blueValueText = findViewById(R.id.blueValue);

        //透明度滑块和数值视图
         alphaSlider = findViewById(R.id.alphaSeekBar);
         alphaValueText = findViewById(R.id.alphaValue);

         //button
        copyButton = findViewById(R.id.copyButton);
        restButton = findViewById(R.id.restButton);

         //设置初始数值
        redValueText.setText("0");
        greenValueText.setText("0");
        blueValueText.setText("0");
        alphaValueText.setText("255");
    }

    protected void setupSeekBarListeners(){  //设置滑块监听方法
        //红色滑块的监听
        redSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                red = (int) v;
                redValueText.setText(String.valueOf((int) v));
                updateColor();
            }
        });

        //绿色滑块的监听
        greenSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                green = (int) v;
                greenValueText.setText(String.valueOf((int) v));
                updateColor();
            }
        });

        //蓝色滑块的监听
        blueSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                blue = (int) v;
                blueValueText.setText(String.valueOf((int) v));
                updateColor();
            }
        });

        //透明度滑块的监听
        alphaSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float v, boolean b) {
                alpha = (int) v;
                alphaValueText.setText(String.valueOf((int) v));
                updateColor();
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyButton();
            }
        });

        restButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButton();
                initView();
                updateColor();
            }
        });
    }

    //颜色更新方法
    protected void updateColor() {
        //最终调整完成的颜色
        int finalColor = Color.argb(alpha,red,green,blue);

        Log.d("fuckColor",String.format("#%08X",finalColor));

        //更新颜色预览
        colorPreviewView.setBackgroundColor(finalColor);

        //更新十六进制颜色码
        String hexCode = String.format("#%08X",finalColor);  //将ARGB转化为十六进制
        hexCodeTextView.setText(hexCode);
    }

    protected void resetButton(){
        redSlider.setValue(0);
        greenSlider.setValue(0);
        blueSlider.setValue(0);
        alphaSlider.setValue(255);
    }

    protected void copyButton(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text",hexCodeTextView.getText().toString());
        Log.d("fuckClip",String.valueOf(clip));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this,"已复制",Toast.LENGTH_SHORT).show();
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  //检查兼容新
            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

            // 判断是否为深色模式
            boolean isDarkTheme = (getResources().getConfiguration().uiMode &
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;

            // 设置状态栏图标颜色
            windowInsetsController.setAppearanceLightStatusBars(!isDarkTheme);
        }

        //初始化视图
        initView();
        //设置滑块监听
        setupSeekBarListeners();
        //更新初始颜色
        updateColor();
    }
}