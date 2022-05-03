package com.gorets.khub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Trace;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScanCode extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSIONS = 5555;
    private final String[] REQUEST_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    private final int SUSPENSION_TIME = 3500;
    PreviewView previewView;
    public boolean isProcess;
    ImageCapture imageCapture;

    SharedPreferences storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);

        previewView = findViewById(R.id.camera);

        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }

    public void qRCodeHandler(String qrCodeText) {
        Context context = this;

        if (qrCodeText != null) {
            String[] map = qrCodeText.split("/");
            if (map[0].equals("keyTOTP")){
                saveData(map[1]);
                goMain();
            }else if(map[0].equals("https:") || map[0].equals("http:")){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeText));
                startActivity(browserIntent);
            }else {
                runOnUiThread(() -> Toast.makeText(context, qrCodeText, Toast.LENGTH_LONG).show());
            }
        }

        new Thread(() -> {
            try {
                Thread.sleep(SUSPENSION_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isProcess = false;
        }).start();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), new QRCodeDecoder(this));

        ImageCapture.Builder builder = new ImageCapture.Builder();

        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        Preview preview = new Preview.Builder().build();

        imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException ignored) {

            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionGranted() {
        for (String permission : REQUEST_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                this.finish();
            }
        }
    }


    public void goMain() {
        Intent instant = new Intent(this, MainActivity.class);
        startActivity(instant);
    }

    private void saveData(String data) {
        storage = getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor ed = storage.edit();
        ed.putString("SECRET", data);
        ed.commit();
    }
}