package com.aries.screenrecord;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private static Context context;

    private LinearLayout linearLayout = null;
    private Button buttonRecord = null;
    private Button buttonCapture = null;

    private boolean isRecord = false;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        getScreenBaseInfo();

        linearLayout = findViewById(R.id.linearLayout);
        buttonRecord = findViewById(R.id.buttonRecord);
        buttonCapture = findViewById(R.id.buttonCapture);

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRecord) {
                    stopScreenRecord();
                } else {
                    startScreenRecord();
                }
            }
        });

        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture(linearLayout);
            }
        });
    }

    /**
     * 获取全局上下文
     */
    public static Context getContext() {
        return context;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                //获得录屏权限，启动Service进行录制
                Intent intent = new Intent(MainActivity.this, ScreenRecordService.class);
                intent.putExtra("resultCode", resultCode);
                intent.putExtra("resultData", data);
                intent.putExtra("mScreenWidth", mScreenWidth);
                intent.putExtra("mScreenHeight", mScreenHeight);
                intent.putExtra("mScreenDensity", mScreenDensity);
                startForegroundService(intent);
                Toast.makeText(this, "录屏开始", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录屏失败", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //start screen record
    private void startScreenRecord() {
        //Manages the retrieval of certain types of MediaProjection tokens.
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //Returns an Intent that must passed to startActivityForResult() in order to start screen capture.
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, 1000);
        isRecord = true;
        buttonRecord.setText(new String("停止录屏"));
    }

    //stop screen record.
    private void stopScreenRecord() {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
        isRecord = false;
        buttonRecord.setText(new String("开始录屏"));
        Toast.makeText(this, "录屏成功", Toast.LENGTH_SHORT).show();

    }

    public void capture(View v) {
        //格式化时间作为截屏文件名
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YY-MM-DD-HH-MM-SS");
        //在获取外部存储的时候，一定注意添加权限，如果添加权限还不能成功，则手动在应用中开启权限。
        String filePathName = Environment.getExternalStorageDirectory() + "/" + simpleDateFormat.format(new Date()) + ".png";
        //Find the topmost view in the current view hierarcht.
        View view = v.getRootView();
        // Enable or disable drawing cache.
        view.setDrawingCacheEnabled(true);
        // Calling this method is equivalent to calling buildDrawingCache(false);
        // In order to force drawing cache to be buuild.
        view.buildDrawingCache();
        //Calling this method is equivalent to calling getDrawingCache(false);
        //Return the Bitmap in which this view drawing is cached.
        Bitmap bitmap = view.getDrawingCache();
        try {
            System.out.println(filePathName);
            FileOutputStream fileOutputStream = new FileOutputStream(filePathName);
            //Write a compressed version of bitmap to specified outputStream.
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            Toast.makeText(this, "Cpature Succeed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cpature Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 在这里将BACK键模拟了HOME键的返回桌面功能（并无必要）
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            simulateHome();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取屏幕基本信息
     */
    private void getScreenBaseInfo() {
        //A structure describing general information about a display, such as its size, density, and font scaling.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
    }

    /**
     * 模拟HOME键返回桌面的功能
     */
    private void simulateHome() {
        //Intent.ACTION_MAIN,Activity Action: Start as a main entry point, does not expect to receive data.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //If set, this activity will become the start of a new task on this history stack.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //This is the home activity, that is the first activity that is displayed when the device boots.
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

}
