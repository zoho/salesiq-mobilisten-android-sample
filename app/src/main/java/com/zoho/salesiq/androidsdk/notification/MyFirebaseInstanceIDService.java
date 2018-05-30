package com.zoho.salesiq.androidsdk.notification;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.zoho.salesiqembed.ZohoSalesIQ;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        ZohoSalesIQ.Notification.enablePush(refreshedToken,true);
    }
}






