package com.cap6411.fallert_alertee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cap6411.fallert_alertee.devices.ServerDevice;
import com.cap6411.fallert_alertee.devices.ServerDevices;
import com.cap6411.fallert_alertee.network.FallertEvent;
import com.cap6411.fallert_alertee.network.FallertEventFall;
import com.cap6411.fallert_alertee.network.FallertInformationEvent;
import com.cap6411.fallert_alertee.network.FallertNetworkService;
import com.cap6411.fallert_alertee.network.FallertRemoveDeviceEvent;
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
    private String clientIPAddress;
    private PreviewView mPreviewView;
    private LifecycleCameraController cameraController;
    private BarcodeScanner mScanner;
    private EditText mClientDeviceName;
    private  ConstraintLayout mQRCodeView;
    private ConstraintLayout mConnectedLayout;
    private ConstraintLayout mFallEventView;
    private ImageView mFallEvenBitmap;
    private TextView mFallEventTitleAndDescription;
    private ImageView mFallEventOkay;
    private ListView mServerListView;
    private TextView mAddServerButton;
    private ServerDevices mServerDevices;
    private FallertNetworkService mFallertNetworkService;
    private Thread mRecievedFallertEventHandler;


    @SuppressLint("UnsafeOptInUsageError")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wm.getConnectionInfo().getIpAddress();
        clientIPAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        mQRCodeView = findViewById(R.id.qr_code_view);
        mPreviewView = findViewById(R.id.qr_code_scan_surface);

        mFallEventView = findViewById(R.id.fall_event_view);
        mFallEvenBitmap = findViewById(R.id.fall_event_bitmap);
        mFallEventTitleAndDescription = findViewById(R.id.fall_event_title_and_description);
        mFallEventOkay = findViewById(R.id.fall_event_okay);
        mFallEventOkay.setOnClickListener(v -> {
            mFallEventView.setVisibility(ConstraintLayout.INVISIBLE);
            FallertNetworkService.mIsClientProcessing = false;
        });

        mFallertNetworkService = new FallertNetworkService();
        mClientDeviceName = findViewById(R.id.device_name);
        mConnectedLayout = findViewById(R.id.connected_layout);
        mServerListView = findViewById(R.id.servers_list);
        mAddServerButton = findViewById(R.id.add_server);
        mServerDevices = new ServerDevices(this, mServerListView, clientIPAddress, mFallertNetworkService::removeServer);
        mAddServerButton.setOnClickListener(v -> {
            mClientDeviceName.setText(Settings.Global.getString(getContentResolver(), "device_name"));
            mFallertNetworkService.stopClientThreads();
            scanQRCode();
        });

        mSharedPreferences = getSharedPreferences("com.cap6411.fallert_alertee", Context.MODE_PRIVATE);
        mClientDeviceName.setText(mSharedPreferences.getString("client_device_name", Settings.Global.getString(getContentResolver(), "device_name")));

        if (mSharedPreferences.getString("server_ip_addresses", "").equals("")) {
            scanQRCode();
        }
        else {
            mQRCodeView.setVisibility(ConstraintLayout.INVISIBLE);
            String barDividedIPString = mSharedPreferences.getString("server_ip_addresses", "");
            mServerDevices.parse(barDividedIPString);
            for(ServerDevice server : mServerDevices.getDevices()) {
                mFallertNetworkService.startClientThread(server.mLastIP);
            }
        }

        mRecievedFallertEventHandler = new Thread(() -> {
            try {
                while (true) {
                    if (mFallertNetworkService == null) continue;
                    if (FallertNetworkService.mEventQueue.size() == 0) continue;
                    FallertEvent event = FallertNetworkService.mEventQueue.poll();
                    if (event == null) continue;
                    if (event.getEventType() == FallertEvent.FallertEventType.FALL) {
                        FallertEventFall fallEvent = (FallertEventFall) event;
                        runOnUiThread(() -> {
                            mFallEventView.setVisibility(ConstraintLayout.VISIBLE);
                            mFallEventTitleAndDescription.setText(fallEvent.getTitle() + "\n" + fallEvent.getDescription());
                            mFallEvenBitmap.setImageBitmap(fallEvent.getBitmap());
                        });
                        new ToneGenerator(AudioManager.STREAM_MUSIC, 100).startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,1000);
                    }
                    else if (event.getEventType() == FallertEvent.FallertEventType.INFORMATION) {
                        FallertInformationEvent serverInformation = (FallertInformationEvent) event;
                        runOnUiThread(() -> {
                            mServerDevices.updateDevice(serverInformation.getInformation(), serverInformation.getIPAddress());
                        });
                        FallertInformationEvent clientInformation = new FallertInformationEvent(String.valueOf(System.currentTimeMillis()), mSharedPreferences.getString("client_ip_address", "Unknown"), mSharedPreferences.getString("client_device_name", "Unknown"));
                        mFallertNetworkService.sendSingleEventToServer(serverInformation.getIPAddress(), clientInformation);
                    }
                    else if (event.getEventType() == FallertEvent.FallertEventType.REMOVE_DEVICE) {
                        FallertRemoveDeviceEvent removeEvent = (FallertRemoveDeviceEvent) event;
                        runOnUiThread(() -> {
                            mServerDevices.removeDevice(removeEvent.getIPAddress());
                        });
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
        mRecievedFallertEventHandler.start();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanQRCode(){
        mQRCodeView.setVisibility(ConstraintLayout.VISIBLE);
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
                        mFallertNetworkService.startClientThread(barcode.getRawValue());
                        mServerDevices.addDevice("Unknown", barcode.getRawValue());

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("server_ip_addresses", mServerDevices.toString()).apply();
                        editor.putString("client_ip_address", clientIPAddress).apply();
                        editor.putString("client_device_name", mClientDeviceName.getText().toString()).apply();

                        mQRCodeView.setVisibility(ConstraintLayout.INVISIBLE);
                        cameraController.unbind();
                        mScanner.close();
                        cameraController.clearImageAnalysisAnalyzer();
                    }
                    else {
                        image.close();
                    }
                });
            }
        });

        cameraController.bindToLifecycle(this);
        mPreviewView.setController(cameraController);
    }
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("server_ip_addresses", mServerDevices.toString()).apply();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanner != null) mScanner.close();
        if (cameraController != null) cameraController.unbind();
        mRecievedFallertEventHandler.interrupt();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("server_ip_addresses", mServerDevices.toString()).apply();
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