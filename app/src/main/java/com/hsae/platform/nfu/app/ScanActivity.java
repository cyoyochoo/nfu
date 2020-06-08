package com.hsae.platform.nfu.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate {
//    private ZBarView zBarView;
    private ZXingView zXingView;
    private boolean alreadyGot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        zXingView = findViewById(R.id.zxingview);
        zXingView.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        zXingView.startCamera();
        zXingView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        zXingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zXingView.onDestroy();
        super.onDestroy();
    }

    @Override
    public synchronized void onScanQRCodeSuccess(String result) {
        if (alreadyGot) return;
        alreadyGot = true;
        if (result.startsWith("\uFEFF")) result = result.substring(1);
        setResult(RESULT_OK, new Intent().putExtra("result", result));
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Snackbar.make(zXingView, "打开相机出错！", Snackbar.LENGTH_SHORT).show();
    }
}
