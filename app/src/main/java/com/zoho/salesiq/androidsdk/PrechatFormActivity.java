package com.zoho.salesiq.androidsdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zoho.salesiqembed.ZohoSalesIQ;

public class PrechatFormActivity extends AppCompatActivity
{
    private EditText nameText, emailText, contactText, messageText;
    private Button startChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prechat_form);

        nameText = findViewById(R.id.name_edittext);
        emailText = findViewById(R.id.email_edittext);
        contactText = findViewById(R.id.contactno_edittext);
        messageText = findViewById(R.id.message_edittext);

        startChatButton = findViewById(R.id.start_chat_button);
        startChatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String name = nameText.getText().toString().trim();
                String email = emailText.getText().toString().trim();
                String contact = contactText.getText().toString().trim();
                String message = messageText.getText().toString().trim();

                if (name.length() > 0 && email.length() > 0 && contact.length() > 0 && message.length() > 0)
                {
                    ZohoSalesIQ.Visitor.setName(name);
                    ZohoSalesIQ.Visitor.setEmail(email);
                    ZohoSalesIQ.Visitor.setContactNumber(contact);
                    ZohoSalesIQ.Visitor.startChat(message);
                }else
                {
                    Toast.makeText(PrechatFormActivity.this, "Please don't leave fields empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
