package com.zoho.salesiq.androidsdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zoho.salesiqembed.ZohoSalesIQ;

public class SecondActivity extends AppCompatActivity
{
    Button likeButton, dislikeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ZohoSalesIQ.Tracking.setPageTitle("SalesIQ Demo | Second");

        likeButton = findViewById(R.id.like_button);
        dislikeButton = findViewById(R.id.dislike_button);

        likeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ZohoSalesIQ.Tracking.setCustomAction("Liked");  //to set custom action performed by visitor
            }
        });

        dislikeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ZohoSalesIQ.Tracking.setCustomAction("Disliked");  //to set custom action performed by visitor
            }
        });
    }
}
