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
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
    private EditText mClientDeviceName;

    private LinearLayout mConnectedLayout;
    private ListView mServerListView;
    private TextView mAddServerButton;
    private ServerDevices mServerDevices;
    private FallertNetworkService mFallertNetworkService;


    @SuppressLint("UnsafeOptInUsageError")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        mPreviewView = findViewById(R.id.qr_code_scan_surface);
        mClientDeviceName = findViewById(R.id.device_name);

        mConnectedLayout = findViewById(R.id.connected_layout);

        mServerListView = findViewById(R.id.servers_list);
        mAddServerButton = findViewById(R.id.add_server);
        mServerDevices = new ServerDevices(this, mServerListView);
        mAddServerButton.setOnClickListener(v -> {
            mConnectedLayout.setVisibility(LinearLayout.INVISIBLE);
            mClientDeviceName.setText(Settings.Global.getString(getContentResolver(), "device_name"));
            mFallertNetworkService.stopClientThreads();
            scanQRCode();
        });

        mSharedPreferences = getSharedPreferences("com.cap6411.fallert_alertee", Context.MODE_PRIVATE);
        mClientDeviceName.setText(mSharedPreferences.getString("client_device_name", Settings.Global.getString(getContentResolver(), "device_name")));

        if (mSharedPreferences.getString("server_ip_addresses", "").equals("")) scanQRCode();
        else {
            String barDividedIPString = mSharedPreferences.getString("server_ip_addresses", "");
            mServerDevices.addViaBarDividedString(barDividedIPString);
            mConnectedLayout.setVisibility(LinearLayout.VISIBLE);
            mFallertNetworkService = new FallertNetworkService();
            for(ServerDevice server : mServerDevices.getDevices()) {
                mFallertNetworkService.startClientThread(server.mLastIP);
            }
        }
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
                        if (mClientDeviceName.getText().toString().equals("")) {
                            Toast.makeText(this, "Please enter a device name", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Barcode barcode = barcodes.get(0);
                        mServerDevices.addDevice("Retrieving Title ...", barcode.getRawValue());
                        mFallertNetworkService.startClientThread(barcode.getRawValue());
                        Context context = getApplicationContext();
                        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        String clientIPAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("server_ip_addresses", mServerDevices.getBarDividedString()).apply();
                        editor.putString("client_ip_address", clientIPAddress).apply();
                        editor.putString("client_device_name", mClientDeviceName.getText().toString()).apply();
                        mConnectedLayout.setVisibility(LinearLayout.VISIBLE);
                    }
                }).addOnCompleteListener(task -> {
                    image.close();
                    cameraController.clearImageAnalysisAnalyzer();
                    mScanner.close();
                    cameraController.unbind();
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
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("server_ip_addresses", mServerDevices.getBarDividedString()).apply();
        mFallertNetworkService.stopClientThreads();
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