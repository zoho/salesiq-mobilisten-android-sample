package com.zoho.salesiq.androidsdk;

import android.app.Application;

import com.zoho.salesiqembed.ZohoSalesIQ;

public class MyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        //initialize SalesIQ
        ZohoSalesIQ.init(this,"","");

        //by default launcher will be hidden can be enabled with following line.
        ZohoSalesIQ.showLauncher(true);
    }
}
