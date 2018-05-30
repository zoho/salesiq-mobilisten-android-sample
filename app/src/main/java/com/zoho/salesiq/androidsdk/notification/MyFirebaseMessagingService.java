package com.zoho.salesiq.androidsdk.notification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zoho.salesiq.androidsdk.R;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Map extras = remoteMessage.getData();
        ZohoSalesIQ.Notification.handle(this.getApplicationContext(),extras, R.mipmap.ic_launcher);
    }
}
