package main.com.dash.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import main.com.dash.MainActivity;
import main.com.dash.R;
import main.com.dash.app.Config;
import main.com.dash.constant.GPSTracker;
import main.com.dash.constant.MySession;
import main.com.dash.utils.NotificationUtils;

public class SplashActivity extends AppCompatActivity {
    MySession mySession;
    public static final int RequestPermissionCode = 1;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    protected static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static double latitude = 0.0, longitude = 0.0;
    GPSTracker gpsTracker;
    private String user_log_data = "", firebase_regid = "";
    private boolean isAccepted=false;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        gpsTracker = new GPSTracker(SplashActivity.this);
         locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                }
            }
        };

        mySession = new MySession(this);
        requestPermission();
        printHashKey(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(SplashActivity.this, new String[]
                {
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            for (int a:grantResults){
                isAccepted=a==0;
                if (!isAccepted){
                    break;
                }
            }
            if (isAccepted){
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Handlers();
                }else {
                    nesePermission();
                }
            }else {
                requestPermission();
            }
    }

    public void nesePermission() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(SplashActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.allowpermission));
        alertDialog.setMessage(getResources().getString(R.string.nessper));
        alertDialog.setPositiveButton(getResources().getString(R.string.allow), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(getResources().getString(R.string.closeapp), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.show();
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        firebase_regid = pref.getString("regId", null);
        Log.e("Splash", "Firebase reg id: " + firebase_regid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        NotificationUtils.clearNotifications(getApplicationContext());
        if (isAccepted){
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Handlers();
            }else {
                nesePermission();
            }
        }
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.e(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }
    private void Handlers(){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        if (gpsTracker.canGetLocation()) {
                            latitude = gpsTracker.getLatitude();
                            longitude = gpsTracker.getLongitude();
                        }
                        if (mySession.IsLoggedIn()){
                            Intent i = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }else {
                            Intent i = new Intent(SplashActivity.this, WelcomeAct.class);
                            startActivity(i);
                            finish();
                        }
                }
            }, 1500);
    }

}


