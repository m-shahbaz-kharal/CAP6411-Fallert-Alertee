package com.cap6411.fallert_alertee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    int PERMISSION_REQUESTS = 1;
    private SharedPreferences mSharedPreferences;
    private PreviewView mPreviewView;
    private LifecycleCameraController cameraController;
    private BarcodeScanner mScanner;
    private String mIPAddress = null;

    private LinearLayout mConnectedLayout;
    private Button mReRegister;


    @SuppressLint("UnsafeOptInUsageError")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        mPreviewView = findViewById(R.id.qr_code_scan_surface);

        mConnectedLayout = findViewById(R.id.connected_layout);
        mReRegister = findViewById(R.id.base_reregister);
        mReRegister.setOnClickListener(v -> {
            mConnectedLayout.setVisibility(LinearLayout.INVISIBLE);
            mIPAddress = null;
            scanQRCode();
        });

        mSharedPreferences = getSharedPreferences("com.cap6411.fallert_alertee", Context.MODE_PRIVATE);
        mIPAddress = mSharedPreferences.getString("ip_address", null);

        if(mIPAddress == null) scanQRCode();
        else mConnectedLayout.setVisibility(LinearLayout.VISIBLE);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanQRCode(){
        cameraController = new LifecycleCameraController(this);
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        mScanner = BarcodeScanning.getClient(options);
        cameraController.setImageAnalysisAnalyzer(ActivityCompat.getMainExecutor(this), image -> {
            if (image.getImage() != null) {
                InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                mScanner.process(inputImage).addOnSuccessListener(barcodes -> {
                    if (barcodes.size() > 0) {
                        Barcode barcode = barcodes.get(0);
                        mIPAddress = barcode.getRawValue();
                    }
                }).addOnCompleteListener(task -> {
                    image.close();
                    if (mIPAddress != null) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("ip_address", mIPAddress).apply();
                        cameraController.clearImageAnalysisAnalyzer();
                        mScanner.close();
                        cameraController.unbind();
                        mConnectedLayout.setVisibility(LinearLayout.VISIBLE);
                    }
                });
            }
        });

        cameraController.bindToLifecycle(this);
        mPreviewView.setController(cameraController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanner.close();
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) return false;
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
}