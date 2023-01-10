package com.salesiq.demoapp.notification;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salesiq.demoapp.BuildConfig;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /*
     * Refer https://www.zoho.com/salesiq/help/developer-guides/android-mobile-push-notification-2.0.html to
     * configure Push notifications for Android SDK in Zoho SalesIQ
     */

    final private static String TAG = "mobilisten:firebase";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> extras = remoteMessage.getData();
        Log.d(TAG, extras.toString());
        ZohoSalesIQ.Notification.handle(this.getApplicationContext(), extras);
    }

    @Override
    public void onNewToken(String token) {
        ZohoSalesIQ.Notification.enablePush(token, BuildConfig.DEBUG);
    }
}