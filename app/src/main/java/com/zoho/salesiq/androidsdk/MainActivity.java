package com.zoho.salesiq.androidsdk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zoho.commons.ChatComponent;
import com.zoho.commons.Color;
import com.zoho.livechat.android.exception.InvalidVisitorIDException;
import com.zoho.salesiqembed.ZohoSalesIQ;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    Button loginButton, logoutButton, navigateButton, prechatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.login_button);
        logoutButton = findViewById(R.id.logout_button);
        navigateButton = findViewById(R.id.next_button);
        prechatButton = findViewById(R.id.prechat_button);

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //visitor information after login successful
                ZohoSalesIQ.Visitor.setName("");
                ZohoSalesIQ.Visitor.setEmail("");
                ZohoSalesIQ.Visitor.setContactNumber("");
                try
                {
                    ZohoSalesIQ.registerVisitor("123456"); //Visitor unique id
                } catch (InvalidVisitorIDException e)
                {
                    e.printStackTrace();
                }
                ZohoSalesIQ.Visitor.addInfo("User Type","Premium"); //Custom visitor information
            }
        });



        //Tracking
        ZohoSalesIQ.Tracking.setPageTitle("SalesIQ Demo | Home");

        //Chat Customization
        ZohoSalesIQ.Chat.setVisibility(ChatComponent.rating,true); //To show rating after chat ends
        ZohoSalesIQ.Chat.setVisibility(ChatComponent.feedback,true); //To show feedback after chat ends
        ZohoSalesIQ.Chat.setVisibility(ChatComponent.operatorImage,true); //To show operator image in chat conversation
        ZohoSalesIQ.Chat.setLanguage(Locale.ENGLISH); //To set visitor locale
        ZohoSalesIQ.Chat.setTitle("Chat with us!"); //No set chat title
        ZohoSalesIQ.Chat.showOperatorImageInLauncher(true); //To show operator image in launcher FAB when chat is connected

        //Chat & Conversation theme
        ZohoSalesIQ.Chat.setThemeColor("colorPrimary",new Color(64,188,189));
        ZohoSalesIQ.Chat.setThemeColor("colorPrimaryDark",new Color(44,131,132));

        //Conversation customization
        ZohoSalesIQ.Conversation.setVisibility(true);  //Enable/disable conversation
        ZohoSalesIQ.Conversation.setTitle("Conversation history"); //Conversation title

        //FAQ customization
        ZohoSalesIQ.FAQ.setVisibility(true); //Enable/disable FAQ

        logoutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ZohoSalesIQ.unregisterVisitor(logoutButton.getContext()); //this will clear visitor data and make him anonymous
            }
        });

        navigateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

        prechatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, PrechatFormActivity.class);
                startActivity(intent);
            }
        });


    }
}
