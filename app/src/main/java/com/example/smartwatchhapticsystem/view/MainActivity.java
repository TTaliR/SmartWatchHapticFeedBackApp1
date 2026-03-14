package com.example.smartwatchhapticsystem.view;

import android.Manifest;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.smartwatchhapticsystem.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "MainActivity";

    // UI Components
    private TextView tvCurrentTime;
    private TextView tvCurrentDate;
    private TextView tvBluetoothStatus;
    private TextView tvLocationStatus;
    private View bluetoothStatusIndicator;
    private View locationStatusIndicator;

    // Time update handler
    private Handler timeHandler;
    private Runnable timeRunnable;
    private static final long TIME_UPDATE_INTERVAL = 1000; // Update every second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Start time updates
        startTimeUpdates();

        // Request necessary permissions
        checkAndRequestPermissions();
        requestCompanionAssociation();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        bluetoothStatusIndicator = findViewById(R.id.bluetoothStatusIndicator);
        locationStatusIndicator = findViewById(R.id.locationStatusIndicator);
    }

    /**
     * Start real-time clock updates
     */
    private void startTimeUpdates() {
        timeHandler = new Handler(Looper.getMainLooper());
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                timeHandler.postDelayed(this, TIME_UPDATE_INTERVAL);
            }
        };
        // Initial update
        updateTimeDisplay();
        // Start periodic updates
        timeHandler.postDelayed(timeRunnable, TIME_UPDATE_INTERVAL);
    }

    /**
     * Update time and date display
     */
    private void updateTimeDisplay() {
        Date now = new Date();

        // Format time (HH:mm)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        tvCurrentTime.setText(timeFormat.format(now));

        // Format date (EEE, MMM dd)
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
        tvCurrentDate.setText(dateFormat.format(now));
    }

    /**
     * Stop time updates to prevent memory leaks
     */
    private void stopTimeUpdates() {
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    /**
     *  Request necessary permissions before starting the server
     */
    private void checkAndRequestPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.WAKE_LOCK
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.WAKE_LOCK // Required on all versions for keeping service alive
            };
        }

        boolean permissionsGranted = true;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }

        if (!permissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, start services
            startServices();
        }

        // Update UI based on current permission status
        updatePermissionStatusUI();
    }

    /**
     * Update UI to reflect current permission status
     */
    private void updatePermissionStatusUI() {
        // Check Bluetooth permission
        boolean bluetoothGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            bluetoothGranted = true; // Not required on older versions
        }

        // Check Location/Body Sensors permission
        boolean locationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                == PackageManager.PERMISSION_GRANTED;

        // Update Bluetooth status
        if (bluetoothGranted) {
            tvBluetoothStatus.setText(R.string.status_ready);
            tvBluetoothStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connected));
            bluetoothStatusIndicator.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.status_connected));
        } else {
            tvBluetoothStatus.setText(R.string.status_no_permission);
            tvBluetoothStatus.setTextColor(ContextCompat.getColor(this, R.color.status_disconnected));
            bluetoothStatusIndicator.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.status_disconnected));
        }

        // Update Location/Sensors status
        if (locationGranted) {
            tvLocationStatus.setText(R.string.status_active);
            tvLocationStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connected));
            locationStatusIndicator.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.status_connected));
        } else {
            tvLocationStatus.setText(R.string.status_no_permission);
            tvLocationStatus.setTextColor(ContextCompat.getColor(this, R.color.status_disconnected));
            locationStatusIndicator.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.status_disconnected));
        }
    }


    /**
     *  Start Background Monitoring Service and Bluetooth GATT Server
     */
    private void startServices() {
        startBackgroundService();
    }

    /**
     *  Start Background Monitoring Service
     */
    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, BackgroundMonitoringService.class);
        startForegroundService(serviceIntent);
        Log.d(TAG, "Background Service Started");
    }

    /**
     *  Handle Permission Request Result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
                startServices();
            } else {
                Toast.makeText(this, "Required permissions denied!", Toast.LENGTH_SHORT).show();
            }

            // Update UI to reflect new permission status
            updatePermissionStatusUI();
        }
    }



    private void requestCompanionAssociation() {
        CompanionDeviceManager cdm = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);

        AssociationRequest request;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            request = new AssociationRequest.Builder()
                    .setSingleDevice(true)
                    .setDeviceProfile(AssociationRequest.DEVICE_PROFILE_WATCH) //  For smartwatches
                    .build();
        } else {
            request = new AssociationRequest.Builder()
                    .setSingleDevice(true)
                    .build(); // No profile for < API 31
        }


        cdm.associate(request, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(@NonNull IntentSender chooserLauncher) {
                try {
                    startIntentSenderForResult(chooserLauncher, 1234, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(CharSequence error) {
                Log.e(TAG, "❌ CDM association failed: " + error);
            }
        }, null);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1234 && resultCode == RESULT_OK) {
            Log.d(TAG, "✅ Companion device associated successfully");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        // Resume time updates
        if (timeHandler != null && timeRunnable != null) {
            updateTimeDisplay();
            timeHandler.postDelayed(timeRunnable, TIME_UPDATE_INTERVAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop time updates when activity is paused to save battery
        stopTimeUpdates();
    }

    /**
     *  Stop Services when the app is destroyed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();


        Log.d(TAG, "MainActivity Destroyed, Stopping Services");
        Intent serviceIntent = new Intent(this, BackgroundMonitoringService.class);
        stopService(serviceIntent);
    }
}
