/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2017 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.activities.js.WebAppInterface;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.services.PreyOverlayService;

public class CheckPasswordHtmlActivity extends AppCompatActivity {

    public static String JS_ALIAS="Android";
    public static String URL_ONB = "file:///android_asset/html/index.html";
    //public static String URL_ONB = "http://10.10.2.91:3000/";

    public static final String CLOSE_PREY = "close_prey";
    private final BroadcastReceiver close_prey_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreyLogger.d("CheckPasswordHtmlActivity BroadcastReceiver: finish");
            finish();
        }
    };

    private WebView myWebView = null;

    public static int OVERLAY_PERMISSION_REQ_CODE = 5469;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.webview);
        PreyLogger.d("CheckPasswordHtmlActivity: onCreate");
        registerReceiver(close_prey_receiver, new IntentFilter(CLOSE_PREY));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreyLogger.d("CheckPasswordHtmlActivity: onResume");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        Bundle extras = getIntent().getExtras();
        String nexturl = "";
        try {
            nexturl = extras.getString("nexturl");
        } catch (Exception e) {
        }
        PreyLogger.d("CheckPasswordHtmlActivity nexturl: " + nexturl);
        if ("tryReport".equals(nexturl)) {
            tryReport();
        } else {
            loadUrl();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(close_prey_receiver);
    }

    public void settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browser);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
    }

    public void tryReport() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport");
        String lng = PreyUtils.getLanguage();
        String url = URL_ONB + "#/" + lng + "/activation";
        settings();
        PreyLogger.d("_url:" + url);
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(url);
    }


    public void loadUrl() {
        PreyLogger.d("CheckPasswordHtmlActivity: loadUrl");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
    }

    public void reload() {
        PreyLogger.d("CheckPasswordHtmlActivity: reload");
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
        myWebView.reload();
    }


    public String getUrl(Context ctx) {
        String lng = PreyUtils.getLanguage();
        String url = "";

        String deviceKey = PreyConfig.getPreyConfig(this).getDeviceId();
        PreyLogger.d("CheckPasswordHtmlActivity: deviceKey:" + deviceKey);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES >=M" );
            boolean canAccessFineLocation = PreyPermission.canAccessFineLocation(this);
            boolean canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this);
            boolean canAccessCamera = PreyPermission.canAccessCamera(this);
            boolean canAccessReadPhoneState = PreyPermission.canAccessReadPhoneState(this);
            boolean canAccessWriteExternalStorage = PreyPermission.canAccessWriteExternalStorage(this);
            boolean canAccessBackgroundLocation =PreyPermission.canAccessBackgroundLocation(this);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessFineLocation:" + canAccessFineLocation);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCoarseLocation:" + canAccessCoarseLocation);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCamera:" + canAccessCamera);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessReadPhoneState:" + canAccessReadPhoneState);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessWriteExternalStorage:" + canAccessWriteExternalStorage);
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessBackgroundLocation:" + canAccessBackgroundLocation);

            boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
            PreyLogger.d("CheckPasswordHtmlActivity: canDrawOverlays:" + canDrawOverlays);
            boolean isAdminActive = FroyoSupport.getInstance(this).isAdminActive();
            PreyLogger.d("CheckPasswordHtmlActivity: isAdminActive:" + isAdminActive);
            boolean configurated=canAccessFineLocation||canAccessCoarseLocation || canAccessCamera
                    || canAccessReadPhoneState || canAccessWriteExternalStorage || canDrawOverlays || canAccessBackgroundLocation||isAdminActive;
            PreyLogger.d("CheckPasswordHtmlActivity: configurated:" + configurated);
            if (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                    && canAccessReadPhoneState && canAccessWriteExternalStorage && canDrawOverlays && canAccessBackgroundLocation&&isAdminActive) {
                    if (deviceKey != null && deviceKey != "") {
                        url = URL_ONB + "#/" + lng + "/";
                    } else {
                        url = URL_ONB + "#/" + lng + "/signin";
                    }

            } else {
                if (configurated) {
                    url = URL_ONB + "#/" + lng + "/permissions";
                } else {
                    url = URL_ONB + "#/" + lng + "/start";
                }
            }
        }else{
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES <M" );
            if (deviceKey != null && deviceKey != ""  ) {
                url = URL_ONB + "#/" + lng + "/";
            } else {
                url = URL_ONB + "#/" + lng + "/signin";
            }


        }

        PreyLogger.d("_url:" + url);
        return url;
    }

    private static final int REQUEST_PERMISSIONS = 5;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermissionAndroid7() {
        PreyLogger.d("CheckPasswordHtmlActivity: askForPermissionAndroid7");
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        startOverlayService();
    }

    private void startOverlayService() {
        PreyLogger.d("CheckPasswordHtmlActivity: startOverlayService");
        Intent intent = new Intent(getApplicationContext(), PreyOverlayService.class);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void askForPermission() {
        ActivityCompat.requestPermissions(CheckPasswordHtmlActivity.this, INITIAL_PERMS, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult");

        boolean canDrawOverlays = PreyPermission.canDrawOverlays(this);
        if (!canDrawOverlays) {
            askForPermissionAndroid7();
            startOverlayService();
        } else {
            finish();
            Intent intent = new Intent(this, CheckPasswordHtmlActivity.class);
            startActivity(intent);
        }
    }

}

